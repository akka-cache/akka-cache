import { CacheNameApi } from 'akka-cache';
import { cfg } from './configuration';
import { CacheNameResponse } from 'akka-cache'
import { log, error } from "console";

export async function testGetCacheName(cacheName:string) : Promise<CacheNameResponse>  {
    const cacheNameAPI = new CacheNameApi(cfg);

    try {
        return cacheNameAPI.cacheNameCacheNameGet(
            {cacheName: cacheName}
        )
        .then(result => {
            log("cacheNameGet successful for " + result.cacheName + "," + result.description);
            return result;
        })
        .catch(err => {
            error("an error occurred: ", err.message  );
            return err;
        })

    } catch (error) {
        console.error('Error thrown : ', error);
    }
}