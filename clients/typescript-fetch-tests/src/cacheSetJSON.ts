import { CacheApi } from 'akka-cache';
import { cfg } from './configuration';
import { log, error } from "console";

export async function testCacheSetJSON(cacheName:string, key:string, payload:string, ttlSeconds?:number):Promise<void>  {
    const cacheAPI = new CacheApi(cfg);
    try {
        return cacheAPI.setPost({
            cacheRequest: {
                cacheName: cacheName, key: key, ttlSeconds: ttlSeconds, value: payload
            }
        })
        .then(nil => {
            log("cache SET successful for " + key);
            return nil;
        })

    } catch (err) {
        error('Error thrown : ', err);
        return err;
    }
}