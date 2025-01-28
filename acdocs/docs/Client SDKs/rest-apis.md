# REST APIs

## Access

Endpoint Authentication is based upon Java Web Tokens (JWT) using HTTP Bearer Token authentication headers.

TODO: (Sample JWT w/ iss, org, and serviceLevel=free )

## Methods

[ Jump to [Models](#models) ]

### Table of Contents

#### CacheName
APIs used to maintain a cache name, namespace, or group.

* [`delete /cacheName/{cacheName}`](#cacheNameCacheNameDelete)
* [`put /cacheName/{cacheName}/flush`](#cacheNameCacheNameFlushPut)
* [`get /cacheName/{cacheName}`](#cacheNameCacheNameGet)
* [`get /cacheName/{cacheName}/keys`](#cacheNameCacheNameKeysGet)
* [`get /cacheName/list`](#cacheNameListGet)
* [`post /cacheName`](#cacheNamePost)
* [`put /cacheName`](#cacheNamePut)

#### Cache
APIs used to create, retrieve, and delete cached objects.

* [`delete /{cacheName}/{key}`](#cacheNameKeyDelete)
* [`get /{cacheName}/{key}`](#cacheNameKeyGet)
* [`post /{cacheName}/{key}`](#cacheNameKeyPost)
* [`post /{cacheName}/{key}/{ttlSeconds}`](#cacheNameKeyTtlSecondsPost)
* [`get /get/{cacheName}/{key}`](#getCacheNameKeyGet)
* [`post /set`](#setPost)

#### Batch
Batch APIs used to create, retrieve, and delete cached objects.

* [`delete /batch`](#batchDelete)
* [`post /batch/get`](#batchGetPost)
* [`post /batch`](#batchPost)

---

# [CacheName](#CacheName)
APIs used to maintain a cache name, namespace, or group.

[Up](#methods)

## cacheNameCacheNameDelete
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
```
put /cacheName/{cacheName}/flush
```

deletes all existing cached objects but leaves the cache namespace in place (cacheNameCacheNameFlushPut)
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
```
get /cacheName/{cacheName}/keys
```

get the current cache namespace description, and delete status (cacheNameCacheNameKeysGet)
### Path parameters

cacheName (required)
Path Parameter — Name of a cache namespace. (group) default: null

### Return type

array[String]

### Example data

Content-Type: application/json
```
[ "", "" ]
```
### Produces

This API call produces the following media types according to the Accept request header;
the media type will be conveyed by the Content-Type response header.

* `application/json`

### Responses

#### 200

returned a JSON format of the cache namespace, and description
#### 400

bad request or doesn't exist

---

[Up](#methods)
```
get /cacheName/list
```

Retrieves a list of cacheNames and optional description. (cacheNameListGet)
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
```
post /cacheName
```

Create or update a cache namespace description (cacheNamePost)
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
```
put /cacheName
```

Create or update a cache namespace description (cacheNamePut)
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


# [Cache](#Cache)
APIs used to create, retrieve, and delete cached objects.

[Up](#methods)
```
delete /{cacheName}/{key}
```

Deletes the currently cached object by key within the given cacheName. (cacheNameKeyDelete)
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
```
get /{cacheName}/{key}
```

Retrieves the currently cached object by key within the given cacheName. Note: this is a binary version of the API and requires that the cache object be passed in the request body. (cacheNameKeyGet)
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
```
post /{cacheName}/{key}
```

Create or update a cached object under the cacheName. Note: this is a binary version of the API and requires that the cache object be passed in the request body. (cacheNameKeyPost)
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
```
post /{cacheName}/{key}/{ttlSeconds}
```

Create or update a cached object by key within the given cacheName. Note: this is a binary version of the API and requires that the cache object be passed in the request body. (cacheNameKeyTtlSecondsPost)
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
```
get /get/{cacheName}/{key}
```

Get the current cache namespace description, and delete status. (getCacheNameKeyGet)
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
```
post /set
```

Create or update a cached object (BASE64 encoded) by key within the given cacheName. (setPost)
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

# [Batch](#Batch)
Batch APIs used to create, retrieve, and delete cached objects.

[Up](# Methods)
```
delete /batch
```

Deletes a batch of individually cached objects. (batchDelete)
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
```
post /batch/get
```

Retrieve a batch of cached objects. (batchGetPost)
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
```
post /batch
```

Create batch of cached objects, results are returned for each batched element. (batchPost)
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

1. [`batchCacheRequest_inner` -](#batchCacheRequest_inner)
2. [`batchCacheResponse` -](#batchCacheResponse)
3. [`batchCacheResponse_results_inner` -](#batchCacheResponse_results_inner)
4. [`batchCacheResult` -](#batchCacheResult)
5. [`batchDeleteCacheResponse_inner` -](#batchDeleteCacheResponse_inner)
6. [`batchGetCacheRequest` -](#batchGetCacheRequest)
7. [`batchGetCacheRequests_inner` -](#batchGetCacheRequests_inner)
8. [`batchGetCacheResponse` -](#batchGetCacheResponse)
9. [`batchGetCacheResponse_results_inner` -](#batchGetCacheResponse_results_inner)
10. [`cacheDeleteResponse` -](#cacheDeleteResponse)
11. [`cacheGetResponse` -](#cacheGetResponse)
12. [`cacheNameCreate` -](#cacheNameCreate)
13. [`cacheNameListResponse_inner` -](#cacheNameListResponse_inner)
14. [`cacheNameResponse` -](#cacheNameResponse)
15. [`cacheRequest` -](#cacheRequest)

### `batchCacheRequest_inner` - [Up](#models)

A JSON array of objects to be cached where the object must be a BASE64 encoded value.
cacheName [String](#string)
key [String](#string)
ttlSeconds (optional)[Integer](#integer)
value [byte[]](#ByteArray) format: byte

### `batchCacheResponse` - [Up](#models)

A JSON wrapped array of batched cache results.

complete (optional)[Boolean](#boolean)
results (optional)[array[batchCacheResponse\_results\_inner]](#batchCacheResponse_results_inner)

### `batchCacheResponse_results_inner` - [Up](#models)

A JSON array of batched cache results.
cacheName [String](#string)
key [String](#string)
success [Boolean](#boolean)

### `batchCacheResult` - [Up](#models)

A JSON based array of results for each batched cache request.
cacheName [String](#string)
key [String](#string)
success [Boolean](#boolean)

### `batchDeleteCacheResponse_inner` - [Up](#models)

cacheName [String](#string)
key [String](#string)
success [Boolean](#boolean)

### `batchGetCacheRequest` - [Up](#models)

A cashName/key value pair of a cached object to retrieve.
cacheName [String](#string)
key [String](#string)

### `batchGetCacheRequests_inner` - [Up](#models)

A JSON array of retrieved cashName/key pairs to retrieve as a batch.
cacheName [String](#string)
key [String](#string)

### `batchGetCacheResponse` - [Up](#models)

complete (optional)[Boolean](#boolean)
results (optional)[array[batchGetCacheResponse\_results\_inner]](#batchGetCacheResponse_results_inner) A JSON response containing the list of cached objects retrieved. Note: each object is encoded as a BASE64 encoded value.

### `batchGetCacheResponse_results_inner` - [Up](#models)

cacheName [String](#string)
key [String](#string)
success [Boolean](#boolean)
value [byte[]](#ByteArray) format: byte

### `cacheDeleteResponse` - [Up](#models)

A JSON response of the cache delete request.
cacheName [String](#string)
key [String](#string)
success [Boolean](#boolean)

### `cacheGetResponse` - [Up](#models)

The cached JSON object returned as a BASE64 encoded value.
cacheName [String](#string)
key [String](#string)
success [Boolean](#boolean)
value [byte[]](#ByteArray) format: byte

### `cacheNameCreate` - [Up](#models)

A JSON object use to create a cacheName namespace.
cacheName [String](#string)
description (optional)[String](#string)

### `cacheNameListResponse_inner` - [Up](#models)

An array of all known cacheNames, along with an optional description.
cacheName [String](#string)
description (optional)[String](#string)

### `cacheNameResponse` - [Up](#models)

The complete cacheName object.
cacheName [String](#string)
description [String](#string)
deleted [Boolean](#boolean)

### `cacheRequest` - [Up](#models)

A JSON request object where the object is provided as a BASE64 encoded value.
cacheName [String](#string)
key [String](#string)
ttlSeconds (optional)[Integer](#integer)
value [byte[]](#ByteArray) format: byte

---
