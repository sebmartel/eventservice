/**
 * 
 */
package com.github.sebmartel.eventservice.start;

import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.google.common.base.Ticker;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * @author seb
 *
 */
public class SimpleEventStore {

	// both caches are by id.
	private Cache<Long, Event> lru;
	private Cache<Long, Event> fifo;
	private long minCount;

	public SimpleEventStore(Duration ttl, int minCount) {
		this(ttl, minCount, Ticker.systemTicker());
	}

	public SimpleEventStore(Duration ttl, int minCount, Ticker ticker) {
		this.minCount = minCount;
		lru = CacheBuilder.newBuilder().expireAfterWrite(ttl.toMillis(), TimeUnit.MILLISECONDS).ticker(ticker).build();
		fifo = CacheBuilder.newBuilder().maximumSize(minCount).ticker(ticker).build();
	}
	
	public void put(Event ev) {
		lru.put(ev.getItem().getId(), ev);
		fifo.put(ev.getItem().getId(), ev);
	}
	
	public Collection<Event> get() {
		Collection<Event> timed = lru.asMap().values();
		return timed.parallelStream().count() < minCount ? 
				fifo.asMap().values().stream().limit(minCount).collect(Collectors.toList()) 
				: timed;
	}
}
