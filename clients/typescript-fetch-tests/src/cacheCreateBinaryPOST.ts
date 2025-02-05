import { CacheApi } from 'akka-cache';
import { cfg } from './configuration';
import { log, error } from "console";

export async function testCachePostBinary(cacheName:string, key:string, payload:Blob):Promise<void>  {
    const cacheAPI = new CacheApi(cfg);
    try {
        return cacheAPI.cacheNameKeyPost(
            {
                cacheName: cacheName, key: key, body: payload
            }
        )
        .then(nil => {
                log("cache binary POST is successful for " + key);
                return nil;
            }
        )

    } catch (err) {
        error('Error thrown : ', err);
        return err;
    }
}

export async function testCacheTTLPostBinary(cacheName:string, key:string,  ttlSeconds:number, payload:Blob):Promise<void>  {
    const cacheAPI = new CacheApi(cfg);
    try {
        return cacheAPI.cacheNameKeyTtlSecondsPost(
            {
                cacheName: cacheName, key: key, ttlSeconds, body: payload
            }
        )
        .then(nil => {
            log("cache SET successful for " + key);
            return nil;
        })

    } catch (err) {
        error('Error thrown : ', err);
        return err;
    }
}