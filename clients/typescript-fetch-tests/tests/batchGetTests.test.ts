import * as constants from "../src/constants"
import {testBatchCreateCache} from "../src/batchCreateCaches";
import {error, log} from "console";
import { BatchGetPostRequest } from 'akka-cache';
import { BatchGetCacheResponse } from 'akka-cache';
import {CACHE1} from "../src/constants";
import {Buffer} from "buffer";
import {testBatchGetCache} from "../src/batchGetCaches";


describe('testing batch create cache APIs', () => {
    test('should create the batched cache successfully', done => {

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

        expect(testBatchGetCache(request)).resolves.toStrictEqual( {"complete":true,"results":[{"cacheName":"ttorgcache1","key":"key10","success":true,"value":"Y2FjaGUgZm9yIGNhY2hlMSwgYW5kIGtleTEw"},{"cacheName":"ttorgcache1","key":"key11","success":true,"value":"Y2FjaGUgZm9yIGNhY2hlMSwgYW5kIGtleTEx"},{"cacheName":"ttorgcache1","key":"key12","success":true,"value":"Y2FjaGUgZm9yIGNhY2hlMSwgYW5kIGtleTEy"}]} );

        done();
    });
/*

    test('should fail because one item isn\'t encoded', done => {

        const request = {
            batchCacheRequest: {
                cacheRequests: [
                    {
                        cacheName: CACHE1,
                        key: "key20",
                        value: b64encode("cache for cache1, and key20")
                    },
                    {
                        cacheName: CACHE1,
                        key: "key31",
                        value: "cache for cache1, and key31"
                    },
                    {
                        cacheName: CACHE1,
                        key: "key32",
                        value: b64encode("cache for cache1, and key32")
                    }
                ]
            }
        } as BatchPostRequest;

        log(request);

        expect(testBatchCreateCache(request)).resolves.toStrictEqual( {"cacheName": constants.ORG+constants.CACHE1 });

        done();
    });
*/



});