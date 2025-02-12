---
# Display h2 to h5 headings
toc_min_heading_level: 2
toc_max_heading_level: 3
---

# REST APIs

## Access

Endpoint Authentication is based upon JSON Web Tokens (JWT) using HTTP Bearer Token authentication headers.
> **_NOTE:_**  When deploying the Akka Cache application to your Akka account the JWT is going to be different from the following example as it’ll be encrypted by your private key. For more information, please see [Securing your Cache Service](/getting-started/securing-cache-service).

The following claims (or fields) are required in your JWT:
```
{
"iss": "https://session.firebase.google.com/akka-cache",
"org": "ttorg",
"name": "John Doe",
"serviceLevel": "free"
}
```

In the REST curl examples used throughout the APIs, we'll be using the following Bearer Token which was generated on the [JWT.io](https://jwt.io/) website with the claims above.

```
-H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJodHRwczovL3Nlc3Npb24uZmlyZWJhc2UuZ29vZ2xlLmNvbS9ha2thLWNhY2hlIiwib3JnIjoidHRvcmciLCJuYW1lIjoiSm9obiBEb2UiLCJzZXJ2aWNlTGV2ZWwiOiJmcmVlIn0.rds8orVxVz149ovTxxYzFIqGmSdWJUlHONem9avKBgQ"
```
However, for brevity we’ll be abbreviating the JWT as the following:
```
-H "Authorization: Bearer <YOUR-JWT>"
```
We'll also be running our curl examples against a locally running version of the akka-cache application using the following command:
```
mvn clean compile exec:java
```
## Methods

