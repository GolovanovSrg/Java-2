package ru.spbau.mit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by golovanov on 10.09.16.
 */
public class LazyFactoryTest {

    @org.junit.Test
    public void createOneThreadLazy() throws Exception {
        final Lazy<Double> lazyObj = LazyFactory.createOneThreadLazy(() -> Math.random());

        Double firstValue = lazyObj.get();
        assertEquals(firstValue, lazyObj.get());
    }

    private <T> void testMultiThreadObj(final Lazy<T> obj, final int nThreads) {
        List<T> threadValues = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < nThreads; i++) {
            Thread currentThread = new Thread(() -> threadValues.add(obj.get()));
            currentThread.start();
        }

        while (threadValues.size() != nThreads) {}

        for (T val : threadValues) {
            assertEquals(val, threadValues.get(0));
        }
    }

    @org.junit.Test
    public void createMultiThreadLazy() throws Exception {
        final Lazy<Double> lazyObj = LazyFactory.createMultiThreadLazy(() -> Math.random());
        final int nThreads = 1000;
        testMultiThreadObj(lazyObj, nThreads);
    }

    @org.junit.Test
    public void createLockFreeLazy() throws Exception {
        final Lazy<Double> lazyObj = LazyFactory.createLockFreeLazy(() -> Math.random());
        final int nThreads = 1000;
        testMultiThreadObj(lazyObj, nThreads);
    }

}