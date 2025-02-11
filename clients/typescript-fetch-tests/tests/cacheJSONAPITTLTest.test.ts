import { testCacheSetJSON } from "../src/cacheSetJSON"
import {testGetCacheJSON} from "../src/cacheGetJSON";
import * as constants from "../src/constants"
import {testDeleteCache} from "../src/cacheDelete";

// note: we don't need to encode to BASE64 as the payload is just a string

beforeAll(done => {
    expect(testCacheSetJSON(constants.CACHE1, constants.KEY2, constants.PAYLOAD+"2", 10)).resolves.toBe(undefined);
    done();
});

describe('testing cacheSET (JSON) API', () => {
    test('should retrieve the cache successfully', async () => {
        const result = await testGetCacheJSON(constants.CACHE1, constants.KEY2);
        expect(result).toMatchObject( {"cacheName": constants.ORG+constants.CACHE1, "key": constants.KEY2, "success": true, "value": constants.PAYLOAD + "2"});
    });
});

