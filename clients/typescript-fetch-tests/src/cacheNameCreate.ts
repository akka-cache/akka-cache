
import { CacheNameApi } from 'akka-cache';
import { cfg } from './configuration';
import { log, error } from "console";

export async function testCreateCacheName(cacheName:string, description:string) : Promise<void>  {
    const cacheNameAPI = new CacheNameApi(cfg);

    try {
        return cacheNameAPI.cacheNamePost({
            cacheNameCreate: {cacheName: cacheName, description: description}
        })
        .then(nil => {
            log("cacheNameCreate successful for " + cacheName);
            return nil;
        })

    } catch (err) {
        error('Error thrown : ', err);
    }
}