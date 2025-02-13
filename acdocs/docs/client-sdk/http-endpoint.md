---
sidebar_position: 1
# Display h2 to h5 headings
toc_min_heading_level: 2
toc_max_heading_level: 3
title: HTTP Endpoint
description: Accessing the cache's HTTP endpoint
---


# HTTP Endpoint

## Access
The cache's HTTP endpoint is secured with an **ACL** that requires all clients to be a service within the same project. This means the HTTP endpoint shouldn't be exposed to the public internet and should be used via an _internal_ HTTP reference using the Akka SDK's [HttpClient](https://doc.akka.io/java/_attachments/api/akka/javasdk/http/HttpClient.html)

## Obtaining an HTTP Client
To get an HTTP client that will connect to your service, you'll use the `HttpClientProvider` class. By providing this class with the name of the service, Akka will seamlessly supply the most appropriate URL for that service, no matter what region(s) the client or server are in.

Using the standard constructor injection, your component can get an instance of the HTTP client provider:

```java
public MyComponent(HttpClientProvider webClientProvider, 
                   ComponentClient componentClient) {
  this.httpClient = webClientProvider.httpClientFor("akka-cache"); 
  this.componentClient = componentClient;
}
```

Once you have a web client, you can then use it to invoke the various methods on the cache:

```java
httpClient.POST("/cache/cacheName") 
          .withRequestBody(cacheNameRequest)
          .invokeAsync() 
          .thenApply(response -> { 
            ...
           });
```

Note that if you've renamed the Akka Cache service to something other than `akka-cache`, you'll need to use that alternate name when obtaining a web client.

# Methods

