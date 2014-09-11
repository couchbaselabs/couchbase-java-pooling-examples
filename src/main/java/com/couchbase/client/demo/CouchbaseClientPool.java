package com.couchbase.client.demo;

import com.couchbase.client.CouchbaseClient;
import com.couchbase.client.CouchbaseConnectionFactoryBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Generic CouchbaseClient pool example.
 *
 * Note that in general, Couchbase does <b>not</b> recommend using object pools.  While there is a known performance
 * advantage owing to the multiple TCP buffers and memory used, the advantage isn't usually worth the extra complexity.
 *
 * This example is usually used when trying to diagnose issues (i.e. prove that the client is not a bottleneck).
 *
 */
public class CouchbaseClientPool {

    private ArrayList<CouchbaseClient> clientList;
    private int size;
    private String bucketname;
    private String password;
    private final CouchbaseConnectionFactoryBuilder connFactBuilder;
    private boolean allDown = false;
    private List<URI> baseList;
    protected AtomicInteger nextPosition = new AtomicInteger(0);  // protected so I can show it on the console when testing

    /**
     * Create a new object pool.
     *
     * Note that thread safety can be a problem here.  Ensure that the CouchbaseConnectionFactoryBuilder passed in is
     * not changed by any other threads after it's passed to the ctor.
     *
     * @param connFactBuilder
     * @param bucketname
     * @param password
     * @param baseList
     */
    public CouchbaseClientPool(int size, CouchbaseConnectionFactoryBuilder connFactBuilder, String bucketname, String password, List<URI> baseList) {
        this.connFactBuilder = connFactBuilder;
        this.bucketname = bucketname;
        this.password = password;
        this.size = size;
        this.baseList = baseList;

    }

    /**
     * Initialize the clients in the pool.
     */
    public void init() throws IOException {
        clientList = new ArrayList<CouchbaseClient>(size);
        for (int i=0; i<size; i++) {
            clientList.add(i, new CouchbaseClient(connFactBuilder.buildCouchbaseConnection(baseList, bucketname, password)));
        }
    }

    /**
     * Shut down all clients in the pool.
     *
     * Note that shutdown of a client object in use by another thread is generally safe, but will lead to exceptions
     * or other errors in other threads.  This is a concern of the application developer when using this class.
     */
    public void shutdown(long timeout, TimeUnit unit) {
        for (int i=0; i<size; i++) {
            clientList.get(i).shutdown(timeout, unit);
        }
    }

    public CouchbaseClient getClient() {
        int next = (nextPosition.incrementAndGet() % size);
        if (next>1000000) {
            synchronized(nextPosition) {
                nextPosition.set(0); // don't grow forever
            }
        }
        return clientList.get(next%size);
    }

}
