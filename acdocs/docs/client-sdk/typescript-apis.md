# Typescript APIs

## Typescript SDK Installation

**TODO**

## Access

Endpoint Authentication is based upon JSON Web Tokens (JWT) using HTTP Bearer Token authentication headers.
> **_NOTE:_**  When deploying the Akka Cache application to your Akka account the JWT is going to be different from the following example as it’ll be encrypted by your private key. For more information, please see [Securing your Cache Service](/getting-started/securing-cache-service).

The following claims (or fields) are required in the JWT:
```
{
"iss": "https://session.firebase.google.com/akka-cache",
"org": "ttorg",
"name": "John Doe",
"serviceLevel": "free"
}
```

## Configuration

In the Typescript examples used throughout the APIs below we'll be relying upon the following configuration file that contains the following including the Bearer Token which was generated on the [JWT.io](https://jwt.io/) website with the claims above.

```ts title="configuration.ts"
import { ConfigurationParameters } from 'akka-cache';
import { Configuration } from 'akka-cache';

const cfgParams: ConfigurationParameters = {
    accessToken: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJodHRwczovL3Nlc3Npb24uZmlyZWJhc2UuZ29vZ2xlLmNvbS9ha2thLWNhY2hlIiwib3JnIjoidHRvcmciLCJuYW1lIjoiSm9obiBEb2UiLCJzZXJ2aWNlTGV2ZWwiOiJmcmVlIn0.rds8orVxVz149ovTxxYzFIqGmSdWJUlHONem9avKBgQ',
//    basePath: "http://localhost:9001/cache"  // this is instead of accessToken if using the unsecured endpoint
}

// Pass the config Params to the Configuration
export const cfg = new Configuration(cfgParams)
```
There are three separate APIs provided for interacting with Akka Cache. These include CacheNames, Cache, and Batch. All three APIs must be accessed through configuration.

For example,

```
import { cfg } from './configuration';
import { CacheNameApi } from 'akka-cache';
import { CacheApi } from 'akka-cache';
import { BatchApi } from 'akka-cache';

...
    const cacheNameAPI = new CacheNameApi(cfg);
    const cacheAPI = new CacheApi(cfg);
    const batchAPI = new BatchApi(cfg);
```
All the following Typescript examples assume the configurations described above are in place.

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
cacheNameAPI.cacheNameCacheNameDelete(cacheName);
```

Deletes the current cache namespace and all associated caches.

#### Parameters

- **cacheName (required)**\
  Path Parameter — Name of a cache namespace. (group) default: null

#### Return type

- **Promise\<void\>**

#### Example
```ts title="cacheNameDelete.ts"
import { CacheNameApi } from 'akka-cache';
import { cfg } from './configuration';
import { log, error } from "console";

export async function testDeleteCacheName(cacheName:string) : Promise<void>  {
    const cacheNameAPI = new CacheNameApi(cfg);

    try {
        return cacheNameAPI.cacheNameCacheNameDelete(
            {cacheName: cacheName}
        )
            .then(result => {
                log("cacheNameDelete successful.");
                return result;
            })

    } catch (err) {
        error('Error thrown : ', err);
    }
}
```

---

### Cache Name Flush

```
cacheNameAPI.cacheNameCacheNameFlushPut(cacheName)
```

Deletes all existing cached objects but leaves the cache namespace in place.

#### Parameters

- **cacheName (required)**\
  Path Parameter — Name of a cache namespace. (group) default: null

#### Return type

- **Promise\<void\>**

```ts title="cacheNameFlush.ts"
import { CacheNameApi } from 'akka-cache';
import { cfg } from './configuration';
import { log, error } from "console";

export async function testFlushCacheName(cacheName:string) : Promise<void>  {
    const cacheNameAPI = new CacheNameApi(cfg);

    try {
        return cacheNameAPI.cacheNameCacheNameFlushPut(
            {cacheName: cacheName}
        )
        .then(result => {
            log("cacheNameFlush successful.");
            return result;
        })

    } catch (err) {
        error('Error thrown : ', err);
    }
}
```
---


### Cache Name Get

```
cacheNameAPI.cacheNameCacheNameGet(cacheName)
```

Get the specified cache name description, and delete status. (cacheNameCacheNameGet)

#### Parameters

- **cacheName (required)**\
  Path Parameter — Name of a cache namespace. (group) default: null

#### Return type

- Promise\<[cacheNameResponse](#cachenameresponse)\>

```ts title="cacheNameGet.ts"
import { CacheNameApi } from 'akka-cache';
import { cfg } from './configuration';
import { CacheNameResponse } from 'akka-cache'
import { log, error } from "console";

