
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
O(N logN).


