import { BatchApi } from 'akka-cache';
import { cfg } from './configuration';
import { log, error } from "console";
import { BatchGetPostRequest } from 'akka-cache';
import { BatchGetCacheResponse } from 'akka-cache';
export async function testBatchGetCache(batchRequestParameters: BatchGetPostRequest) : Promise<BatchGetCacheResponse>  {
    const batchAPI = new BatchApi(cfg);

    try {
        return batchAPI.batchGetPost(batchRequestParameters)
            .then(response => {
                log("batchPost successful ");
                return response;
            })
            .catch(err => {
                error("an error occurred w/ batchGetPost: ", err.message  );
                return err;
            })
    } catch (err) {
        error('Error thrown : ', err);
    }
}