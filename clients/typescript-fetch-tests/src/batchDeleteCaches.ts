import {BatchApi, BatchPostRequest} from 'akka-cache';
import { cfg } from './configuration';
import { log, error } from "console";
import { BatchDeleteRequest } from 'akka-cache';
import { BatchDeleteCacheResponse } from 'akka-cache';
export async function testBatchDeleteCache(batchRequestParameters: BatchDeleteRequest) : Promise<BatchDeleteCacheResponse>  {
    const batchAPI = new BatchApi(cfg);

    try {
        return batchAPI.batchDelete(batchRequestParameters)
            .then(response => {
                log("batchDelete successful ");
                return response;
            })
            .catch(err => {
                error("an error occurred w/ batchDelete: ", err.message  );
                return err;
            })
    } catch (err) {
        error('Error thrown : ', err);
    }
}