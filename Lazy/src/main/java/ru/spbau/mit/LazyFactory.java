package ru.spbau.mit;


import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * Created by golovanov on 10.09.16.
 */
public class LazyFactory {

    private static class SupplierResult<T> {
        T value = null;
        boolean isDone = false;

        SupplierResult() {}

        SupplierResult(T value, boolean isDone) {
            this.value = value;
            this.isDone = isDone;
        }
    }

    private static class OneThreadLazy<T> implements Lazy<T> {
        private final Supplier<T> supplier;
        private SupplierResult<T> result = new SupplierResult<>();

        OneThreadLazy(Supplier<T> supplier) {
            this.supplier = supplier;
        }

        public T get() {
            if (!result.isDone) {
                result.value = supplier.get();
                result.isDone = true;
            }

            return result.value;
        }
    }

    private static class MultiThreadLazy<T> implements Lazy<T> {
        private final Supplier<T> supplier;
        private volatile SupplierResult<T> result = new SupplierResult<>();

        MultiThreadLazy(Supplier<T> supplier) {
            this.supplier = supplier;
        }

        public T get() {
            if (!result.isDone) {
                synchronized (MultiThreadLazy.class) {
                    if (!result.isDone) {
                        result.value = supplier.get();
                        result.isDone = true;
                    }
                }
            }

            return result.value;
        }
    }

    private static class LockFreeLazy<T> implements Lazy<T> {
        private final Supplier<T> supplier;
        private AtomicReference<SupplierResult<T>> result = new AtomicReference<>(null);

        LockFreeLazy(Supplier<T> supplier) {
            this.supplier = supplier;
        }

        public T get() {
            if (result.get() == null) {
                SupplierResult<T> curResult = new SupplierResult<>(supplier.get(), true);
                result.compareAndSet(null, curResult);
            }

            return result.get().value;
        }
    }

    public static <T> Lazy<T> createOneThreadLazy(Supplier<T> supplier) {
        return new OneThreadLazy<T>(supplier);
    }

    public static <T> Lazy<T> createMultiThreadLazy(Supplier<T> supplier) {
        return new MultiThreadLazy<T>(supplier);
    }

    public static <T> Lazy<T> createLockFreeLazy(Supplier<T> supplier) {
        return new LockFreeLazy<T>(supplier);
    }

}
