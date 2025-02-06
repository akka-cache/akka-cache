---
# Display h2 to h5 headings
toc_min_heading_level: 2
toc_max_heading_level: 3
---

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

In the following REST curl examples we'll be using the following Bearer Token which was generated on the [JWT.io](https://jwt.io/) website with the claims above.
```
-H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJodHRwczovL3Nlc3Npb24uZmlyZWJhc2UuZ29vZ2xlLmNvbS9ha2thLWNhY2hlIiwib3JnIjoidHRvcmciLCJuYW1lIjoiSm9obiBEb2UiLCJzZXJ2aWNlTGV2ZWwiOiJmcmVlIn0.rds8orVxVz149ovTxxYzFIqGmSdWJUlHONem9avKBgQ"
```
We'll also be running our curl examples against a locally running version of the akka-cache application.
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

#### Example data

Content-Type: application/json

```
{
  "cacheName" : "cacheName",
  "deleted" : true,
  "description" : "description"
}
```

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
get /cacheName/{cacheName}/keys
```

Get the current cache namespace's list of cache keys.

#### Path parameters

- **cacheName (required)**\
Path Parameter — Name of a cache namespace. (group) default: null

#### Return type

array[String]

#### Example data

Content-Type: application/json

```
{"cacheName":"cache1", "description":"This is our first test"}
```

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
get /cacheName/list
```

Retrieves a list of cacheNames and optional description.

#### Return type

array[[cacheNameListResponse_inner](#cachenamelistresponse_inner)]

#### Example data

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

---


### Cache Name Put

```
put /cacheName
```

Create or update a cache namespace description.

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
Path Parameter — Time to Live (TTL) of the cached object before being automatically deleted. default: null

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
get /get/{cacheName}/{key}
```

Get the current cache namespace description, and delete status.

#### Path parameters

- **cacheName (required)**\
Path Parameter — Name of a cache namespace. (group) default: null key (required)
Path Parameter — The key of a specific cache object within a cacheName. default: null

#### Return type

[cacheGetResponse](#cachegetresponse)

#### Example data

Content-Type: application/json

```
{
  "cacheName" : "cacheName",
  "success" : true,
  "value" : "value",
  "key" : "key"
}
```

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

#### Example data

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

#### Example data

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

#### Example data

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
