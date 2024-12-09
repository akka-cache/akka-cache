---
sidebar_position: 3
---

# Common Caching Strategies

Now that we know the key choices you need to make when implementing a caching strategy, let's review some popular caching patterns. For each pattern, we will describe the pattern, the choices the pattern makes on the three questions above, and when you may want to use that pattern.

...

## Local Browser Caching

The first, and perhaps simplest, caching strategy is local browser caching.

...

## Local Backend Caching

A second caching strategy is another local strategy. With local backend caching, your backend server instances may cache network responses or intermediate data from other systems.

...

## Read-Aside Caching

The third, and most common, caching strategy is read-aside caching (commonly called "lazy loading").

...

## Write-Aside Caching

The next caching strategy is similar to the previous. With write-aside caching, we are using a centralized, aside cache like with read-aside caching.

...

## Read-Through and Write-Through Caching

The final two strategies are read-through and write-through caching. These two strategies are unique in that all data access goes through the cache directly. 

...