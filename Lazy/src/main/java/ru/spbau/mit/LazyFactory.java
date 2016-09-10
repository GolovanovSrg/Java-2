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
        private final Supplier<T> sup;
        private SupplierResult<T> result = new SupplierResult<>();

        OneThreadLazy(Supplier<T> sup) {
            this.sup = sup;
        }

        public T get() {
            if (!result.isDone) {
                result.value = sup.get();
                result.isDone = true;
            }

            return result.value;
        }
    }

    private static class MultiThreadLazy<T> implements Lazy<T> {
        private final Supplier<T> sup;
        private volatile SupplierResult<T> result = new SupplierResult<>();

        MultiThreadLazy(Supplier<T> sup) {
            this.sup = sup;
        }

        public T get() {
            if (!result.isDone) {
                synchronized (MultiThreadLazy.class) {
                    if (!result.isDone) {
                        result.value = sup.get();
                        result.isDone = true;
                    }
                }
            }

            return result.value;
        }
    }

    private static class LockFreeLazy<T> implements Lazy<T> {
        private final Supplier<T> sup;
        private AtomicReference<SupplierResult<T>> result = new AtomicReference<>(null);

        LockFreeLazy(Supplier<T> sup) {
            this.sup = sup;
        }

        public T get() {
            if (result.get() == null) {
                SupplierResult<T> curResult = new SupplierResult<>(sup.get(), true);
                result.compareAndSet(null, curResult);
            }

            return result.get().value;
        }
    }

    public static <T> Lazy<T> createOneThreadLazy(Supplier<T> sup) {
        return new OneThreadLazy<T>(sup);
    }

    public static <T> Lazy<T> createMultiThreadLazy(Supplier<T> sup) {
        return new MultiThreadLazy<T>(sup);
    }

    public static <T> Lazy<T> createLockFreeLazy(Supplier<T> sup) {
        return new LockFreeLazy<T>(sup);
    }

}
