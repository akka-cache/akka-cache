import * as constants from "../src/constants"
import {testBatchCreateCache} from "../src/batchCreateCaches";
import {error, log} from "console";
import { BatchDeleteRequest } from 'akka-cache';
import { BatchDeleteCacheResponse } from 'akka-cache';
import {CACHE1} from "../src/constants";
import {Buffer} from "buffer";
import {testBatchGetCache} from "../src/batchGetCaches";
import {testBatchDeleteCache} from "../src/batchDeleteCaches";


describe('testing batch delete cache APIs', () => {
    test('should delete the batched cache successfully', done => {

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

        expect(testBatchDeleteCache(request)).resolves.toStrictEqual( {"cacheDeleteResponses":[{"cacheName":"ttorgcache1","key":"key10","success":true},{"cacheName":"ttorgcache1","key":"key11","success":true},{"cacheName":"ttorgcache1","key":"key12","success":true}]} );

        done();
    });

});