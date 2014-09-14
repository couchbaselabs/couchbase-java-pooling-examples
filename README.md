couchbase-java-pooling-examples
===============================

Examples of how to do object pooling of Java Couchbase Clients.

Note that in general, Couchbase does <b>not</b> recommend using object pools.  While there is a known performance
advantage owing to the multiple TCP buffers and memory used, the advantage isn't usually worth the extra complexity.

This example is usually used when trying to diagnose issues (i.e. prove that the client is not a bottleneck).
