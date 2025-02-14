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
    test('create should be successful', async ()=> {
        const result = await testCreateCacheName(constants.CACHE1, constants.DESCRIPTION1);
        log(JSON.stringify(result));
        expect(result).toBe(undefined);
    });
    test('should create eleven caches' , async ()=> {
        const result1 = await testCacheSetJSON(constants.CACHE1, constants.KEY1, b64encode(constants.DESCRIPTION1));
        expect(result1).toBe(undefined);
        const result2 = await testCacheSetJSON(constants.CACHE1, constants.KEY2, b64encode(constants.DESCRIPTION2));
        expect(result2).toBe(undefined);
        const result3= await testCacheSetJSON(constants.CACHE1, constants.KEY3, b64encode(constants.DESCRIPTION3));
        expect(result3).toBe(undefined);
        const result4= await testCacheSetJSON(constants.CACHE1, constants.KEY4, b64encode(constants.DESCRIPTION4));
        expect(result4).toBe(undefined);
        const result5= await testCacheSetJSON(constants.CACHE1, constants.KEY5, b64encode(constants.DESCRIPTION5));
        expect(result5).toBe(undefined);
        const result6= await testCacheSetJSON(constants.CACHE1, constants.KEY6, b64encode(constants.DESCRIPTION6));
        expect(result6).toBe(undefined);
        const result7= await testCacheSetJSON(constants.CACHE1, constants.KEY7, b64encode(constants.DESCRIPTION7));
        expect(result7).toBe(undefined);
        const result8= await testCacheSetJSON(constants.CACHE1, constants.KEY8, b64encode(constants.DESCRIPTION8));
        expect(result8).toBe(undefined);
        const result9= await testCacheSetJSON(constants.CACHE1, constants.KEY9, b64encode(constants.DESCRIPTION9));
        expect(result9).toBe(undefined);
        const result10= await testCacheSetJSON(constants.CACHE1, constants.KEY10, b64encode(constants.DESCRIPTION10));
        expect(result10).toBe(undefined);
        const result11= await testCacheSetJSON(constants.CACHE1, constants.KEY11, b64encode(constants.DESCRIPTION11));
        expect(result11).toBe(undefined);
    })
});

