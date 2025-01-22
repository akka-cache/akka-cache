import { CacheApi } from 'akka-cache';
import {cfg} from "./configuration";
import {error, log} from "console";

export async function testGetCacheBinary(cacheName:string, key:string) : Promise<Blob>  {
    const cacheAPI = new CacheApi(cfg);

    try {
        return cacheAPI.cacheNameKeyGet(
            {cacheName: cacheName, key: key}
        )
        .then(response => {
            log("cacheGet successful for " + cacheName + "," + key + "," + response.bytes());
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