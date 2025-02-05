import * as constants from "../src/constants"
import {testBatchCreateCache} from "../src/batchCreateCaches";
import {error, log} from "console";
import type {BatchPostRequest} from "akka-cache";
import type {CacheRequest} from "akka-cache";
import {CACHE1} from "../src/constants";
import {Buffer} from "buffer";

const b64encode = (str: string):string => Buffer.from(str, 'binary').toString('base64');

describe('testing batch create cache APIs', () => {
    test('should create the batched cache successfully', done => {

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

        expect(testBatchCreateCache(request)).resolves.toStrictEqual( {"complete":true,"results":[]} );

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