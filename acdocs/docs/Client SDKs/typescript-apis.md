# Typescript APIs

## Access

Endpoint Authentication is based upon Java Web Tokens (JWT) using HTTP Bearer Token authentication headers.

The following claims (or fields) are required in the JWT:
```
{
"iss": "https://session.firebase.google.com/akka-cache",
"org": "ttorg",
"name": "John Doe",
"serviceLevel": "free"
}
```

## Methods

[ Jump to [Models](#models) ]

## Table of Contents

### [CacheName](#cachename)
APIs used to maintain a cache name, namespace, or group.

### [Cache](#cache)
APIs used to create, retrieve, and delete cached objects.

### [Batch](#batch)
Batch APIs used to create, retrieve, and delete cached objects in a batch fashion.

---

# CacheName {#cachename}
APIs used to maintain a cache name, namespace, or group.

# Cache {#cache}
APIs used to create, retrieve, and delete cached objects.

# Batch {#batch}
Batch APIs used to create, retrieve, and delete cached objects in a batch fashion.

---