
import { CacheNameApi } from 'akka-cache';
import { cfg } from './configuration';
import { log, error } from "console";

export async function testPutCacheName(cacheName:string, description:string) : Promise<void>  {
    const cacheNameAPI = new CacheNameApi(cfg);

    try {
        return cacheNameAPI.cacheNamePut({
            cacheNameCreate: {cacheName: cacheName, description: description}
        })
        .then(nil => {
            log("cacheNamePut successful for " + cacheName);
            return nil;
        })

    } catch (err) {
        error('Error thrown : ', err);
    }
}