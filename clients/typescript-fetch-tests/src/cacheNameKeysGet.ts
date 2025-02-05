import { CacheNameApi } from 'akka-cache';
import { cfg } from './configuration';
import { CacheNameResponse } from 'akka-cache'
import { log, error } from "console";

export async function testGetCacheNameKeysGet(cacheName:string) : Promise<string[]>  {
    const cacheNameAPI = new CacheNameApi(cfg);

    try {
        return cacheNameAPI.cacheNameCacheNameKeysGet(
            {cacheName: cacheName}
        )
        .then(result => {
            log("cacheNameCacheNameKeysGet successful for " + result);
            return result;
        })
        .catch(err => {
            error("an error occurred w/ cacheNameCacheNameKeysGet: ", err.message  );
            return err;
        })

    } catch (error) {
        console.error('Error thrown : ', error);
    }
}