[ Jump to [Models](#models) ]

## Table of Contents

### [CacheName](#cachename-section)

APIs used to maintain a cache name, namespace, or group.

- [`delete /cacheName/{cacheName}`](#cache-name-delete)
- [`put /cacheName/{cacheName}/flush`](#cache-name-flush)
- [`get /cacheName/{cacheName}`](#cache-name-get)
- [`get /cacheName/{cacheName}/keys`](#cache-name-get-keys)
- [`get /cacheName/list`](#cache-name-list)
- [`post /cacheName`](#cache-name-post)
- [`put /cacheName`](#cache-name-put)

### [Cache](#cache-section)

APIs used to create, retrieve, and delete cached objects.

- [`delete /{cacheName}/{key}`](#cache-delete)
- [`get /{cacheName}/{key}`](#cache-get-binary)
- [`post /{cacheName}/{key}`](#cache-create-binary)
- [`post /{cacheName}/{key}/{ttlSeconds}`](#cache-create-with-time-to-live-binary)
- [`get /get/{cacheName}/{key}`](#cache-get-json)
- [`post /set`](#cache-set-json)

### [Batch](#batch-section)

Batch APIs used to create, retrieve, and delete cached objects.

- [`delete /batch`](#batch-delete)
- [`post /batch/get`](#batch-get)
- [`post /batch`](#batch-create)

---

## CacheName {#cachename-section}

APIs used to maintain a cache name, namespace, or group.


### Cache Name Delete

```
delete /cacheName/{cacheName}
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

#### REST Curl Example
```
curl -i -H "Authorization: Bearer <YOUR-JWT>" -X DELETE http://localhost:9001/cacheName/cache1
```

#### REST Curl Result
```
HTTP/1.1 201 Created
Access-Control-Allow-Origin: *
Server: akka-http/10.7.0
Date: ...
Content-Length: 0
```
---


### Cache Name Flush

```
put /cacheName/{cacheName}/flush
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

#### REST Curl Example
```
curl -i -H "Authorization: Bearer <YOUR-JWT>" -H "Content-Type: application/json" -X PUT http://localhost:9001/cacheName/cache1/flush
```

#### REST Curl Result
```
HTTP/1.1 202 Accepted
Access-Control-Allow-Origin: *
Server: akka-http/10.7.0
Date: ...
Content-Length: 0
```
---


### Cache Name Get

```
get /cacheName/{cacheName}
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
#### REST Curl Example
```
curl -i -H "Authorization: Bearer <YOUR-JWT>" -H "Content-Type: application/json" http://localhost:9001/cacheName/cache1
```

#### REST Curl Result
```
HTTP/1.1 200 OK
Access-Control-Allow-Origin: *
Server: akka-http/10.7.0
Date: ...
Content-Type: application/json
Content-Length: 77

{"cacheName":"cache1","description":"This is our first test","deleted":false}
```

### Cache Name Get Keys

```
get /cacheName/{cacheName}/keys
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

#### REST Curl Example
```
curl -i -H "Authorization: Bearer <YOUR-JWT>" -H "Content-Type: application/json" http://localhost:9001/cacheName/cache1/keys
```

#### REST Curl Result
```
HTTP/1.1 200 OK
Access-Control-Allow-Origin: *
Server: akka-http/10.7.0
Date: ...
Content-Type: application/json
Content-Length: 47

{"cacheName":"cache1","keys":["key32","key31"]}
```
---


### Cache Name List

```
get /cacheName/list
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

#### REST Curl Example
```
TODO:
```

#### REST Curl Result
```
TODO:
```
---

### Cache Name Post

```
post /cacheName
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

#### REST Curl Example
```
curl -i -H "Authorization: Bearer <YOUR-JWT>" -d '{"cacheName":"cache1", "description":"This is our first test"}' -H "Content-Type: application/json" -X POST http://localhost:9001/cacheName
```

#### REST Curl Result
```
HTTP/1.1 201 Created
Access-Control-Allow-Origin: *
Server: akka-http/10.7.0
Date: ...
Content-Length: 0
```
---


### Cache Name Put

```
put /cacheName
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

#### REST Curl Example
```
curl -i -H "Authorization: Bearer <YOUR-JWT>" -d '{"cacheName":"cache1", "description":"This is our first test modified"}' -H "Content-Type: application/json" -X PUT http://localhost:9001/cacheName
```

#### REST Curl Result
```
HTTP/1.1 202 Accepted
Access-Control-Allow-Origin: *
Server: akka-http/10.7.0
Date: ...
Content-Length: 0

```
---

## Cache {#cache-section}

APIs used to create, retrieve, and delete cached objects.


### Cache Delete

```
delete /{cacheName}/{key}
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

#### REST Curl Example
```
curl -i -H "Authorization: Bearer <YOUR-JWT>" -X DELETE http://localhost:9001/cache1/key31
```

#### REST Curl Result
```
HTTP/1.1 202 Accepted
Access-Control-Allow-Origin: *
Server: akka-http/10.7.0
Date: ...
Content-Length: 0

```
---


### Cache Get (binary)

```
get /{cacheName}/{key}
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

#### REST Curl Example
```
curl -H "Authorization: Bearer <YOUR-JWT>" -i http://localhost:9001/cache1/key32
```

#### REST Curl Result
```
HTTP/1.1 200 OK
Access-Control-Allow-Origin: *
Server: akka-http/10.7.0
Date: ...
Content-Type: application/octet-stream
Content-Length: 9

payload32
```
---


### Cache Create (binary)

```
post /{cacheName}/{key}
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

#### REST Curl Example
```
curl -i -H "Authorization: Bearer <YOUR-JWT>" -d 'This is our second test' -H "Content-Type: application/octet-stream" -X POST http://localhost:9001/cache1/key2
```

#### REST Curl Result
```
HTTP/1.1 201 Created
Access-Control-Allow-Origin: *
Server: akka-http/10.7.0
Date: ...
Content-Length: 0

```
---


### Cache Create with Time to Live (binary)

```
post /{cacheName}/{key}/{ttlSeconds}
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

#### REST Curl Example
```
curl -i -H "Authorization: Bearer <YOUR-JWT>" -d 'This is our first test' -H "Content-Type: application/octet-stream"  -X POST http://localhost:9001/cache1/key1/30
```

#### REST Curl Result
```
HTTP/1.1 201 Created
Access-Control-Allow-Origin: *
Server: akka-http/10.7.0
Date: ...
Content-Length: 0

```
---


### Cache Get (JSON)

```
get /get/{cacheName}/{key}
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

#### REST Curl Example
```
curl -H "Authorization: Bearer <YOUR-JWT>" -i http://localhost:9001/get/cache1/key2
```

#### REST Curl Result
```
HTTP/1.1 200 OK
Access-Control-Allow-Origin: *
Server: akka-http/10.7.0
Date: Tue, 11 Feb 2025 23:46:26 GMT
Content-Type: application/json
Content-Length: 93

{"cacheName":"cache1","key":"key2","success":true,"value":"VGhpcyBpcyBvdXIgc2Vjb25kIHRlc3Q="}
```
---


### Cache Set (JSON)

```
post /set
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

#### REST Curl Example
```
curl -i -H "Authorization: Bearer <YOUR-JWT>" -d '{"cacheName":"cache1", "key":"key1", "value":"dGhpcyBpcyB0aGUgcGF5bG9hZCBvZiBvbmU="}' -H "Content-Type: application/json" -X POST http://localhost:9001/set
```

#### REST Curl Result
```
HTTP/1.1 201 Created
Access-Control-Allow-Origin: *
Server: akka-http/10.7.0
Date: ...
Content-Length: 0

```
---
## Batch {#batch-section}

Batch APIs used to create, retrieve, and delete cached objects.


### Batch Delete

```
delete /batch
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

#### REST Curl Example
```
curl -i -H "Authorization: Bearer <YOUR-JWT>" -d '{"getCachedBatch" : [{"cacheName":"cache1", "key":"key3"}, {"cacheName":"cache1", "key":"key2"}]}' -H "Content-Type: application/json" -X DELETE http://localhost:9001/batch
```

#### REST Curl Result
```
HTTP/1.1 200 OK
Access-Control-Allow-Origin: *
Server: akka-http/10.7.0
Date: ...
Content-Type: application/json
Content-Length: 153

{"success":true,"cacheDeleteResponses":[{"cacheName":"ttorgcache1","key":"key3","success":true},{"cacheName":"ttorgcache1","key":"key2","success":true}]}
```
---


### Batch Get

```
post /batch/get
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

#### REST Curl Example
```
curl -i -curl -i -H "Authorization: Bearer <YOUR-JWT>" -d '{"getCachedBatch" : [{"cacheName":"cache1", "key":"key3"}, {"cacheName":"cache1", "key":"key2"},  {"cacheName":"cache1", "key":"key1"}]}' -H "Content-Type: application/json" -X POST http://localhost:9001/batch/get -d '{"getCachedBatch" : [{"cacheName":"cache1", "key":"key3"}, {"cacheName":"cache1", "key":"key2"},  {"cacheName":"cache1", "key":"key1"}]}' -H "Content-Type: application/json" -X POST http://localhost:9001/batch/get
```

#### REST Curl Result
```
HTTP/1.1 200 OK
Access-Control-Allow-Origin: *
Server: akka-http/10.7.0
Date: Wed, 12 Feb 2025 00:02:46 GMT
Content-Type: application/json
Content-Length: 327

{"complete":true,"results":[{"cacheName":"cache1","key":"key3","success":true,"value":"dGhpcyBpcyB0aGUgcGF5bG9hZCBvZiB0aHJlZQ=="},{"cacheName":"cache1","key":"key2","success":true,"value":"dGhpcyBpcyB0aGUgcGF5bG9hZCBvZiB0d28="},{"cacheName":"cache1","key":"key1","success":true,"value":"dGhpcyBpcyB0aGUgcGF5bG9hZCBvZiBvbmU="}]}
```
---


### Batch Create

```
post /batch
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

#### REST Curl Example
```
curl -i -H "Authorization: Bearer <YOUR-JWT>" -d '{ "cacheRequests" : [{"cacheName":"cache1", "key":"key1", "value":"dGhpcyBpcyB0aGUgcGF5bG9hZCBvZiBvbmU="}, {"cacheName":"cache1", "key":"key2", "value":"dGhpcyBpcyB0aGUgcGF5bG9hZCBvZiB0d28="}, {"cacheName":"cache1", "key":"key3", "value":"dGhpcyBpcyB0aGUgcGF5bG9hZCBvZiB0aHJlZQ=="}, {"cacheName":"cache1", "key":"key2", "value":"dGhpcyBpcyB0aGUgcGF5bG9hZCBvZiB0d28="}]}' -H "Content-Type: application/json" -X POST http://localhost:9001/batch
```

#### REST Curl Result
```
HTTP/1.1 200 OK
Access-Control-Allow-Origin: *
Server: akka-http/10.7.0
Date: Tue, 11 Feb 2025 23:59:46 GMT
Content-Type: application/json
Content-Length: 30

{"complete":true,"results":[]}
```
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
