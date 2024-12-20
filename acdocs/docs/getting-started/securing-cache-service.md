---
title: Securing your Cache Service
sidebar_position: 4
---

In the previous guide we deployed a version of Akka Cache that was open to the internet and did not enforce any security constraints. While that made it easy for experimenting and testing, it's definitely not the security posture we want for real applications. In this guide, you'll go through the process of securing the Akka Cache service and consuming it securely using JWT bearer tokens.

## Deploy a Secure Version of Akka Cache
In the previous guide, we deployed an image of Akka Cache with the tag `insecure`. For this guide you can either choose to deploy the secure version in place of the previous, or you can choose to name it something else and deploy both alongside each other. In any case, you will want to deploy the secure service from an accessible container registry:

```
akka services deploy akka-cache-secure {registry host}/{repo}:{tag}
```

If you're deploying as a new service, you'll also need to go through the process of adding a host and creating a route to this new service (as usual, your host name will not match what's in the guide):

```
akka project hostname add
akka route create akka-cache-secure --path /=akka-cache-secure --hostname new-demo-1.gcp-us-east1.akka.services
```

:::info

There are actually two endpoints in this service: the `CacheEndpoint` and the `CacheJWTEndpoint`. In the previous guide we were communicating with the `CacheEndpoint` directly, which is no longer allowed. This endpoint is now secured with the ACL for service-to-service calls. To communicate with the service in this guide, we'll be using the JWT endpoint, which has a slightly different syntax for its routes and enforces a specific issuer on the tokens.

:::


## Create a JWT Key and Secret
Now that we've got our secure service deployed and available at a public route, we need to set up JWT authentication. The first step here is to create a [JWT key](https://doc.akka.io/security/jwts.html) bound to our service. This is accomplished by creating a _JWT key_ and a corresponding _secret_, both of which can be created at the same time. The JWT key (not to be confused with an actual token) holds a key ID and a valid issuer and refers to a secret. This combined set of data is used by Akka to validate incoming requests to your service.

The current version of the Akka Cache service requires a fixed issuer of `https://session.firebase.google.com/akka-cache`, but this may change to make sure you check the latest `README`.


First, let's make sure that there are no existing JWT keys:

```
akka services jwt list akka-cache-secure
```

Use the following command to create a new JWT key called `cachekey` with an issuer of `https://session.firebase.google.com/akka-cache`, and a secret (private key) of the same name:

```
akka services jwt generate akka-cache-secure --key-id cachekey --issuer https://session.firebase.google.com/akka-cache --algorithm ES256
```

Now let's run a few query commands to examine our new JWT key and secret:

```
$ akka services jwt list akka-cache-secure
KEY ID      ALGORITHM   ISSUER                                           
cachekey    ES256       https://session.firebase.google.com/akka-cache  

$ akka secret get cachekey -o json
```

The JSON that you receive as the output of querying the key will have two fields, `public.key` and `private.key`. These are base64 encoded strings. When you decode them, they will have the familiar `-----BEGIN/END PUBLIC/PRIVATE KEY-----` lines. With the decoded versions of the keys in hand (you can use a terminal command to decode or any number of online base64 decoders), it's time to create a token for clients to use to interact with your service.

:::note

In this example we're using the asymmetric algorithm `ES256`. The JWT specification (and therefore Akka) support many different asymmetric and symmetric algorithms. To see which ones the current version of Akka supports, you can run `akka services jwt list-algorithms` 

:::

## Issue a Client JWT
To illustrate that the tokens used by Akka's JWT authentication are not proprietary, we'll use the [jwt.io](https://jwt.io) site to generate our token manually. Open up the site and enter the following JSON into the `payload` field (the site can be finicky about live editing, so it might be easier to create the JSON elsewhere and then paste it):

```json
{
  "iss": "https://session.firebase.google.com/akka-cache",
  "iat": 1734630582,
  "exp": 1766166583,
  "aud": "www.example.com",
  "sub": "akka-cache-secure",
  "org": "sample",
  "user_id": "sample",
  "serviceLevel": "FREE",
  "service_tier": "FREE"
}
```

Depending on the version of Akka Cache you're deploying, some of these fields may no longer be necessary, but it will work just fine if you leave the extra ones in place. The most important field here is `iss`, which must match _exactly_ the issuer you supplied when you created the JWT key used by the service.

Now paste the _decoded_ versions of the public and private keys into the corresponding spots on the right and use the following JSON for the `header` field:

```json
{
  "typ": "JWT",
  "alg": "ES256"
}
```

If you've supplied a valid public and private key and valid JSON for the payload, you should notice that any changes you make to the JSON on the right trigger a re-generation of the token on the left. Once you're satisfied with the token, copy it to your clipboard for use in the next step.

## Securely Perform Cache Actions
To make a request of a service that requires JWT authentication, all you need to do is include the JWT as a `Bearer` token in the `Authorization` HTTP header. We can use the following `curl` command to create a new cache value within the cache named `secure1` in the key `securedata` (note that this data is base64 encoded in the JSON input):


```
curl -i -X POST -H "Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NiJ9.eyJpc3MiOiJodHRwczovL3Nlc3Npb24uZmlyZWJhc2UuZ29vZ2xlLmNvbS9ha2thLWNhY2hlIiwiaWF0IjoxNzM0NjMwNTgyLCJleHAiOjE3NjYxNjY1ODMsImF1ZCI6Ind3dy5leGFtcGxlLmNvbSIsInN1YiI6ImFra2EtY2FjaGUtc2VjdXJlIiwib3JnIjoic2FtcGxlIiwidXNlcl9pZCI6InNhbXBsZSIsInNlcnZpY2VMZXZlbCI6IkZSRUUiLCJzZXJ2aWNlX3RpZXIiOiJGUkVFIn0.1fLiq8n9mn9PunEONwxE_3JM86yuAg7W6UKfPgcDICN1zQaI-IkAm-pa08uG8Fs_8ZvuqaZimkQvK8zlrow9ZQ" -H "Content-Type: application/json" https://yellow-bonus-0803.gcp-us-east1.akka.services/set -d '{"cacheName": "secure1", "key": "securedata", "value": "aGVsbG8gd29ybGQ="}'
HTTP/2 201 
content-length: 0
date: Fri, 20 Dec 2024 14:25:56 GMT
server: envoy
x-envoy-upstream-service-time: 165
```

Now we can reuse that same token to make a `GET` request of the service. Note that the URL here is `/secure1/securedata`, which obtains the raw data. If we want the cache item's metadata, then we would prefix this URL path with `/get`.

```
curl -i -H "Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NiJ9.eyJpc3MiOiJodHRwczovL3Nlc3Npb24uZmlyZWJhc2UuZ29vZ2xlLmNvbS9ha2thLWNhY2hlIiwiaWF0IjoxNzM0NjMwNTgyLCJleHAiOjE3NjYxNjY1ODMsImF1ZCI6Ind3dy5leGFtcGxlLmNvbSIsInN1YiI6ImFra2EtY2FjaGUtc2VjdXJlIiwib3JnIjoic2FtcGxlIiwidXNlcl9pZCI6InNhbXBsZSIsInNlcnZpY2VMZXZlbCI6IkZSRUUiLCJzZXJ2aWNlX3RpZXIiOiJGUkVFIn0.1fLiq8n9mn9PunEONwxE_3JM86yuAg7W6UKfPgcDICN1zQaI-IkAm-pa08uG8Fs_8ZvuqaZimkQvK8zlrow9ZQ" -H "Content-Type: application/json" https://yellow-bonus-0803.gcp-us-east1.akka.services/secure1/securedata
HTTP/2 200 
content-type: application/octet-stream
content-length: 11
date: Fri, 20 Dec 2024 14:27:12 GMT
server: envoy
x-envoy-upstream-service-time: 21

hello world
```

Because Akka Cache was created by composing Akka components, we can actually look at the `CacheEntity`'s data by clicking on it in the Akka console, as shown in the following screenshot:

![screenshot of cache entity](./cache_entity_data.png)

