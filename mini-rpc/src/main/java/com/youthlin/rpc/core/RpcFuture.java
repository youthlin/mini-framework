package com.youthlin.rpc.core;

import java.util.concurrent.Future;

/**
 * 如果是异步调用, 每个线程对应的 Future.
 * 每次调用会对应一个 Future, 因此应该在调用后立即 {@link #get()}
 *
 * @see FutureAdapter
 * 创建: youthlin.chen
 * 时间: 2017-11-27 14:41
 */
public class RpcFuture<T> {
    private static ThreadLocal<RpcFuture> LOCAL = new ThreadLocal<RpcFuture>() {
        @Override
        protected RpcFuture initialValue() {
            return new RpcFuture();
        }
    };
    private Future<T> future;

    private RpcFuture() {
    }

    @SuppressWarnings("unchecked")
    static <V> RpcFuture<V> getRpcFuture() {
        return LOCAL.get();
    }

    void setFuture(Future<T> future) {
        this.future = future;
    }

    @SuppressWarnings("unchecked")
    public static <V> Future<V> get() {
        return LOCAL.get().future;
    }

}
