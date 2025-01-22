import { testCacheSetJSON } from "../src/cacheSetJSON"
import {testGetCacheJSON} from "../src/cacheGetJSON";
import * as constants from "../src/constants"
import {testDeleteCache} from "../src/cacheDelete";

beforeAll(() => {
    expect(testCacheSetJSON(constants.CACHE1, constants.KEY1, constants.PAYLOAD+"1")).resolves.toBe(undefined);
});

afterAll(() => {
    expect(testDeleteCache(constants.CACHE1, constants.KEY1)).resolves.toBe(undefined);
});

describe('testing cacheSET (JSON) API', () => {
    test('should retrieve the cache successfully', () => {
        // NOTE: the org from the bearer token is used to prefix the return cacheName
        expect(testGetCacheJSON(constants.CACHE1, constants.KEY1)).resolves.toStrictEqual( {"cacheName": constants.ORG+constants.CACHE1, "key": constants.KEY1, "success": true, "value": constants.PAYLOAD + "1"});
    });
});

