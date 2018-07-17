package com.github.sebmartel.eventservice.start;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Ticker;

public class SimpleEventStoreTest {

	SimpleEventStore store;
	FakeTicker ticker;
	
	@Before
	public void setUp() throws Exception {
		ticker = new FakeTicker(2000);
		store = new SimpleEventStore(Duration.ofMillis(2000), 100, ticker);
	}

	@Test
	public void testStore() {
		for (int i = 1; i <= 2000; ++i) {
			store.put( new Event(i, i));
			ticker.tick();
		}
		
		Collection<Event> events = store.get();
		assertThat(events.stream().count(), is(2000l));
	}
}

class FakeTicker extends Ticker {

	private final AtomicLong nanos = new AtomicLong();
	private volatile long autoIncrementStepNanos;

	public FakeTicker(double tps) {
		autoIncrementStepNanos = (long) (TimeUnit.SECONDS.toNanos(1) / tps);
	}

	public long tick() {
		return nanos.getAndAdd(autoIncrementStepNanos);
	}

	@Override
	public long read() {
		return nanos.get();
	}
}