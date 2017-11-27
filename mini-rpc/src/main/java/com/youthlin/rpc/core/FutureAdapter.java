package com.youthlin.rpc.core;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 哈, 这算不算一个 SettableFuture.
 * <p>
 * 创建: youthlin.chen
 * 时间: 2017-11-27 15:29
 */
@SuppressWarnings("WeakerAccess")
public class FutureAdapter<V> implements Future<V> {
    private V value;
    private long timeout;//ms
    private Throwable exception;
    private AtomicBoolean done = new AtomicBoolean(false);

    public void setValue(V value) {
        this.value = value;
        done.set(true);
    }

    public void setException(Throwable exception) {
        this.exception = exception;
        done.set(true);
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return done.get();
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        while (!done.get()) {//自旋等待
            //异常了
            if (exception != null) {
                throw new ExecutionException(exception);
            }
        }
        return value;
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        long nanos = unit.toNanos(timeout);
        long start = System.nanoTime();
        while (!done.get()) {//自旋等待
            //异常了
            if (exception != null) {
                throw new ExecutionException(exception);
            }
            //超时了
            if (System.nanoTime() - start > nanos) {
                throw new TimeoutException(timeout + " " + unit);
            }
        }
        return value;
    }

}
