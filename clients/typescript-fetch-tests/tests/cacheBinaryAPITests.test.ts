import * as constants from "../src/constants"
import {testCachePostBinary} from "../src/cacheCreateBinaryPOST";
import {testGetCacheBinary} from "../src/cacheGetBinary";
import {error, log} from "console";

async function fetchBlobByURL(url) {
    const response = fetch(url);
    return response.then(response => response.blob());
}

var payload = constants.PAYLOAD+"3";

describe('testing cacheSET (Binary) API', () => {
    test('should create the cache successfully', async () => {
        var blob = new Blob([payload], { type: "text/plain" });

        const result = await testCachePostBinary(constants.CACHE1, constants.KEY3, blob);

        expect(result).toBe(undefined);
    });

    test('should retrieve the cache successfully', async () => {
        const blob = await testGetCacheBinary(constants.CACHE1, constants.KEY3);

        const result = await blob.text();

        expect(result).toBe(payload);
    });

});

describe('testing cacheSET (Binary) API with images', () => {

   test('should retrieve the image from the internet and cache it', async () => {

       const blob = await fetchBlobByURL("https://www.wsupercars.com/wallpapers-regular/Aston-Martin/2022-Aston-Martin-DBR22-Concept-001-1536.jpg");
       log("image type:", blob.type);

       const result = await testCachePostBinary(constants.CACHE1, constants.CAR, blob);

       expect(result).toBe(undefined);

   });

});
