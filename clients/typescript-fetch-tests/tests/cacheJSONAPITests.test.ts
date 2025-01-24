import { testCacheSetJSON } from "../src/cacheSetJSON"
import {testGetCacheJSON} from "../src/cacheGetJSON";
import * as constants from "../src/constants"
import {testDeleteCache} from "../src/cacheDelete";
import { Buffer } from "buffer";

const b64decode = (str: string):string => Buffer.from(str, 'base64').toString('binary');
const b64encode = (str: string):string => Buffer.from(str, 'binary').toString('base64');

beforeAll(done => {
    expect(testCacheSetJSON(constants.CACHE1, constants.KEY1, constants.PAYLOAD+"1")).resolves.toBe(undefined);
    // @ts-ignore
    done();
});

afterAll(done => {
    expect(testDeleteCache(constants.CACHE1, constants.KEY1)).resolves.toBe(undefined);
    done();
});

describe('testing cacheSET (JSON) API', () => {
    test('should retrieve the cache successfully', done => {
        // NOTE: the org from the bearer token is used to prefix the return cacheName
        expect(testGetCacheJSON(constants.CACHE1, constants.KEY1)).resolves.toStrictEqual( {"cacheName": constants.ORG+constants.CACHE1, "key": constants.KEY1, "success": true, "value": constants.PAYLOAD + "1"});
        done();
    });
});

describe('testing cacheSET (JSON) API with BASE64', () => {
    test('should create a base64 encoded cache and then retrieve successfully', done => {
        // NOTE: the org from the bearer token is used to prefix the return cacheName
        var payload = constants.PAYLOAD+"X2XX";
        var b64Payload = b64encode(payload);
        expect(testCacheSetJSON(constants.CACHE1, constants.KEY2, b64Payload)).resolves.toBe(undefined);
        expect(testGetCacheJSON(constants.CACHE1, constants.KEY2)).resolves.toMatchObject({"cacheName": constants.ORG+constants.CACHE1, "key": constants.KEY2, "success": true, "value": payload});
        done();
    });
});