import { CacheApi } from 'akka-cache';
import { cfg } from './configuration';
import { log, error } from "console";

export async function testDeleteCache(cacheName:string,key:string) : Promise<void>  {
    const cacheAPI = new CacheApi(cfg);

    try {
        return cacheAPI.cacheNameKeyDelete(
            {cacheName: cacheName, key: key}
        )
        .then(result => {
            log("cacheDelete successful.");
            return result;
        })

    } catch (err) {
        error('Error thrown : ', err);
    }
}