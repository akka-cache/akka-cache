# akka-cache


To understand the Akka concepts that are the basis for this example, see [Development Process](https://doc.akka.io/concepts/development-process.html) in the documentation.


This project contains the skeleton to create an Akka service. To understand more about these components, see [Developing services](https://doc.akka.io/java/index.html). Examples can be found [here](https://doc.akka.io/samples/index.html).


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
curl http://localhost:9000/...
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

Curl Examples

Cache Name:

POST /cache/cacheName/create
```
curl -i -d '{"cacheName":"cache1", "description":"This is our first test"}' -H "Content-Type: application/json" -X POST http://localhost:9000/cache/cacheName/create
```

GET /cache/cacheName/{cacheName}
```
curl -i http://localhost:9000/cache/cacheName/cache1
```

POST /cache/cacheName/update
```
curl -i -d '{"cacheName":"cache1", "description":"This is our first modification"}' -H "Content-Type: application/json" -X POST http://localhost:9000/cache/cacheName/update
```

GET /cache/cacheName/keys/{cacheName}

> **_NOTE:_** execute the cache commands below before executing this
```
curl -i http://localhost:9000/cache/cacheName/keys/cache1
```

Cache:

POST /cache (this is the payload of one)
```
curl -i -d '{"cacheName":"cache1", "key":"key1", "value":"dGhpcyBpcyB0aGUgcGF5bG9hZCBvZiBvbmU="}' -H "Content-Type: application/json" -X POST http://localhost:9000/cache
```
(this is the payload of two)
```
curl -i -d '{"cacheName":"cache1", "key":"key2", "value":"dGhpcyBpcyB0aGUgcGF5bG9hZCBvZiB0d28="}' -H "Content-Type: application/json" -X POST http://localhost:9000/cache
```
(this is the payload of three)
```
curl -i -d '{"cacheName":"cache1", "key":"key3", "value":"dGhpcyBpcyB0aGUgcGF5bG9hZCBvZiB0aHJlZQ=="}' -H "Content-Type: application/json" -X POST http://localhost:9000/cache
```

(this is the payload of four, with ttl 35 seconds)
```
curl -i -d '{"cacheName":"cache1", "key":"key4", "value":"dGhpcyBpcyB0aGUgcGF5bG9hZCBvZiBmb3VyLCB3aXRoIHR0bCAzNSBzZWNvbmRzCg==","ttlSeconds":35}' -H "Content-Type: application/json" -X POST http://localhost:9000/cache 
```

@Get("/{cacheName}/{key}")
```
curl -i http://localhost:9000/cache/cache1/key3
```
```
curl -i http://localhost:9000/cache/cache1/key4
```