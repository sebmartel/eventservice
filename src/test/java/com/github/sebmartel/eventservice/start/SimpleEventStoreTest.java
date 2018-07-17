package com.github.sebmartel.eventservice.start;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertThat;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import javax.management.RuntimeErrorException;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.base.Ticker;

@RunWith(Parameterized.class)
public class SimpleEventStoreTest {

	
    @Parameters(name= "{index}: {0} threads, {1} events, {2} tps, {3} expected, {4} ")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] { 
          			{ 1, 2000, 2000, 2000l }, 
        			{ 4, 10000000, 100000, 200000l }, // each thread simulates ~ 2500 / 100 ms
          			{ 1, 50, 100, 50 },
          			{ 1, 200, 40, 100 },
        });
    }    
    
	SimpleEventStore store;
	FakeTicker ticker;
	
	private final int nbThreads;
	private final int nbEvents;
	private final double tps;
	private final long expected;
		
	public SimpleEventStoreTest(int nbThreads, int nbEvents, double tps, long expected) {
		this.nbThreads = nbThreads;
		this.nbEvents = nbEvents;
		this.tps = tps;
		this.expected = expected;
	}

	@Before
	public void setUp() throws Exception {
		ticker = new FakeTicker(tps);
		store = new SimpleEventStore(Duration.ofMillis(2000), 100, ticker);
	}

	void postEvents(FakeTicker ticker, AtomicLong count) {
		long serial;
		while ((serial = count.getAndIncrement()) < nbEvents) {			
			;
			store.put( new Event(ticker.tick(), serial) );
		}
	}
		
	@Test
	public void testStore() throws InterruptedException {
		ExecutorService pool = Executors.newFixedThreadPool(nbThreads);
		
		List<Callable<Void>> posters = new ArrayList<>(nbThreads);
		AtomicLong serials = new AtomicLong();
		for (int i = 0; i < nbThreads; ++i) {
			posters.add( () -> {
				postEvents(ticker, serials);
				return null;
			});
		}
		
		List<Future<Void>> checks = pool.invokeAll(posters);
		checks.stream().forEach( f -> {
			try {
				f.get();
			} catch (InterruptedException | ExecutionException e) {
				throw new RuntimeException(e);
			}
		});
		
		Collection<Event> events = store.get();
//		events.stream().sorted().forEach( System.out::println );
		
		if (nbThreads == 1) {
			assertThat(events.stream().count(), is(expected));
		}
		assertThat(events.stream().count(), between(expected - nbThreads, expected + nbThreads));
	}
	
	Matcher<Long> between(long a, long b) {
		Matcher<Long> matcher = is( allOf( greaterThanOrEqualTo(a), lessThanOrEqualTo(b) ) );
		return matcher;
	}	
}


class FakeTicker extends Ticker {

	private final AtomicLong nanos = new AtomicLong();
	private volatile long autoIncrementStepNanos;

	public FakeTicker(double tps) {
		autoIncrementStepNanos = (long) (TimeUnit.SECONDS.toNanos(1) / tps);
	}

	public long readMillis() {
		return TimeUnit.NANOSECONDS.toMillis(read());
	}

	public long tick() {
		return nanos.getAndAdd(autoIncrementStepNanos);
	}

	@Override
	public long read() {
		return nanos.get();
	}
	
	public long getIncrement() {
		return autoIncrementStepNanos;
	}
}