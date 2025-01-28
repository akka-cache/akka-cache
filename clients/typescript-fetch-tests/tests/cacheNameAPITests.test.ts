import { testCreateCacheName } from '../src/cacheNameCreate';
import { testGetCacheName } from "../src/cacheNameGet";
import { testDeleteCacheName } from "../src/cacheNameDelete";
import * as constants from '../src/constants';

describe('testing cacheNameCreate', () => {
    test('should be successful', done => {
        expect(testCreateCacheName(constants.CACHE1, constants.DESCRIPTION1)).resolves.toBe(null);
        done();
    });
});

describe('testing cacheNameGet', () => {
    test('should be successful', done => {
        expect(testGetCacheName(constants.CACHE1)).resolves.toStrictEqual( {"cacheName": "ttorgcache1", "deleted": false, "description": constants.DESCRIPTION1 });
        done();
    });
});

describe('testing cacheNameDelete', () => {
    test('should be successful', done => {
        expect(testDeleteCacheName(constants.CACHE1)).resolves.toBe(null);
        done();
    });
});

describe('make sure the cacheName has been deleted', () => {
    test('cacheName should not exist', done => {
        expect(testGetCacheName(constants.CACHE1)).resolves.toBe(null);
        done();
    });
});