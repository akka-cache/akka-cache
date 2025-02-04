# REST APIs

## Access

Endpoint Authentication is based upon JSON Web Tokens (JWT) using HTTP Bearer Token authentication headers.

The following claims (or fields) are required:
```
{
"iss": "https://session.firebase.google.com/akka-cache",
"org": "ttorg",
"name": "John Doe",
"serviceLevel": "free"
}
```

## Methods

[ Jump to [Models](#models) ]

## Table of Contents

### [CacheName APIs](#cachnamedtl)
APIs used to maintain a cache name, namespace, or group.

* [`delete /cacheName/{cacheName}`](#cacheNameCacheNameDelete)
* [`put /cacheName/{cacheName}/flush`](#cacheNameCacheNameFlushPut)
* [`get /cacheName/{cacheName}`](#cacheNameCacheNameGet)
* [`get /cacheName/{cacheName}/keys`](#cacheNameCacheNameKeysGet)
* [`get /cacheName/list`](#cacheNameListGet)
* [`post /cacheName`](#cacheNamePost)
* [`put /cacheName`](#cacheNamePut)

### [Cache APIs](#cachedtl)
APIs used to create, retrieve, and delete cached objects.

* [`delete /{cacheName}/{key}`](#cacheNameKeyDelete)
* [`get /{cacheName}/{key}`](#cacheNameKeyGet)
* [`post /{cacheName}/{key}`](#cacheNameKeyPost)
* [`post /{cacheName}/{key}/{ttlSeconds}`](#cacheNameKeyTtlSecondsPost)
* [`get /get/{cacheName}/{key}`](#getCacheNameKeyGet)
* [`post /set`](#setPost)

### [Batch APIs](#batchdtl)
Batch APIs used to create, retrieve, and delete cached objects.

* [`delete /batch`](#batchDelete)
* [`post /batch/get`](#batchGetPost)
* [`post /batch`](#batchPost)

---

# CacheName {#cachenamedtl}
APIs used to maintain a cache name, namespace, or group.

[Up](#methods)

## Cache Name Delete {#cacheNameCacheNameDelete}
```
delete /cacheName/{cacheName}
```

delete the current cache namespace and all associated caches.
### Path parameters

cacheName (required)
Path Parameter — Name of a cache namespace. (group) default: null

### Responses

#### 202

(accepted) deleted
#### 400

Bad Request

---

[Up](#methods)
## Cache Name Flush {#cacheNameCacheNameFlushPut}

```
put /cacheName/{cacheName}/flush
```

Deletes all existing cached objects but leaves the cache namespace in place.
### Path parameters

cacheName (required)
Path Parameter — Name of a cache namespace. (group) default: null

### Responses

#### 202

accepted
#### 400

bad request or doesn't exist

---

[Up](#methods)
## Cache Name Get {#cacheNameCacheNameGet}

```
get /cacheName/{cacheName}
```

Get the specified cache name description, and delete status. (cacheNameCacheNameGet)
### Path parameters

cacheName (required)
Path Parameter — Name of a cache namespace. (group) default: null

### Return type

[cacheNameResponse](#cacheNameResponse)

### Example data

Content-Type: application/json
```
{
  "cacheName" : "cacheName",
  "deleted" : true,
  "description" : "description"
}
```
### Produces

This API call produces the following media types according to the Accept request header;
the media type will be conveyed by the Content-Type response header.

* `application/json`

### Responses

#### 200

returned a JSON format of the cache namespace, and description
[cacheNameResponse](#cacheNameResponse)
#### 400

bad request or doesn't exist

---

[Up](#methods)
## Cache Name Get Keys {#cacheNameCacheNameKeysGet}
```
get /cacheName/{cacheName}/keys
```

Get the current cache namespace's list of cache keys.
### Path parameters

cacheName (required)
Path Parameter — Name of a cache namespace. (group) default: null

### Return type

array[String]

### Example data

Content-Type: application/json
```
{"cacheName":"cache1", "description":"This is our first test"}
```
### Produces

This API call produces the following media types according to the Accept request header;
the media type will be conveyed by the Content-Type response header.

* `application/json`

### Responses

#### 200

returns a JSON format array of cache keys
#### 400

bad request or doesn't exist

---

[Up](#methods)
## Cache Name List {#cacheNameListGet}

```
get /cacheName/list
```

Retrieves a list of cacheNames and optional description.
### Return type

array[[cacheNameListResponse\_inner](#cacheNameListResponse_inner)]

### Example data

Content-Type: application/json
```
[ {
  "cacheName" : "cacheName",
  "description" : "description"
}, {
  "cacheName" : "cacheName",
  "description" : "description"
} ]
```
### Produces

This API call produces the following media types according to the Accept request header;
the media type will be conveyed by the Content-Type response header.

* `application/json`

### Responses

#### 200

Returns a JSON format array of the cache namespaces, and description.
#### 400

Bad Request

---

[Up](#methods)
## Cache Name Post {#cacheNamePost}
```
post /cacheName
```

Create or update a cache namespace description 
### Consumes

This API call consumes the following media types via the Content-Type request header:

* `application/json`

### Request body

cacheNameCreate [cacheNameCreate](#cacheNameCreate) (required)
Body Parameter —

### Responses

#### 201

(created) Successfully created or updated.
#### 400

Bad Request

---

[Up](#methods)
## Cache Name Put {#cacheNamePut}
```
put /cacheName
```

Create or update a cache namespace description.
### Consumes

This API call consumes the following media types via the Content-Type request header:

* `application/json`

### Request body

cacheNameCreate [cacheNameCreate](#cacheNameCreate) (required)
Body Parameter —

### Responses

#### 202

(accepted) Successfully created or updated.
#### 400

Bad Request

---


# Cache {#cachedtl}
APIs used to create, retrieve, and delete cached objects.

[Up](#methods)

## Cache Delete {#cacheNameKeyDelete}
```
delete /{cacheName}/{key}
```

Deletes the currently cached object by key within the given cacheName.
### Path parameters

cacheName (required)
Path Parameter — Name of a cache namespace. (group) default: null key (required)
Path Parameter — The key of a specific cache object within a cacheName. default: null

### Responses

#### 202

(accepted) deleted
#### 400

Bad Request

---

[Up](#methods)
## Cache Get (binary) {#cacheNameKeyGet}
```
get /{cacheName}/{key}
```

Retrieves the currently cached object by key within the given cacheName. Note: this is a binary version of the API and requires that the cache object be passed in the request body.
### Path parameters

cacheName (required)
Path Parameter — Name of a cache namespace. (group) default: null key (required)
Path Parameter — The key of a specific cache object within a cacheName. default: null

### Return type

File

### Produces

This API call produces the following media types according to the Accept request header;
the media type will be conveyed by the Content-Type response header.

* `application/octet-stream`

### Responses

#### 200

Okay
[File](#File)
#### 404

Not Found
#### 400

Bad Request

---

[Up](#methods)
## Cache Create (binary) {#cacheNameKeyPost}
```
post /{cacheName}/{key}
```

Create or update a cached object under the cacheName. Note: this is a binary version of the API and requires that the cache object be passed in the request body.
### Path parameters

cacheName (required)
Path Parameter — Name of a cache namespace. (group) default: null key (required)
Path Parameter — The key of a specific cache object within a cacheName. default: null

### Consumes

This API call consumes the following media types via the Content-Type request header:

* `application/octet-stream`

### Request body

body [file](#file) (required)
Body Parameter —

### Responses

#### 201

(created) Successfully created or updated.
#### 400

bad request or potentially exceeded maximum cached bytes allowed

---

[Up](#methods)
## Cache Create with Time to Live (binary) {#cacheNameKeyTtlSecondsPost}
```
post /{cacheName}/{key}/{ttlSeconds}
```

Create or update a cached object by key within the given cacheName. Note: this is a binary version of the API and requires that the cache object be passed in the request body.
### Path parameters

cacheName (required)
Path Parameter — Name of a cache namespace. (group) default: null key (required)
Path Parameter — The key of a specific cache object within a cacheName. default: null ttlSeconds (required)
Path Parameter — Time to Live (TTL) of the cached object before being automatically deleted. default: null

### Consumes

This API call consumes the following media types via the Content-Type request header:

* `application/octet-stream`

### Request body

body [file](#file) (required)
Body Parameter —

### Responses

#### 201

(created) Successfully created or updated.
#### 400

Bad Request or potentially exceeded maximum cached bytes allowed

---

[Up](#methods)
## Cache Get (JSON) {#getCacheNameKeyGet}
```
get /get/{cacheName}/{key}
```

Get the current cache namespace description, and delete status.
### Path parameters

cacheName (required)
Path Parameter — Name of a cache namespace. (group) default: null key (required)
Path Parameter — The key of a specific cache object within a cacheName. default: null

### Return type

[cacheGetResponse](#cacheGetResponse)

### Example data

Content-Type: application/json
```
{
  "cacheName" : "cacheName",
  "success" : true,
  "value" : "value",
  "key" : "key"
}
```
### Produces

This API call produces the following media types according to the Accept request header;
the media type will be conveyed by the Content-Type response header.

* `application/json`

### Responses

#### 200

Returns a JSON format of the cache namespace, and description.
[cacheGetResponse](#cacheGetResponse)
#### 400

Bad Request

---

[Up](#methods)
## Cache Set (JSON) {#setPost}
```
post /set
```

Create or update a cached object (BASE64 encoded) by key within the given cacheName.
### Consumes

This API call consumes the following media types via the Content-Type request header:

* `application/json`

### Request body

cacheRequest [cacheRequest](#cacheRequest) (required)
Body Parameter —

### Responses

#### 201

(created) Successfully created or updated.
#### 400

Bad Request or, Invalid cacheRequest, or potentially exceeded maximum cached bytes allowed.

# Batch {#batchdtl}
Batch APIs used to create, retrieve, and delete cached objects.

[Up](#methods)
## Batch Delete {#batchDelete}
```
delete /batch
```

Deletes a batch of individually cached objects.
### Consumes

This API call consumes the following media types via the Content-Type request header:

* `application/json`

### Request body

batchGetCacheRequests\_inner [batchGetCacheRequests\_inner](#batchGetCacheRequests_inner) (required)
Body Parameter —

### Return type

array[[batchDeleteCacheResponse\_inner](#batchDeleteCacheResponse_inner)]

### Example data

Content-Type: application/json
```
[ {
  "cacheName" : "cacheName",
  "success" : true,
  "key" : "key"
}, {
  "cacheName" : "cacheName",
  "success" : true,
  "key" : "key"
} ]
```
### Produces

This API call produces the following media types according to the Accept request header;
the media type will be conveyed by the Content-Type response header.

* `application/json`

### Responses

#### 200

Returned a JSON formated object of all cached results.
#### 400

Bad Request

---

[Up](#methods)
## Batch Get {#batchGetPost}
```
post /batch/get
```

Retrieve a batch of cached objects.
### Consumes

This API call consumes the following media types via the Content-Type request header:

* `application/json`

### Request body

batchGetCacheRequests\_inner [batchGetCacheRequests\_inner](#batchGetCacheRequests_inner) (required)
Body Parameter —

### Return type

[batchGetCacheResponse](#batchGetCacheResponse)

### Example data

Content-Type: application/json
```
{
  "complete" : true,
  "results" : [ {
    "cacheName" : "cacheName",
    "success" : true,
    "value" : "value",
    "key" : "key"
  }, {
    "cacheName" : "cacheName",
    "success" : true,
    "value" : "value",
    "key" : "key"
  } ]
}
```
### Produces

This API call produces the following media types according to the Accept request header;
the media type will be conveyed by the Content-Type response header.

* `application/json`

### Responses

#### 200

Returns a JSON formated object of all cached results.
[batchGetCacheResponse](#batchGetCacheResponse)
#### 400

Bad Request

---

[Up](#methods)
## Batch Create {#batchPost}
```
post /batch
```

Create batch of cached objects, results are returned for each batched element.
### Consumes

This API call consumes the following media types via the Content-Type request header:

* `application/json`

### Request body

batchCacheRequest\_inner [batchCacheRequest\_inner](#batchCacheRequest_inner) (required)
Body Parameter —

### Return type

[batchCacheResponse](#batchCacheResponse)

### Example data

Content-Type: application/json
```
{
  "complete" : true,
  "results" : [ {
    "cacheName" : "cacheName",
    "success" : true,
    "key" : "key"
  }, {
    "cacheName" : "cacheName",
    "success" : true,
    "key" : "key"
  } ]
}
```
### Produces

This API call produces the following media types according to the Accept request header;
the media type will be conveyed by the Content-Type response header.

* `application/json`

### Responses

#### 200

returns a JSON formated object of each cached result.
[batchCacheResponse](#batchCacheResponse)
#### 400

Bad Request

---

## Models

[ Jump to [Methods](#methods) ]
### Table of Contents

* [batchCacheRequest_inner](#batchCacheRequest_inner)
* [batchCacheResponse](#batchCacheResponse)
* [batchCacheResponse_results_inner](#batchCacheResponse_results_inner)
* [batchCacheResult](#batchCacheResult)
* [batchDeleteCacheResponse_inner](#batchDeleteCacheResponse_inner)
* [batchGetCacheRequest](#batchGetCacheRequest)
* [batchGetCacheRequests_inner](#batchGetCacheRequests_inner)
* [batchGetCacheResponse](#batchGetCacheResponse)
* [batchGetCacheResponse_results_inner](#batchGetCacheResponse_results_inner)
* [cacheDeleteResponse](#cacheDeleteResponse)
* [cacheGetResponse](#cacheGetResponse)
* [cacheNameCreate](#cacheNameCreate)
* [cacheNameListResponse_inner](#cacheNameListResponse_inner)
* [cacheNameResponse](#cacheNameResponse)
* [cacheRequest](#cacheRequest)

[Up](#models)
### batchCacheRequest_inner 

A JSON array of objects to be cached where the object must be a BASE64 encoded value.
cacheName [String](#string)
key [String](#string)
ttlSeconds (optional)[Integer](#integer)
value [byte[]](#ByteArray) format: byte

[Up](#models)
### batchCacheResponse

A JSON wrapped array of batched cache results.

complete (optional)[Boolean](#boolean)
results (optional)[array[batchCacheResponse\_results\_inner]](#batchCacheResponse_results_inner)

[Up](#models)
### batchCacheResponse_results_inner

A JSON array of batched cache results.
cacheName [String](#string)
key [String](#string)
success [Boolean](#boolean)

[Up](#models)
### batchCacheResult

A JSON based array of results for each batched cache request.
cacheName [String](#string)
key [String](#string)
success [Boolean](#boolean)

[Up](#models)
### batchDeleteCacheResponse_inner 

cacheName [String](#string)
key [String](#string)
success [Boolean](#boolean)

[Up](#models)
### batchGetCacheRequest 

A cashName/key value pair of a cached object to retrieve.
cacheName [String](#string)
key [String](#string)

[Up](#models)
### batchGetCacheRequests_inner

A JSON array of retrieved cashName/key pairs to retrieve as a batch.
cacheName [String](#string)
key [String](#string)

[Up](#models)
### batchGetCacheResponse

complete (optional)[Boolean](#boolean)
results (optional)[array[batchGetCacheResponse\_results\_inner]](#batchGetCacheResponse_results_inner) A JSON response containing the list of cached objects retrieved. Note: each object is encoded as a BASE64 encoded value.

[Up](#models)
### batchGetCacheResponse_results_inner

cacheName [String](#string)
key [String](#string)
success [Boolean](#boolean)
value [byte[]](#ByteArray) format: byte

[Up](#models)
### cacheDeleteResponse

A JSON response of the cache delete request.
cacheName [String](#string)
key [String](#string)
success [Boolean](#boolean)

[Up](#models)
### cacheGetResponse

The cached JSON object returned as a BASE64 encoded value.
cacheName [String](#string)
key [String](#string)
success [Boolean](#boolean)
value [byte[]](#ByteArray) format: byte

[Up](#models)
### cacheNameCreate

A JSON object use to create a cacheName namespace.
cacheName [String](#string)
description (optional)[String](#string)

[Up](#models)
### cacheNameListResponse_inner

An array of all known cacheNames, along with an optional description.
cacheName [String](#string)
description (optional)[String](#string)

[Up](#models)
### cacheNameResponse

The complete cacheName object.
cacheName [String](#string)
description [String](#string)
deleted [Boolean](#boolean)

[Up](#models)
### cacheRequest

A JSON request object where the object is provided as a BASE64 encoded value.
cacheName [String](#string)
key [String](#string)
ttlSeconds (optional)[Integer](#integer)
value [byte[]](#ByteArray) format: byte

---