[ Jump to [Models](#models) ]

## Table of Contents

### [CacheName](#cachename-section)

APIs used to maintain a cache name, namespace, or group.

- [`delete /cache/cacheName/{cacheName}`](#cache-name-delete)
- [`put /cache/cacheName/{cacheName}/flush`](#cache-name-flush)
- [`get /cache/cacheName/{cacheName}`](#cache-name-get)
- [`get /cache/cacheName/{cacheName}/keys`](#cache-name-get-keys)
- [`get /cache/cacheName/list`](#cache-name-list)
- [`post /cache/cacheName`](#cache-name-post)
- [`put /cache/cacheName`](#cache-name-put)

### [Cache](#cache-section)

APIs used to create, retrieve, and delete cached objects.

- [`delete /cache/{cacheName}/{key}`](#cache-delete)
- [`get /cache/{cacheName}/{key}`](#cache-get-binary)
- [`post /cache/{cacheName}/{key}`](#cache-create-binary)
- [`post /cache/{cacheName}/{key}/{ttlSeconds}`](#cache-create-with-time-to-live-binary)
- [`get /cache/get/{cacheName}/{key}`](#cache-get-json)
- [`post /cache/set`](#cache-set-json)

### [Batch](#batch-section)

Batch APIs used to create, retrieve, and delete cached objects.

- [`delete /cache/batch`](#batch-delete)
- [`post /cache/batch/get`](#batch-get)
- [`post /cache/batch`](#batch-create)

---

## CacheName {#cachename-section}

APIs used to maintain a cache name, namespace, or group.


### Cache Name Delete

```
delete /cache/cacheName/{cacheName}
```

delete the current cache namespace and all associated caches.

#### Path parameters

- **cacheName (required)**\
Path Parameter — Name of a cache namespace. (group) default: null

#### Responses

###### 202

(accepted) deleted

##### 400

Bad Request


### Cache Name Flush

```
put /cache/cacheName/{cacheName}/flush
```

Deletes all existing cached objects but leaves the cache namespace in place.

#### Path parameters

- **cacheName (required)**\
Path Parameter — Name of a cache namespace. (group) default: null

#### Responses

###### 202

accepted

##### 400

bad request or doesn't exist

---


### Cache Name Get

```
get /cache/cacheName/{cacheName}
```

Get the specified cache name description, and delete status. (cacheNameCacheNameGet)

#### Path parameters

- **cacheName (required)**\
Path Parameter — Name of a cache namespace. (group) default: null

#### Return type

[cacheNameResponse](#cachenameresponse)

#### Produces

This API call produces the following media types according to the Accept request header;
the media type will be conveyed by the Content-Type response header.

- `application/json`

#### Responses

##### 200

returned a JSON format of the cache namespace, and description
[cacheNameResponse](#cachenameresponse)

##### 400

bad request or doesn't exist

---
### Cache Name Get Keys

```
get /cache/cacheName/{cacheName}/keys
```

Get the current cache namespace's list of cache keys.

#### Path parameters

- **cacheName (required)**\
Path Parameter — Name of a cache namespace. (group) default: null

#### Return type

array[String]

#### Produces

This API call produces the following media types according to the Accept request header;
the media type will be conveyed by the Content-Type response header.

- `application/json`

#### Responses

##### 200

returns a JSON format array of cache keys

##### 400

bad request or doesn't exist

---


### Cache Name List

```
get /cache/cacheName/list
```

Retrieves a list of cacheNames and optional description.

#### Return type

array[[cacheNameListResponse_inner](#cachenamelistresponse_inner)]

#### Produces

This API call produces the following media types according to the Accept request header;
the media type will be conveyed by the Content-Type response header.

- `application/json`

#### Responses

##### 200

Returns a JSON format array of the cache namespaces, and description.

##### 400

Bad Request

---

### Cache Name Post

```
post /cache/cacheName
```

Create or update a cache namespace description

#### Consumes

This API call consumes the following media types via the Content-Type request header:

- `application/json`

#### Request body

cacheNameCreate [cacheNameCreate](#cachenamecreate) (required)\
Body Parameter —

#### Responses

##### 201

(created) Successfully created or updated.

##### 400

Bad Request

---


### Cache Name Put

```
put /cache/cacheName
```

Update an existing cache namespace's description.

#### Consumes

This API call consumes the following media types via the Content-Type request header:

- `application/json`

#### Request body

cacheNameCreate [cacheNameCreate](#cachenamecreate) (required)\
Body Parameter —

#### Responses

###### 202

(accepted) Successfully created or updated.

##### 400

Bad Request

---

## Cache {#cache-section}

APIs used to create, retrieve, and delete cached objects.


### Cache Delete

```
delete /cache/{cacheName}/{key}
```

Deletes the currently cached object by key within the given cacheName.

#### Path parameters

- **cacheName (required)**\
Path Parameter — Name of a cache namespace. (group) default: null key (required)
Path Parameter — The key of a specific cache object within a cacheName. default: null

#### Responses

###### 202

(accepted) deleted

##### 400

Bad Request

---

### Cache Get (binary)

```
get /cache/{cacheName}/{key}
```

Retrieves the currently cached object by key within the given cacheName. Note: this is a binary version of the API and requires that the cache object be passed in the request body.

#### Path parameters

- **cacheName (required)**\
Path Parameter — Name of a cache namespace. (group) default: null key (required)
Path Parameter — The key of a specific cache object within a cacheName. default: null

#### Return type

File

#### Produces

This API call produces the following media types according to the Accept request header;
the media type will be conveyed by the Content-Type response header.

- `application/octet-stream`

#### Responses

##### 200

Okay
[File](#File)

##### 404

Not Found

##### 400

Bad Request

---

### Cache Create (binary)

```
post /cache/{cacheName}/{key}
```

Create or update a cached object under the cacheName. Note: this is a binary version of the API and requires that the cache object be passed in the request body.

#### Path parameters

- **cacheName (required)**\
Path Parameter — Name of a cache namespace. (group) default: null key (required)
Path Parameter — The key of a specific cache object within a cacheName. default: null

#### Consumes

This API call consumes the following media types via the Content-Type request header:

- `application/octet-stream`

#### Request body

body [file](#file) (required)\
Body Parameter —

#### Responses

##### 201

(created) Successfully created or updated.

##### 400

bad request or potentially exceeded maximum cached bytes allowed

---

### Cache Create with Time to Live (binary)

```
post /cache/{cacheName}/{key}/{ttlSeconds}
```

Create or update a cached object by key within the given cacheName. Note: this is a binary version of the API and requires that the cache object be passed in the request body.

#### Path parameters

- **cacheName (required)**\
Path Parameter — Name of a cache namespace. (group) default: null key (required)
Path Parameter — The key of a specific cache object within a cacheName. default: null ttlSeconds (required)
Path Parameter — Time to Live (TTL), in seconds, of the cached object before being automatically deleted. default: null

#### Consumes

This API call consumes the following media types via the Content-Type request header:

- `application/octet-stream`

#### Request body

body [file](#file) (required)\
Body Parameter —

#### Responses

##### 201

(created) Successfully created or updated.

##### 400

Bad Request or potentially exceeded maximum cached bytes allowed

---

### Cache Get (JSON)

```
get /cache/get/{cacheName}/{key}
```

Get the current cache namespace description, and delete status.

#### Path parameters

- **cacheName (required)**\
Path Parameter — Name of a cache namespace. (group) default: null key (required)
Path Parameter — The key of a specific cache object within a cacheName. default: null

#### Return type

[cacheGetResponse](#cachegetresponse)

#### Produces

This API call produces the following media types according to the Accept request header;
the media type will be conveyed by the Content-Type response header.

- `application/json`

#### Responses

##### 200

Returns a JSON format of the cache namespace, and description.\
[cacheGetResponse](#cachegetresponse)

##### 400

Bad Request

---

### Cache Set (JSON)

```
post /cache/set
```

Create or update a cached object (BASE64 encoded) by key within the given cacheName.

#### Consumes

This API call consumes the following media types via the Content-Type request header:

- `application/json`

#### Request body

cacheRequest [cacheRequest](#cacherequest) (required)\
Body Parameter —

#### Responses

##### 201

(created) Successfully created or updated.

##### 400

Bad Request or, Invalid cacheRequest, or potentially exceeded maximum cached bytes allowed.

---
## Batch {#batch-section}

Batch APIs used to create, retrieve, and delete cached objects.


### Batch Delete

```
delete /cache/batch
```

Deletes a batch of individually cached objects.

#### Consumes

This API call consumes the following media types via the Content-Type request header:

- `application/json`

#### Request body

batchGetCacheRequests_inner [batchGetCacheRequests_inner](#batchgetcacherequests_inner) (required)\
Body Parameter —

#### Return type

array[[batchDeleteCacheResponse_inner](#batchdeletecacheresponse_inner)]

#### Produces

This API call produces the following media types according to the Accept request header;
the media type will be conveyed by the Content-Type response header.

- `application/json`

#### Responses

##### 200

Returned a JSON formated object of all cached results.

##### 400

Bad Request

---

### Batch Get

```
post /cache/batch/get
```

Retrieve a batch of cached objects.

#### Consumes

This API call consumes the following media types via the Content-Type request header:

- `application/json`

#### Request body

batchGetCacheRequests_inner [batchGetCacheRequests_inner](#batchgetcacherequests_inner) (required)\
Body Parameter —

#### Return type

[batchGetCacheResponse](#batchgetcacheresponse)

#### Produces

This API call produces the following media types according to the Accept request header;
the media type will be conveyed by the Content-Type response header.

- `application/json`

#### Responses

##### 200

Returns a JSON formated object of all cached results.\
[batchGetCacheResponse](#batchgetcacheresponse)

##### 400

Bad Request

---

### Batch Create

```
post /cache/batch
```

Create batch of cached objects, results are returned for each batched element.

#### Consumes

This API call consumes the following media types via the Content-Type request header:

- `application/json`

#### Request body

batchCacheRequest_inner [batchCacheRequest_inner](#batchcacherequest_inner) (required)\
Body Parameter —

#### Return type

[batchCacheResponse](#batchcacheresponse)

#### Produces

This API call produces the following media types according to the Accept request header;
the media type will be conveyed by the Content-Type response header.

- `application/json`

#### Responses

##### 200

returns a JSON formated object of each cached result.\
[batchCacheResponse](#batchcacheresponse)

##### 400

Bad Request

---

## Models

[ Jump to [Methods](#methods) ]

### Table of Contents

- [`batchCacheRequest_inner`](#batchcacherequest_inner)
- [`batchCacheResponse`](#batchcacheresponse)
- [`batchCacheResponse_results_inner`](#batchcacheresponse_results_inner)
- [`batchCacheResult`](#batchcacheresult)
- [`batchDeleteCacheResponse_inner`](#batchdeletecacheresponse_inner)
- [`batchGetCacheRequest`](#batchgetcacherequest)
- [`batchGetCacheRequests_inner`](#batchgetcacherequests_inner)
- [`batchGetCacheResponse`](#batchgetcacheresponse)
- [`batchGetCacheResponse_results_inner`](#batchgetcacheresponse_results_inner)
- [`cacheDeleteResponse`](#cachedeleteresponse)
- [`cacheGetResponse`](#cachegetresponse)
- [`cacheNameCreate`](#cachenamecreate)
- [`cacheNameListResponse_inner`](#cachenamelistresponse_inner)
- [`cacheNameResponse`](#cachenameresponse)
- [`cacheRequest`](#cacherequest)


### batchCacheRequest_inner

A JSON array of objects to be cached where the object must be a BASE64 encoded value.

**cacheName**\
`String`

**key**\
`String`

**ttlSeconds** (optional)\
`Integer`

**value**\
`byte[]` format: byte

---
### batchCacheResponse

A JSON wrapped array of batched cache results.

**complete** (optional)\
`Boolean`

**results** (optional)\
`array[batchCacheResponse_results_inner]`

---
### batchCacheResponse_results_inner

A JSON array of batched cache results.

**cacheName**\
`String`

**key**\
`String`

**success**\
`Boolean`

---
### batchCacheResult

A JSON based array of results for each batched cache request.

**cacheName**\
`String`

**key**\
`String`

**success**\
`Boolean`

---
### batchDeleteCacheResponse_inner

**cacheName**\
`String`

**key**\
`String`

**success**\
`Boolean`

---
### batchGetCacheRequest

A cashName/key value pair of a cached object to retrieve.

**cacheName**\
`String`

**key**\
`String`

---
### batchGetCacheRequests_inner

A JSON array of retrieved cashName/key pairs to retrieve as a batch.

**cacheName**\
`String`

**key**\
`String`

---
### batchGetCacheResponse

**complete** (optional)\
`Boolean`

**results** (optional)\
`array[batchGetCacheResponse_results_inner]` A JSON response containing the list of cached objects retrieved. Note: each object is encoded as a BASE64 encoded value.

---
### batchGetCacheResponse_results_inner

**cacheName**\
`String`

**key**\
`String`

**success**\
`Boolean`

**value**\
`byte[]` format: byte

---
### cacheDeleteResponse

A JSON response of the cache delete request.

**cacheName**\
`String`

**key**\
`String`

**success**\
`Boolean`

---
### cacheGetResponse

The cached JSON object returned as a BASE64 encoded value.

**cacheName**\
`String`

**key**\
`String`

**success**\
`Boolean`

**value**\
`byte[]` format: byte

---
### cacheNameCreate

A JSON object use to create a cacheName namespace.

**cacheName**\
`String`

**description** (optional)\
`String`

---
### cacheNameListResponse_inner

An array of all known cacheNames, along with an optional description.

**cacheName**\
`String`

**description** (optional)\
`String`

---
### cacheNameResponse

The complete cacheName object.

**cacheName**\
`String`

**description**\
`String`

**deleted**\
`Boolean`

---
### cacheRequest

A JSON request object where the object is provided as a BASE64 encoded value.

**cacheName**\
`String`

**key**\
`String`

**ttlSeconds** (optional)\
`Integer`

**value**\
`byte[]` format: byte
