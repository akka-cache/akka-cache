---
sidebar_position: 2
---

# Common Caching Patterns

### Caching is Fast

With an in-memory system optimized for key-value access, you can get sub-millisecond p99 response times as measured by the client. That's fast. And because it's so fast …

...

### Caching is Fun

Nobody likes a slow website. Slow websites lead to bored users and lost sales. Developers don't want to work on a slow website or deal with unhappy users.

...

## Caching Choices

Before we discuss specific caching patterns you may use in your application, let's discuss some of the key choices you'll need to make when adding caching to your application.

...

### Local vs Remote Caching

The first caching choice you need to make is on where to cache your data.

...

### Read vs Write Caching

The second caching choice to consider is when to cache the data. Again, you have two choices -- cache the data when it is read the first time (often called "lazy-loading"), or cache the data when it is written.

...

### Inline vs Aside Caching

The final consideration in choosing your caching strategy is whether to use an inline cache or a side cache.

...