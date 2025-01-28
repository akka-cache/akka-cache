import { CacheNameApi } from 'akka-cache';
import { cfg } from './configuration';
import { log, error } from "console";

export async function testDeleteCacheName(cacheName:string) : Promise<void>  {
    const cacheNameAPI = new CacheNameApi(cfg);

    try {
        return cacheNameAPI.cacheNameCacheNameDelete(
            {cacheName: cacheName}
        )
        .then(result => {
            log("cacheNameDelete successful.");
            return result;
        })

    } catch (err) {
        error('Error thrown : ', err);
    }
}