export async function testGetCacheName(cacheName:string) : Promise<CacheNameResponse>  {
    const cacheNameAPI = new CacheNameApi(cfg);

    try {
        return cacheNameAPI.cacheNameCacheNameGet(
            {cacheName: cacheName}
        )
        .then(result => {
            log("cacheNameGet successful for " + result.cacheName + "," + result.description);
            return result;
        })
        .catch(err => {
            error("an error occurred: ", err.message  );
            return err;
        })

    } catch (error) {
        console.error('Error thrown : ', error);
    }
}
```

### Cache Name Get Keys

```
cacheNameAPI.cacheNameCacheNameKeysGet(cacheName)
```

Get the current cache namespace's list of cache keys.

#### Parameters

- **cacheName (required)**\
  Path Parameter — Name of a cache namespace. (group) default: null

#### Return type

- Promise\<[CacheNameKeys](#cachenamekeys)\>

```ts title="CacheNameKeysGet.ts"
import { CacheNameApi } from 'akka-cache';
import { cfg } from './configuration';
import { CacheNameKeys } from 'akka-cache'
import { log, error } from "console";

export async function testCacheNameKeysGet(cacheName:string) : Promise<CacheNameKeys>  {
    const cacheNameAPI = new CacheNameApi(cfg);

    try {
        return cacheNameAPI.cacheNameCacheNameKeysGet(
            {cacheName: cacheName}
        )
        .then(result => {
            log("cacheNameCacheNameKeysGet successful for " + result.keys);
            return result;
        })
        .catch(err => {
            error("an error occurred w/ cacheNameCacheNameKeysGet: ", err.message  );
            return err;
        })

    } catch (error) {
        console.error('Error thrown : ', error);
    }
}
```
---

### Cache Name List

TODO: NOT IMPLEMENTED YET

```
get /cacheName/list
```

Retrieves a list of cacheNames and optional description.

#### Return type

array[[cacheNameListResponse_inner](#cachenamelistresponse_inner)]

---

### Cache Name Post

```
cacheNameAPI.cacheNamePost(cacheNameCreate)
```

Create or update a cache namespace description

#### Parameters

- **[CacheNameCreate](#cachenamecreate) (required)**

#### Return type

```ts title="cacheNameCreate.ts"
import { CacheNameApi } from 'akka-cache';
import { cfg } from './configuration';
import { log, error } from "console";

