package com.lixiande.client;



import com.lixiande.common.codec.RpcRequest;
import com.lixiande.common.codec.RpcResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.ReentrantLock;

public class RpcFuture implements Future<Object> {
    private Sync sync ;
    private RpcRequest request;
    private RpcResponse response;
    private long startTime;
    private long timeThreshhold = 5000;
    private List<AsyncRPCCallback> pendingCallbacks = new ArrayList<>();
    private ReentrantLock lock = new ReentrantLock();

    public RpcFuture(RpcRequest request) {
        this.sync = new Sync();
        this.request = request;
        this.startTime = System.currentTimeMillis();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isCancelled() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isDone() {
        return sync.isDone();
    }

    @Override
    public Object get() throws InterruptedException, ExecutionException {
        sync.acquire(1);
        if(this.response!=null){
            return this.response.getResult();
        }else {
            return null;
        }
    }

    @Override
    public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        boolean success = sync.tryAcquireNanos(1, unit.toNanos(timeout));
        if(success){
            if(this.response!=null){
                return this.response.getResult();
            }else {
                return null;
            }
        }else {
            throw new RuntimeException("Timeot exception. Request Id : " + this.request.getRequestId()+". Request class name: " + this.request.getClassName() + ". Reuqest method : " + this.request.getMethodName());
        }
    }

    public void done(RpcResponse response){
        this.response = response;
        sync.release(1);


    }

    private void invokeCallbacks(){
        lock.lock();
        try {
            for(final AsyncRPCCallback callback : pendingCallbacks){
                runCallback(callback);
            }
        }finally {
            lock.unlock();
        }
    }

    private void runCallback(final AsyncRPCCallback callback){
        final RpcResponse response = this.response;
        RpcClient.submit(new Runnable(){
            @Override
            public void run() {
                if(!response.isError()){
                    callback.success(response.getResult());
                }else {
                    callback.fail(new RuntimeException("Response error ", new Throwable(response.getError())));
                }
            }
        });
    }

    private RpcFuture addCallback(AsyncRPCCallback callback){
        lock.lock();
        try{
            if(isDone()){
                runCallback(callback);
            }else {
                this.pendingCallbacks.add(callback);
            }
        }finally {
            lock.unlock();
        }
        return this;
    }

    static class Sync extends AbstractQueuedSynchronizer{
        private static final long serialVersionUID = 1L;

        private final int done = 1;
        private final int pending=0;

        @Override
        protected boolean tryAcquire(int arg) {
            return getState()==done;
        }

        @Override
        protected boolean tryRelease(int arg) {
            if(getState() == pending){
                if(compareAndSetState(pending, done)){
                    return true;
                }else {
                    return false;
                }
            }else {
                return false;
            }
        }

        protected boolean isDone(){
            return getState() == done;
        }
    }
}
