import * as constants from "../src/constants"
import {testBatchCreateCache} from "../src/batchCreateCaches";
import {error, log} from "console";
import {BatchDeleteRequest, BatchGetPostRequest, BatchPostRequest} from "akka-cache";
import type {CacheRequest} from "akka-cache";
import {CACHE1} from "../src/constants";
import {Buffer} from "buffer";
import {testBatchGetCache} from "../src/batchGetCaches";
import {testBatchDeleteCache} from "../src/batchDeleteCaches";

const b64encode = (str: string):string => Buffer.from(str, 'binary').toString('base64');

describe('testing batch cache APIs', () => {
    test('should create the batched cache successfully', async () => {
        const request = {
            batchCacheRequest: {
                cacheRequests: [
                    {
                        cacheName: CACHE1,
                        key: "key10",
                        value: b64encode("cache for cache1, and key10")
                    },
                    {
                        cacheName: CACHE1,
                        key: "key11",
                        value: b64encode("cache for cache1, and key11")
                    },
                    {
                        cacheName: CACHE1,
                        key: "key12",
                        value: b64encode("cache for cache1, and key12")
                    }
                ]
            }
        } as BatchPostRequest;

        log(request);

        const result = await testBatchCreateCache(request);
        expect(result).toStrictEqual( {"complete":true,"results":[]} );
    });

    test('should get the batched cache successfully', async () => {

        const request = {
            batchGetCacheRequests: {
                getCachedBatch: [
                    {
                        cacheName: CACHE1,
                        key: "key10",
                    },
                    {
                        cacheName: CACHE1,
                        key: "key11",
                    },
                    {
                        cacheName: CACHE1,
                        key: "key12",
                    }
                ]
            }
        } as BatchGetPostRequest;

        log(request);

        const result = await testBatchGetCache(request);
        expect(result).toStrictEqual( {"complete":true,"results":[{"cacheName":"ttorgcache1","key":"key10","success":true,"value":"Y2FjaGUgZm9yIGNhY2hlMSwgYW5kIGtleTEw"},{"cacheName":"ttorgcache1","key":"key11","success":true,"value":"Y2FjaGUgZm9yIGNhY2hlMSwgYW5kIGtleTEx"},{"cacheName":"ttorgcache1","key":"key12","success":true,"value":"Y2FjaGUgZm9yIGNhY2hlMSwgYW5kIGtleTEy"}]} );
    });

    test('should delete the batched cache successfully', async () => {

        const request = {
            batchGetCacheRequests: {
                getCachedBatch: [
                    {
                        cacheName: CACHE1,
                        key: "key10",
                    },
                    {
                        cacheName: CACHE1,
                        key: "key11",
                    },
                    {
                        cacheName: CACHE1,
                        key: "key12",
                    }
                ]
            }
        } as BatchDeleteRequest;

        log(request);

        const result = await testBatchDeleteCache(request);

        expect(result).toStrictEqual( {"cacheDeleteResponses":[{"cacheName":"ttorgcache1","key":"key10","success":true},{"cacheName":"ttorgcache1","key":"key11","success":true},{"cacheName":"ttorgcache1","key":"key12","success":true}]} );
    });

});