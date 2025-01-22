import { CacheApi } from 'akka-cache';
import { cfg } from './configuration';
import * as constants from './constants';
import { CacheGetResponse } from 'akka-cache'
import { log, error } from "console";

export async function testGetCacheJSON(cacheName:string, key:string) : Promise<CacheGetResponse>  {
    const cacheAPI = new CacheApi(cfg);

    try {
        return cacheAPI.getCacheNameKeyGet(
            {cacheName: cacheName, key: key}
        )
        .then(response => {
            log("cacheGet successful for " + response.cacheName + "," + response.key + "," + response.value);
            return response;
        })
        .catch(err => {
            error("an error occurred: ", err.message  );
            return err;
        })

    } catch (error) {
        console.error('Error thrown : ', error);
    }
}