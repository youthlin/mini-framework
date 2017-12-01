package com.youthlin.rpc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 哈, 这算不算一个 SettableFuture.
 * <p>
 * 创建: youthlin.chen
 * 时间: 2017-11-27 15:29
 */
@SuppressWarnings("WeakerAccess")
public class FutureAdapter<V> implements Future<V> {
    private static final Logger LOGGER = LoggerFactory.getLogger(FutureAdapter.class);
    private V value;
    private Throwable exception;
    private AtomicBoolean done = new AtomicBoolean(false);
    private ReentrantLock lock = new ReentrantLock();
    private Condition condition = lock.newCondition();

    private void waitResult(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        lock.lock();
        try {
            LOGGER.trace("wait {}", lock);
            if (!condition.await(timeout, unit)) {
                throw new TimeoutException(timeout + " " + unit.toString().toLowerCase());
            }
            LOGGER.trace("notified {}", lock);
        } finally {
            lock.unlock();
        }
    }

    private void notifyResult() {
        lock.lock();
        LOGGER.trace("notify {}", lock);
        condition.signalAll();
        lock.unlock();
    }

    public void setValue(V value) {
        this.value = value;
        done.set(true);
        notifyResult();
    }

    public void setException(Throwable exception) {
        this.exception = exception;
        done.set(true);
        notifyResult();
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
        long start = System.nanoTime();
        while (!done.get()) {//自旋等待
            try {
                LOGGER.trace("wait... {}", lock);
                waitResult(1, TimeUnit.SECONDS);
            } catch (TimeoutException ignore) {
            }
            checkException();//异常了
        }
        checkException();
        LOGGER.trace("get future: {}ms", (System.nanoTime() - start) / 1000000);
        return value;
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        long nanosTimeout = unit.toNanos(timeout);
        long start = System.nanoTime();
        while (!done.get()) {//自旋等待
            waitResult(timeout, unit);
            checkException();//异常了
            checkTimeout(timeout, unit, start, nanosTimeout);//超时了
        }
        checkException();
        checkTimeout(timeout, unit, start, nanosTimeout);//超时了
        LOGGER.trace("get future: {}ms", (System.nanoTime() - start) / 1000000);
        return value;
    }

    private void checkException() throws ExecutionException {
        if (exception != null) {
            throw new ExecutionException(exception);
        }
    }

    private void checkTimeout(long timeout, TimeUnit unit, long startNanos, long nanosTimeout) throws TimeoutException {
        if (System.nanoTime() - startNanos > nanosTimeout) {
            throw new TimeoutException(timeout + " " + unit.toString().toLowerCase());
        }
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException, TimeoutException {
        final FutureAdapter<String> futureAdapter = new FutureAdapter<>();
        new Thread(new Runnable() {
            @Override public void run() {
                try {
                    Thread.sleep(1200);
                    futureAdapter.setValue("ok");
                } catch (InterruptedException ignore) {
                }
            }
        }).start();
        LOGGER.info("{}", futureAdapter.get(1205, TimeUnit.MILLISECONDS));
    }
}
