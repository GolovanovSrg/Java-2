package ru.spbau.mit;

import ru.spbau.mit.Lazy;
import ru.spbau.mit.LazyFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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

    private <T> void testMultiThreadObj(final Lazy<T> obj, final int nThreads) throws InterruptedException {
        List<T> threadValues = Collections.synchronizedList(new ArrayList<>());

        ExecutorService service = Executors.newFixedThreadPool(nThreads);

        int nTasks = 1000;
        for (int i = 0; i < nTasks; i++) {
            service.submit(() -> threadValues.add(obj.get()));
        }

        service.shutdown();
        service.awaitTermination(5, TimeUnit.SECONDS);

        long countGoodValues = threadValues.stream()
                                           .filter((val) -> val.equals(threadValues.get(0)))
                                           .count();

        assertEquals(countGoodValues, nTasks);
    }

    @org.junit.Test
    public void createMultiThreadLazy() throws Exception {
        final Lazy<Double> lazyObj = LazyFactory.createMultiThreadLazy(() -> Math.random());
        final int nThreads = 3;
        testMultiThreadObj(lazyObj, nThreads);
    }

    @org.junit.Test
    public void createLockFreeLazy() throws Exception {
        final Lazy<Double> lazyObj = LazyFactory.createLockFreeLazy(() -> Math.random());
        final int nThreads = 3;
        testMultiThreadObj(lazyObj, nThreads);
    }

}