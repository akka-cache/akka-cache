import { testCacheSetJSON } from "../src/cacheSetJSON"
import {testGetCacheJSON} from "../src/cacheGetJSON";
import * as constants from "../src/constants"
import {testDeleteCache} from "../src/cacheDelete";

beforeAll(() => {
    expect(testCacheSetJSON(constants.CACHE1, constants.KEY2, constants.PAYLOAD+"2", 10)).resolves.toBe(undefined);
});

describe('testing cacheSET (JSON) API', () => {
    test('should retrieve the cache successfully', () => {
        // NOTE: the org from the bearer token is used to prefix the return cacheName
        expect(testGetCacheJSON(constants.CACHE1, constants.KEY2)).resolves.toStrictEqual( {"cacheName": constants.ORG+constants.CACHE1, "key": constants.KEY1, "success": true, "value": constants.PAYLOAD + "2"});
    });
});

