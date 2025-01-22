import * as constants from "../src/constants"
import {testCachePostBinary} from "../src/cacheCreateBinaryPOST";
import {testGetCacheJSON} from "../src/cacheGetJSON";
import {testGetCacheName} from "../src/cacheNameGet";
import {testGetCacheBinary} from "../src/cacheGetBinary";

describe('testing cacheSET (Binary) API', () => {
    test('should create the cache, and then read it successfully', () => {
        // NOTE: the org from the bearer token is used to prefix the return cacheName
        expect(testCachePostBinary(constants.CACHE1, constants.KEY3, new Blob([constants.PAYLOAD+"3"], { type: "text/plain" }))).resolves.toBe(undefined);
        expect(testGetCacheBinary(constants.CACHE1, constants.KEY3)).resolves.toStrictEqual( {"cacheName": constants.ORG+constants.CACHE1, "key": constants.KEY1, "success": true, "value": constants.PAYLOAD + "3"});
    });
});

