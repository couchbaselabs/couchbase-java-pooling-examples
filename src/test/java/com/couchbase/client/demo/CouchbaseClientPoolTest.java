package com.couchbase.client.demo;

import com.couchbase.client.CouchbaseClient;
import com.couchbase.client.CouchbaseConnectionFactoryBuilder;
import org.junit.Test;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class CouchbaseClientPoolTest {

    CouchbaseClientPool testPool;

    @org.junit.Before
    public void setUp() throws Exception {

        List<URI> baseList = Arrays.asList(
                URI.create("http://192.168.1.201:8091/pools"),
                URI.create("http://192.168.0.202:8091/pools"));

        CouchbaseConnectionFactoryBuilder builder = new CouchbaseConnectionFactoryBuilder();
        builder.setOpTimeout(200);  // just an example

        testPool = new CouchbaseClientPool(5, builder, "named", "named", baseList);
        testPool.init();



    }

    @Test
    public void runTests() {
        for (int i = 0; i < 100; i++) {
            new Thread() {
                public void run() {
                    CouchbaseClient mine = testPool.getClient();  // get a client object and use it over the course of the method

                    mine.set(Thread.currentThread().getName(), 600, "foo");
                    assertTrue(((String) mine.get(Thread.currentThread().getName())).contentEquals("foo"));
                    System.err.println("Using client " + mine.hashCode()); //sanity, see that they're different in the console
                }
            }.start();
        }
    }

    @org.junit.After
    public void tearDown() throws Exception {

        testPool.shutdown(10, TimeUnit.SECONDS);


    }
}