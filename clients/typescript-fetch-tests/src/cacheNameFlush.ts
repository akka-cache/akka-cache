import { CacheNameApi } from 'akka-cache';
import { cfg } from './configuration';
import { log, error } from "console";

export async function testFlushCacheName(cacheName:string) : Promise<void>  {
    const cacheNameAPI = new CacheNameApi(cfg);

    try {
        return cacheNameAPI.cacheNameCacheNameFlushPut(
            {cacheName: cacheName}
        )
        .then(result => {
            log("cacheNameFlush successful.");
            return result;
        })

    } catch (err) {
        error('Error thrown : ', err);
    }
}