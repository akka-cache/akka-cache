import { testCreateCacheName } from '../src/cacheNameCreate';
import { testGetCacheName } from "../src/cacheNameGet";
import { testFlushCacheName } from "../src/cacheNameFlush";
import * as constants from '../src/constants';
import {error, log} from "console";
import {testCacheSetJSON} from "../src/cacheSetJSON";
import {Buffer} from "buffer";
import {DESCRIPTION1} from "../src/constants";

const b64encode = (str: string):string => Buffer.from(str, 'binary').toString('base64');

describe('testing cacheName Create, Cache, Flush, and Get', () => {
    test('flush should be successful', async () => {
        const result = await testFlushCacheName(constants.CACHE1);
        expect(result).toBe(undefined);
    });
    test('get should be successful',async () => {
        const result = await testGetCacheName(constants.CACHE1);
        expect(result).toStrictEqual( {"cacheName": "cache1", "deleted": false, "description": constants.DESCRIPTION1 });
    });
});

