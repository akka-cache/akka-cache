import {testCacheNameKeysGet} from "../src/cacheNameKeysGet";
import * as constants from "../src/constants";
import {testCacheSetJSON} from "../src/cacheSetJSON";
import {Buffer} from "buffer";
import {error, log} from "console";
/*
const b64encode = (str: string):string => Buffer.from(str, 'binary').toString('base64');

beforeAll(done => {
    expect(testCacheSetJSON(constants.CACHE1, constants.KEY31, b64encode(constants.PAYLOAD+"31"))).resolves.toBe(undefined);
    expect(testCacheSetJSON(constants.CACHE1, constants.KEY32, b64encode(constants.PAYLOAD+"32"))).resolves.toBe(undefined);
    done();
});
*/

describe('testing cache get keys', () => {
    test('should retrieve the cache list of keys', done => {
        expect(testCacheNameKeysGet(constants.CACHE1)
            .then(result => {
                log("key count " + result.keys.length);
                return result.keys.length;
            })
            .catch(err => {
                error("an error occurred w/ testCacheNameKeysGet: ", err.message  );
                return err;
            })
        ).resolves.toBeGreaterThan(0);
        done();
    });
})