# akka-cache


Use Maven to build your project:

```shell
mvn compile
```

When running an Akka service locally.

To start your service locally, run:

```shell
mvn compile exec:java
```

This command will start your Akka service. With your Akka service running, the endpoint it's available at:

```shell
curl http://localhost:9001/...
```

To deploy your service, install the `akka` CLI as documented in
[Install Akka CLI](https://doc.akka.io/akka-cli/index.html)
and configure a Docker Registry to upload your docker image to.

You will need to update the `dockerImage` property in the `pom.xml` and refer to
[Configuring registries](https://doc.akka.io/operations/projects/container-registries.html)
for more information on how to make your docker image available to Akka.

Finally, you can use the [Akka Console](https://console.kalix.io)
to create a project and then deploy your service into the project by first packaging and publishing the docker image through `mvn deploy` and then deploying the image through the `akka` CLI.

_____
## Current APIs:
paths:
- POST /cache/{cacheName}/{key}
- POST /cache/{cacheName}/{key}/{ttlSeconds}
- GET /cache/cacheName/{cacheName}
- GET /cache/{cacheName}/keys
- GET /cache/cacheName/{cacheName}/keys
- DELETE /cache/cacheName/{cacheName}
- PUT /cache/cacheName/{cacheName}/flush
- GET /cache/{cacheName}/{key}
- POST /cache/batch/
- POST /cache/batch/get
- DELETE /cache/batch/
- POST /cache/set
- PUT /cache/cacheName
- DELETE /cache/{cacheName}/{key}
- POST /cache/cacheName
- GET /cache/get/{cacheName}/{key}

-----
## Curl Examples

We have two approaches to APIs; JSON, and Binary. Akka 3 really promotes the use of JSON and Jackson for doing message serialization. This is fine for most things but really isn’t as fast when working with large objects.

The most heavily used APIs for caches are the POST & GET of the cache itself. We’re currently supporting both JSON and Binary for these two APIs, and are the only ones that leverage binary.  The primary problem of using JSON for saving a cache is that it must be encoded as BASE64, and then decoded to be readable.
  
### Cache Name:

POST /cache/cacheName
```shell
curl -i -d '{"cacheName":"cache1", "description":"This is our first test"}' -H "Content-Type: application/json" -X POST http://localhost:9001/cache/cacheName
```

GET /cache/cacheName/{cacheName}
```shell
curl -i http://localhost:9001/cache/cacheName/cache1
```

POST /cache/cacheName/update
```shell
curl -i -d '{"cacheName":"cache1", "description":"This is our first modification"}' -H "Content-Type: application/json" -X POST http://localhost:9001/cache/cacheName/update
```

GET /cache/cacheName/{cacheName}/keys

> **_NOTE:_** execute the cache commands below before executing this
```shell
curl -i http://localhost:9001/cache/cacheName/cache1/keys
```

PUT /cacheName/{cacheName}/flush
```shell
curl -i -H "Content-Type: application/json" -X PUT http://localhost:9001/cacheName/cache1/flush
```

DELETE /cacheName/{cacheName}
```shell
curl -i -X DELETE http://localhost:9001/cacheName/cache1
```

### Cache:

BINARY POST (without TTL)

```shell
curl -i -d 'This is our first test' -H "Content-Type: application/octet-stream" -X POST http://localhost:9001/cache/cache1/key1
````

BINARY POST (with TTL at 30 seconds)
```shell
curl -i -d 'This is our first test' -H "Content-Type: application/octet-stream" -X POST http://localhost:9001/cache/cache1/key1/30
````

JSON POST /cache/set (this is the payload of one)
```shell
curl -i -d '{"cacheName":"cache1", "key":"key1", "value":"dGhpcyBpcyB0aGUgcGF5bG9hZCBvZiBvbmU="}' -H "Content-Type: application/json" -X POST http://localhost:9001/cache/set
```
(this is the payload of two)
```shell
curl -i -d '{"cacheName":"cache1", "key":"key2", "value":"dGhpcyBpcyB0aGUgcGF5bG9hZCBvZiB0d28="}' -H "Content-Type: application/json" -X POST http://localhost:9001/cache/set
```
(this is the payload of three)
```shell
curl -i -d '{"cacheName":"cache1", "key":"key3", "value":"dGhpcyBpcyB0aGUgcGF5bG9hZCBvZiB0aHJlZQ=="}' -H "Content-Type: application/json" -X POST http://localhost:9001/cache/set
```

(this is the payload of four, with ttl 35 seconds)
```shell
curl -i -d '{"cacheName":"cache1", "key":"key4", "value":"dGhpcyBpcyB0aGUgcGF5bG9hZCBvZiBmb3VyLCB3aXRoIHR0bCAzNSBzZWNvbmRzCg==","ttlSeconds":35}' -H "Content-Type: application/json" -X POST http://localhost:9001/cache 
```

BINARY GET:
```shell
curl -i http://localhost:9001/cache/cache1/key3
```
```shell
curl -i http://localhost:9001/cache/cache1/key4
```

JSON GET:

```shell
curl -i http://localhost:9001/cache/get/cache1/key1
```

DELETE /{cacheName}/{key}
```shell
curl -i -X DELETE http://localhost:9001/cache1/key31
```

### Batch

Batch Cache (JSON):
```shell
curl -i -d '{ "cacheRequests" : [{"cacheName":"cache1", "key":"key1", "value":"dGhpcyBpcyB0aGUgcGF5bG9hZCBvZiBvbmU="}, {"cacheName":"cache1", "key":"key2", "value":"dGhpcyBpcyB0aGUgcGF5bG9hZCBvZiB0d28="}, {"cacheName":"cache1", "key":"key3", "value":"dGhpcyBpcyB0aGUgcGF5bG9hZCBvZiB0aHJlZQ=="}, {"cacheName":"cache1", "key":"key2", "value":"dGhpcyBpcyB0aGUgcGF5bG9hZCBvZiB0d28="}]}' -H "Content-Type: application/json"  -X POST http://localhost:9001/batch
```

Batch Cache (JSON) w/ failure due to lack of BASE64 encoding on payload
```shell
curl -i -d '{ "cacheRequests" : [{"cacheName":"cache1", "key":"key4", "value":"this is a fourth payload"}, {"cacheName":"cache1", "key":"key5", "value":"dGhpcyBpcyB0aGUgcGF5bG9hZCBvZiB0d28="}]}' -H "Content-Type: application/json" -X POST http://localhost:9001/batch
```

Batch Get Cached
```shell
curl -i -d '{"getCachedBatch" : [{"cacheName":"cache1", "key":"key3"}, {"cacheName":"cache1", "key":"key2"},  {"cacheName":"cache1", "key":"key1"}]}' -H "Content-Type: application/json" -X POST http://localhost:9001/batch/get
```

Batch Delete Cached
```shell
curl -i -d '{"getCachedBatch" : [{"cacheName":"cache1", "key":"key3"}, {"cacheName":"cache1", "key":"key2"}]}' -H "Content-Type: application/json" -X DELETE http://localhost:9001/batch
```