export async function testCreateCacheName(cacheName:string, description:string) : Promise<void>  {
    const cacheNameAPI = new CacheNameApi(cfg);

    try {
        return cacheNameAPI.cacheNamePost({
            cacheNameCreate: {cacheName: cacheName, description: description}
        })
        .then(nil => {
            log("cacheNameCreate successful for " + cacheName);
            return nil;
        })

    } catch (err) {
        error('Error thrown : ', err);
    }
}
```
---


### Cache Name Put

```
put /cacheName
```

Update an existing cache namespace's description.


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


---


### Cache Set (JSON)

```
post /set
```

Create or update a cached object (BASE64 encoded) by key within the given cacheName.


#### Request body

cacheRequest [cacheRequest](#cacherequest) (required)\
Body Parameter —

---
## Batch {#batch-section}

Batch APIs used to create, retrieve, and delete cached objects.


### Batch Delete

```
delete /batch
```

Deletes a batch of individually cached objects.


#### Request body

batchGetCacheRequests_inner [batchGetCacheRequests_inner](#batchgetcacherequests_inner) (required)\
Body Parameter —

#### Return type

array[[batchDeleteCacheResponse_inner](#batchdeletecacheresponse_inner)]



---


### Batch Get

```
post /batch/get
```

Retrieve a batch of cached objects.


#### Request body

batchGetCacheRequests_inner [batchGetCacheRequests_inner](#batchgetcacherequests_inner) (required)\
Body Parameter —

#### Return type

[batchGetCacheResponse](#batchgetcacheresponse)


#### REST Curl Example
```
curl -i -curl -i -H "Authorization: Bearer <YOUR-JWT>" -d '{"getCachedBatch" : [{"cacheName":"cache1", "key":"key3"}, {"cacheName":"cache1", "key":"key2"},  {"cacheName":"cache1", "key":"key1"}]}' -H "Content-Type: application/json" -X POST http://localhost:9001/batch/get -d '{"getCachedBatch" : [{"cacheName":"cache1", "key":"key3"}, {"cacheName":"cache1", "key":"key2"},  {"cacheName":"cache1", "key":"key1"}]}' -H "Content-Type: application/json" -X POST http://localhost:9001/batch/get
```


---


### Batch Create

```
post /batch
```

Create batch of cached objects, results are returned for each batched element.


#### Request body

batchCacheRequest_inner [batchCacheRequest_inner](#batchcacherequest_inner) (required)\
Body Parameter —

#### Return type

[batchCacheResponse](#batchcacheresponse)

---

## Models

[ Jump to [Methods](#methods) ]

### Table of Contents

- [`batchCacheRequest`](#batchcacherequest)
- [`batchCacheResponse`](#batchcacheresponse)
- [`batchCacheResponseResults`](#batchcacheresponseresultsinner)
- [`batchCacheResult`](#batchcacheresult)
- [`batchDeleteCacheResponse`](#batchdeletecacheresponse)
- [`batchGetCacheRequest`](#batchgetcacherequest)
- [`batchGetCacheRequests`](#batchgetcacherequests)
- [`batchGetCacheResponse`](#batchgetcacheresponse)
- [`batchGetCacheResponseResults`](#batchgetcacheresponseresults)
- [`cacheDeleteResponse`](#cachedeleteresponse)
- [`cacheGetResponse`](#cachegetresponse)
- [`cacheNameCreate`](#cachenamecreate)
- [`cacheNameKeys`](#cachenamekeys)
- [`cacheNameListResponses`](#cachenamelistresponses)
- [`CacheNamePostRequest`](#cachenamepostrequest)
- [`cacheNameResponse`](#cachenameresponse)
- [`cacheRequest`](#cacherequest)


### batchCacheRequest

A JSON array of objects to be cached where the object must be a BASE64 encoded value.

```ts title="BatchCacheRequest.ts"
export interface BatchCacheRequest {
    /**
     * 
     * @type {Array<CacheRequest>}
     * @memberof BatchCacheRequest
     */
    cacheRequests?: Array<CacheRequest>;
}
```

---
### batchCacheResponse

A JSON wrapped array of batched cache results.

```ts title="BatchCacheResponse"
export interface BatchCacheResponse {
    /**
     * 
     * @type {boolean}
     * @memberof BatchCacheResponse
     */
    complete?: boolean;
    /**
     * 
     * @type {Array<BatchCacheResponseResultsInner>}
     * @memberof BatchCacheResponse
     */
    results?: Array<BatchCacheResponseResultsInner>;
}
```

---
### batchCacheResponseResultsInner

A JSON array of batched cache results.

```ts title="BatchCacheResponseResultsInner.ts"
export interface BatchCacheResponseResultsInner {
    /**
     * 
     * @type {string}
     * @memberof BatchCacheResponseResultsInner
     */
    cacheName: string;
    /**
     * 
     * @type {string}
     * @memberof BatchCacheResponseResultsInner
     */
    key: string;
    /**
     * 
     * @type {boolean}
     * @memberof BatchCacheResponseResultsInner
     */
    success: boolean;
}
```

---
### batchCacheResult

A JSON based array of results for each batched cache request.
```ts title="BatchCacheResult.ts"
export interface BatchCacheResult {
    /**
     * 
     * @type {string}
     * @memberof BatchCacheResult
     */
    cacheName: string;
    /**
     * 
     * @type {string}
     * @memberof BatchCacheResult
     */
    key: string;
    /**
     * 
     * @type {boolean}
     * @memberof BatchCacheResult
     */
    success: boolean;
}
```

---
### batchDeleteCacheResponse

```
export interface BatchDeleteCacheResponse {
    /**
     * A JSON wrapped array of batched delete cache results.
     * @type {Array<CacheDeleteResponse>}
     * @memberof BatchDeleteCacheResponse
     */
    cacheDeleteResponses?: Array<CacheDeleteResponse>;
}
```

---
### batchGetCacheRequest

A cashName/key value pair of a cached object to retrieve.

```ts title="BatchGetCacheRequest.ts"
export interface BatchGetCacheRequest {
    /**
     * 
     * @type {string}
     * @memberof BatchGetCacheRequest
     */
    cacheName: string;
    /**
     * 
     * @type {string}
     * @memberof BatchGetCacheRequest
     */
    key: string;
}
```

---
### batchGetCacheRequests

A JSON array of retrieved cashName/key pairs to retrieve as a batch.

```ts title="BatchGetCacheRequests.ts"
export interface BatchGetCacheRequests {
    /**
     * 
     * @type {Array<BatchGetCacheRequest>}
     * @memberof BatchGetCacheRequests
     */
    getCachedBatch?: Array<BatchGetCacheRequest>;
}
```

---
### batchGetCacheResponse

```ts title="BatchGetCacheResponse.ts"
export interface BatchGetCacheResponse {
    /**
     * 
     * @type {boolean}
     * @memberof BatchGetCacheResponse
     */
    complete?: boolean;
    /**
     * 
     * @type {Array<BatchGetCacheResponseResultsInner>}
     * @memberof BatchGetCacheResponse
     */
    results?: Array<BatchGetCacheResponseResultsInner>;
}
```
---
### batchGetCacheResponseResults

```ts title="BatchGetCacheResponseResultsInner.ts"
export interface BatchGetCacheResponseResultsInner {
    /**
     * 
     * @type {string}
     * @memberof BatchGetCacheResponseResultsInner
     */
    cacheName: string;
    /**
     * 
     * @type {string}
     * @memberof BatchGetCacheResponseResultsInner
     */
    key: string;
    /**
     * 
     * @type {boolean}
     * @memberof BatchGetCacheResponseResultsInner
     */
    success: boolean;
    /**
     * 
     * @type {string}
     * @memberof BatchGetCacheResponseResultsInner
     */
    value: string;
}
```

---
### cacheDeleteResponse

```ts title="CacheDeleteResponse.ts"
export interface CacheDeleteResponse {
  /**
   *
   * @type {string}
   * @memberof CacheDeleteResponse
   */
  cacheName: string;
  /**
   *
   * @type {string}
   * @memberof CacheDeleteResponse
   */
  key: string;
  /**
   *
   * @type {boolean}
   * @memberof CacheDeleteResponse
   */
  success: boolean;
}
```
---
### cacheGetResponse

The cached JSON object returned as a BASE64 encoded value.

```ts title="CacheGetResponse.ts"
export interface CacheGetResponse {
    /**
     * 
     * @type {string}
     * @memberof CacheGetResponse
     */
    cacheName: string;
    /**
     * 
     * @type {string}
     * @memberof CacheGetResponse
     */
    key: string;
    /**
     * 
     * @type {boolean}
     * @memberof CacheGetResponse
     */
    success: boolean;
    /**
     * 
     * @type {string}
     * @memberof CacheGetResponse
     */
    value: string;
}
```
---
### cacheNameCreate

A JSON object use to create a cacheName namespace.

```ts title="CacheNameCreate.ts"
export interface CacheNameCreate {
    /**
     * 
     * @type {string}
     * @memberof CacheNameCreate
     */
    cacheName: string;
    /**
     * 
     * @type {string}
     * @memberof CacheNameCreate
     */
    description?: string;
}
```
---
### cacheNameKeys

An array of all known keys under the given cacheName.

```ts title="CacheNameKeys.ts"
export interface CacheNameKeys {
    /**
     * 
     * @type {string}
     * @memberof CacheNameKeys
     */
    cacheName?: string;
    /**
     * An array of all known keys under the given cacheName.
     * @type {Array<string>}
     * @memberof CacheNameKeys
     */
    keys?: Array<string>;
}
```
---
### cacheNameListResponse

An array of all known cacheNames, along with an optional description.

```ts title="CacheNameListResponse.ts"
export interface CacheNameListResponse {
  /**
   *
   * @type {Array<CacheNameListResponseCacheNameListInner>}
   * @memberof CacheNameListResponse
   */
  cacheNameList?: Array<CacheNameListResponseCacheNameListInner>;
}
```
---
### cacheNameListResponses

An array of all known cacheNames, along with an optional description.

```ts title="CacheNameListResponseCacheNameListInner.ts"
export interface CacheNameListResponseCacheNameListInner {
    /**
     * 
     * @type {string}
     * @memberof CacheNameListResponseCacheNameListInner
     */
    cacheName: string;
    /**
     * 
     * @type {string}
     * @memberof CacheNameListResponseCacheNameListInner
     */
    description?: string;
}
```
---
### cacheNameResponse

The complete cacheName object.

```ts title="CacheNameResponse.ts"
export interface CacheNameResponse {
    /**
     * 
     * @type {string}
     * @memberof CacheNameResponse
     */
    cacheName: string;
    /**
     * 
     * @type {string}
     * @memberof CacheNameResponse
     */
    description: string;
    /**
     * 
     * @type {boolean}
     * @memberof CacheNameResponse
     */
    deleted: boolean;
}
```
---
### cacheRequest

A JSON request object where the object is provided as a BASE64 encoded value.

```ts title="CacheRequest.ts"
export interface CacheRequest {
    /**
     * 
     * @type {string}
     * @memberof CacheRequest
     */
    cacheName: string;
    /**
     * 
     * @type {string}
     * @memberof CacheRequest
     */
    key: string;
    /**
     * 
     * @type {number}
     * @memberof CacheRequest
     */
    ttlSeconds?: number;
    /**
     * 
     * @type {string}
     * @memberof CacheRequest
     */
    value: string;
}

```