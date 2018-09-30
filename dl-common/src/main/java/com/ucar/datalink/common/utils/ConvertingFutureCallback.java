package com.ucar.datalink.common.utils;

import java.util.concurrent.*;

public abstract class ConvertingFutureCallback<U, T> implements Callback<U>, Future<T> {

    private Callback<T> underlying;
    private CountDownLatch finishedLatch;
    private T result = null;
    private Throwable exception = null;

    public ConvertingFutureCallback(Callback<T> underlying) {
        this.underlying = underlying;
        this.finishedLatch = new CountDownLatch(1);
    }

    public abstract T convert(U result);

    @Override
    public void onCompletion(Throwable error, U result) {
        this.exception = error;
        this.result = convert(result);
        if (underlying != null)
            underlying.onCompletion(error, this.result);
        finishedLatch.countDown();
    }

    @Override
    public boolean cancel(boolean b) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return finishedLatch.getCount() == 0;
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        finishedLatch.await();
        return result();
    }

    @Override
    public T get(long l, TimeUnit timeUnit)
            throws InterruptedException, ExecutionException, TimeoutException {
        if (!finishedLatch.await(l, timeUnit))
            throw new TimeoutException("Timed out waiting for future");
        return result();
    }

    private T result() throws ExecutionException {
        if (exception != null) {
            throw new ExecutionException(exception);
        }
        return result;
    }
}

