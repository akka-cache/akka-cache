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

3. Run Tests
```
$ npx jest
```

