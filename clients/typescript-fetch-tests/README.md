# Testing Akka Cache OpenAPI 3.0.1 generated typescript-fetch client SDK

Clone and run the akka-cache locally. From the backend directory execute the following in the terminal: 
```
mvn clean compile exec:java
```

## Node Setup
> **_NOTE:_** this separate project should be created with the OpenAPI typescript-fetch generator using the akka-cache-jwt-v3.0.1.yaml with the following settings:
```
npmName = akka-cache
npmVersion = 0.1.0
```
1.From client SDK dir issue:
```
$ npm link
```
2. From this Project's root issue:
```
$ npm link akka-cache
```

3. Run Unit Tests

All tests can be run at once with:
```
$ npm test
```

To run tests individually, you can use any of the following examples:

```
$ npm test tests/cacheNameAPITests.test.ts
$ npm test tests/cacheBinaryAPITests.test.ts
$ npm test tests/cacheJSONAPITests.test.ts
$ npm test tests/cacheJSONAPITTLTest.test.ts
$ npm test tests/batchAPITests.test.ts
$ npm test tests/cacheNameKeysTest.test.ts
```

