import { testCreateCacheName } from '../src/cacheNameCreate';
import { testGetCacheName } from "../src/cacheNameGet";
import { testDeleteCacheName } from "../src/cacheNameDelete";
import * as constants from '../src/constants';
import {error, log} from "console";

/*beforeAll(done => {
    expect(testCreateCacheName(constants.CACHE1, constants.DESCRIPTION1)).resolves.toBe(undefined);
    done();
});*/

describe('testing cacheName Create, Get, & Delete', () => {
    test('create should be successful', async ()=> {
        const result = await testCreateCacheName(constants.CACHE1, constants.DESCRIPTION1);
        log(JSON.stringify(result));
        expect(result).toBe(undefined);
    });
    test('get should be successful',async () => {
        const result = await testGetCacheName(constants.CACHE1);
        expect(result).toStrictEqual( {"cacheName": "cache1", "deleted": false, "description": constants.DESCRIPTION1 });
    });
    test('delete should be successful', async () => {
        const result = await testDeleteCacheName(constants.CACHE1);
        expect(result).toBe(undefined);
    });
    // NOTE: since deleting the cacheName triggers a potentially long workflow we can't easily make sure the cacheName was successfully deleted.
});

