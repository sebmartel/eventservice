
# Eventservice

Demonstration of a multithreaded in-memory cache with self-expiring
keys and a minimum retention count policy.

# Requirements

* Java 8
* Maven

# Getting started

Clone this repository and from the shell:

```
$ mvn clean install
$ mvn exec:java

```

The server is now listening on port 8080

```
$ curl --verbose http://localhost:8080/items -d "{ \"item\":{ \"id\": 123, \"timestamp\": \"2016-01-01T23:01:01.000Z\" } }"
*   Trying 127.0.0.1...
* Connected to localhost (127.0.0.1) port 8080 (#0)
> POST /items HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/7.47.0
> Accept: */*
> Content-Length: 65
> Content-Type: application/x-www-form-urlencoded
> 
* upload completely sent off: 65 out of 65 bytes
< HTTP/1.1 201 Created
< Date: Tue, 17 Jul 2018 09:05:36 GMT
< Content-Length: 0
< Server: Jetty(9.4.11.v20180605)
< 
* Connection #0 to host localhost left intact
```

An event was successfully posted. Let's confirm:

```
$ curl -X GET http://localhost:8080/items
[{"item":{"timestamp":"2016-01-01T23:01:01.000Z","id":123}}]
```

# Details

The events are stored in two guava cache:

```java

	protected SimpleEventStore(Duration ttl, int fifoSize, Ticker ticker) {
		this.minSize = fifoSize;
		store = CacheBuilder
			.newBuilder()
			.ticker(ticker)
			.expireAfterWrite(ttl.toMillis(), TimeUnit.MILLISECONDS)
			.build();
		recent = CacheBuilder.newBuilder().maximumSize(minSize).build();
	}	

```

The `store` keeps the last ttl seconds of posted events before
expiring content.  `recent` is a simple fifo, capped at 100 items.

Both caches are keyed by `id`, ensuring that `POST /events` is idempotent.

Insertion is constant time.  Extracting items is O(N) on average where
n is the number of items returned.  The API sorts them, so it's really
O(2N logN) before serialization.

Note that the backing data structure returned by the cache could be
the cache backing the timed cache. This is simply to avoid needless
data duplication and isn't a problem unless someone needs an
accurate count.

Space wise, the store is O(N) but at runtime, the memory needed is
proportional to the number of concurrent GET /items (strictly speaking
in terms of number of active references to items). Assuming a heavy
load of 100,000 TPS , that would be 21 Mb of space to host the
references needed to process the GET.

If more than 1 concurrent GET is the norm, an improvement to the store
would be to serve the results pre-sorted, which would enable the GET
handler to serialize directly from the backing collection without
having to sort into a temporary collection.

The first question is to determine what sort is desired. It could be
by id, by item timestamp or by arrival timestamp.  The implementation
could use a ConcurrentSkipListMap keyed by the desired property to
weak references of the events stored in the ttl cache. These could be
hidden behind a Collection implementation that would dereference or
skip the refernces during iteration.  A RemovalListener on the ttl
cache could explicitely remove the entries in the sorted map whenever
invoked to keep things tidy.

If the sorted property is the arrival time, another option could be to
leverage the fact that this is the same key as the ttl cache and use
an AtomicReferenceArray of size 2^n -1 large enough to cover the ttl
in milliseconds (or whatever other granularity).

Each of array position, or bucket, is easily accessible by masking the
upper bits representing the timestamp used for ordering. Storing the
events in a ConcurrentSkipListMap yields a logN insertion time, but
the N is bounded by the number of transactions over the slice of time
allocated for each bucket.  For 100,000 tps hashed into a bucket for a
1ms interval is about 1000 items: this N is very small.

What is gained by any of these options is more predictable use of
space for many concurrent GET /items, bounded by O(2N) with N being
the total number of items returned, instead of O(MN) with M being the
total number of concurrent GET.

