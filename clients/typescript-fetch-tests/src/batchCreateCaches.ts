import {BatchApi, BatchPostRequest} from 'akka-cache';
import { cfg } from './configuration';
import { log, error } from "console";
import { BatchCacheResponse } from 'akka-cache';

export async function testBatchCreateCache(batchRequestParameters: BatchPostRequest) : Promise<BatchCacheResponse>  {
    const batchAPI = new BatchApi(cfg);

    try {
        return batchAPI.batchPost(batchRequestParameters)
            .then(response => {
                log("batchPost successful ");
                return response;
            })
            .catch(err => {
                error("an error occurred w/ batchPost: ", err.message  );
                return err;
            })
    } catch (err) {
        error('Error thrown : ', err);
    }
}