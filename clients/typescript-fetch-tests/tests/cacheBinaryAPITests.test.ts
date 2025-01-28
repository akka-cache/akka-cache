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
    test('should create the cache successfully', done => {
        var blob = new Blob([payload], { type: "text/plain" });
        // NOTE: the org from the bearer token is used to prefix the return cacheName
        expect(testCachePostBinary(constants.CACHE1, constants.KEY3, blob)).resolves.toBe(undefined);
        done();
    });

    test('should retrieve the cache successfully', done => {
        expect(
            testGetCacheBinary(constants.CACHE1, constants.KEY3).then(result => {return result.text()})
        ).resolves.toBe(payload);
        done();
    });

    /* This is the same test as the previous as async
        test('should retrieve the cache successfully', async () => {
            const data = await testGetCacheBinary(constants.CACHE1, constants.KEY3);
            expect(data.text()).resolves.toEqual(payload);
        });
    */

});

describe('testing cacheSET (Binary) API with images', () => {

   test('should retrieve the image from the internet and cache it', done => {
       expect(
           fetchBlobByURL("https://www.wsupercars.com/wallpapers-regular/Aston-Martin/2022-Aston-Martin-DBR22-Concept-001-1536.jpg")
               .then(image => {
                   log("image type:", image.type);
                   return testCachePostBinary(constants.CACHE1, constants.CAR, image)
               })
       )
       .resolves.toBe(undefined);
       done();

   });

});
