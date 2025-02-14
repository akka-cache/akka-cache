import {testCacheNameKeysGet} from "../src/cacheNameKeysGet";
import * as constants from "../src/constants";
import {testCacheSetJSON} from "../src/cacheSetJSON";
import {Buffer} from "buffer";
import {error, log} from "console";

const b64encode = (str: string):string => Buffer.from(str, 'binary').toString('base64');

describe('testing cache get keys', () => {

/*
    test('should successfully create cache1(KEY31)', async () => {
        const result = testCacheSetJSON(constants.CACHE1, constants.KEY31, b64encode(constants.PAYLOAD+"31"))
    });

    test('should successfully create cache1(KEY32)', async () => {
        const result = testCacheSetJSON(constants.CACHE1, constants.KEY32, b64encode(constants.PAYLOAD+"32"))
    });
*/

    test('should retrieve the cache list of keys', async () => {
        const result = await testCacheNameKeysGet(constants.CACHE1).then(result => result.keys.length);

        expect(result).toBeGreaterThan(0);
    });
})