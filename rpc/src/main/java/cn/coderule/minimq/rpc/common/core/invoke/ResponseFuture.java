/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.coderule.minimq.rpc.common.core.invoke;

import cn.coderule.common.lang.concurrent.sync.SemaphoreGuard;
import cn.coderule.minimq.rpc.common.core.exception.RemotingException;
import cn.coderule.minimq.rpc.common.core.exception.RemotingSendRequestException;
import cn.coderule.minimq.rpc.common.core.exception.RemotingTimeoutException;
import io.netty.channel.Channel;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.Data;

@Data
public class ResponseFuture {
    private final Channel channel;
    private final int opaque;
    private final RpcCommand request;
    private final long timeoutMillis;
    private final RpcCallback invokeCallback;
    private final long beginTimestamp = System.currentTimeMillis();
    private final CountDownLatch countDownLatch = new CountDownLatch(1);

    private final SemaphoreGuard semaphoreGuard;

    private final AtomicBoolean executeCallbackOnlyOnce = new AtomicBoolean(false);
    private volatile RpcCommand response;
    private volatile boolean sendRequestOK = true;
    private volatile Throwable cause;
    private volatile boolean interrupted = false;

    public ResponseFuture(Channel channel, int opaque, long timeoutMillis, RpcCallback invokeCallback,
                          SemaphoreGuard semaphoreGuard) {
        this(channel, opaque, null, timeoutMillis, invokeCallback, semaphoreGuard);
    }

    public ResponseFuture(Channel channel, RpcCommand request, long timeoutMillis) {
        this(channel, request.getOpaque(), request, timeoutMillis, null, null);
    }

    public ResponseFuture(Channel channel, int opaque, RpcCommand request, long timeoutMillis, RpcCallback invokeCallback,
                          SemaphoreGuard semaphoreGuard) {
        this.channel = channel;
        this.opaque = opaque;
        this.request = request;
        this.timeoutMillis = timeoutMillis;
        this.invokeCallback = invokeCallback;
        this.semaphoreGuard = semaphoreGuard;
    }

    public void executeRpcCallback() {
        if (invokeCallback == null) {
            return;
        }

        if (!this.executeCallbackOnlyOnce.compareAndSet(false, true)) {
            return;
        }

        RpcCommand response = getResponse();
        if (response != null) {
            invokeCallback.onSuccess(response);
            invokeCallback.onComplete(this);
            return;
        }

        if (!isSendRequestOK()) {
            invokeCallback.onFailure(new RemotingSendRequestException(channel.remoteAddress().toString(), getCause()));
        } else if (isTimeout()) {
            invokeCallback.onFailure(new RemotingTimeoutException(channel.remoteAddress().toString(), getTimeoutMillis(), getCause()));
        } else {
            invokeCallback.onFailure(new RemotingException(getRequest().toString(), getCause()));
        }

        invokeCallback.onComplete(this);
    }

    public void interrupt() {
        interrupted = true;
        executeRpcCallback();
    }

    public void release() {
        if (this.semaphoreGuard != null) {
            this.semaphoreGuard.release();
        }
    }

    public boolean isTimeout() {
        long diff = System.currentTimeMillis() - this.beginTimestamp;
        return diff > this.timeoutMillis;
    }

    public RpcCommand waitResponse(final long timeoutMillis) throws InterruptedException {
        this.countDownLatch.await(timeoutMillis, TimeUnit.MILLISECONDS);
        return this.response;
    }

    public void putResponse(final RpcCommand responseCommand) {
        this.response = responseCommand;
        this.countDownLatch.countDown();
    }

    @Override
    public String toString() {
        return "ResponseFuture [responseCommand=" + response + ", sendRequestOK=" + sendRequestOK
            + ", cause=" + cause + ", opaque=" + opaque + ", timeoutMillis=" + timeoutMillis
            + ", invokeCallback=" + invokeCallback + ", beginTimestamp=" + beginTimestamp
            + ", countDownLatch=" + countDownLatch + "]";
    }
}
