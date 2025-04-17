package cn.coderule.minimq.rpc.rpc.netty.service;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.rpc.rpc.core.invoke.ResponseFuture;
import cn.coderule.minimq.rpc.rpc.core.invoke.RpcCommand;
import io.netty.channel.Channel;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ResponseHandler implements Lifecycle {
    /**
     * response map
     * { opaque : ResponseFuture }
     */
    private final ConcurrentMap<Integer, ResponseFuture> responseMap;
    private final HashedWheelTimer timer;
    private final ExecutorService callbackExecutor;

    public ResponseHandler(ExecutorService callbackExecutor) {
        this.callbackExecutor = callbackExecutor;

        this.timer = new HashedWheelTimer(r -> new Thread(r, "NettyTimer"));
        this.responseMap = new ConcurrentHashMap<>(256);
    }

    @Override
    public void start() {
        this.startScanResponse();
    }

    @Override
    public void shutdown() {
        this.timer.stop();
    }

    public ResponseFuture getResponse(int opaque) {
        return responseMap.get(opaque);
    }

    public void putResponse(int opaque, ResponseFuture response) {
        this.responseMap.put(opaque, response);
    }

    public void removeResponse(int opaque) {
        this.responseMap.remove(opaque);
    }

    public void interruptRequests(Set<String> brokerAddrSet) {
        for (ResponseFuture responseFuture : responseMap.values()) {
            RpcCommand cmd = responseFuture.getRequest();
            if (cmd == null) {
                continue;
            }
            String remoteAddr = NettyHelper.getRemoteAddr(responseFuture.getChannel());
            // interrupt only pull message request
            if (brokerAddrSet.contains(remoteAddr) && (cmd.getCode() == 11 || cmd.getCode() == 361)) {
                log.info("interrupt {}", cmd);
                responseFuture.interrupt();
            }
        }
    }

    public void failFast(final Channel channel) {
        for (Map.Entry<Integer, ResponseFuture> entry : responseMap.entrySet()) {
            if (entry.getValue().getChannel() != channel) {
                continue;
            }

            Integer opaque = entry.getKey();
            if (opaque != null) {
                failFast(opaque);
            }
        }
    }

    public void failFast(final int opaque) {
        ResponseFuture responseFuture = responseMap.remove(opaque);
        if (responseFuture == null) {
            return;
        }

        responseFuture.setSendRequestOK(false);
        responseFuture.putResponse(null);
        try {
            executeInvokeCallback(responseFuture);
        } catch (Throwable e) {
            log.warn("execute callback in requestFail, and callback throw", e);
        } finally {
            responseFuture.release();
        }
    }

    /**
     * Execute callback in callback executor. If callback executor is null, run directly in current thread
     */
    public void executeInvokeCallback(final ResponseFuture responseFuture) {
        ExecutorService executor = this.callbackExecutor;
        boolean runInThisThread = executor == null || executor.isShutdown();

        if (!runInThisThread) {
            runInThisThread = submitInvokeCallback(responseFuture, executor);
        }

        if (runInThisThread) {
            callInvokeCallback(responseFuture);
        }
    }

    private void startScanResponse() {
        TimerTask task = new TimerTask() {
            @Override
            public void run (Timeout timeout) {
                try {
                    ResponseHandler.this.scanResponse();
                } catch (Throwable t) {
                    log.error("NettyInvoker.scanResponse exception", t);
                } finally {
                    timer.newTimeout(this, 1000, TimeUnit.MILLISECONDS);
                }
            }
        };
        timer.newTimeout(task, 3_000, TimeUnit.MILLISECONDS);
    }

    private void scanResponse() {
        List<ResponseFuture> rfList = new LinkedList<>();
        Iterator<Map.Entry<Integer, ResponseFuture>> it = this.responseMap.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<Integer, ResponseFuture> next = it.next();
            ResponseFuture responseFuture = next.getValue();

            long maxTime = responseFuture.getBeginTimestamp() + responseFuture.getTimeoutMillis() + 1000;
            if (maxTime > System.currentTimeMillis()) {
                continue;
            }

            responseFuture.release();
            it.remove();
            rfList.add(responseFuture);
            log.warn("remove timeout request, {}", responseFuture);
        }

        executeInvokeCallback(rfList);
    }

    private void executeInvokeCallback(List<ResponseFuture> rfList) {
        for (ResponseFuture rf : rfList) {
            try {
                executeInvokeCallback(rf);
            } catch (Throwable e) {
                log.warn("scanResponseTable, operationComplete Exception", e);
            }
        }
    }

    private boolean submitInvokeCallback(final ResponseFuture responseFuture, ExecutorService executor) {
        boolean runInThisThread = false;

        try {
            executor.submit(() -> {
                callInvokeCallback(responseFuture);
            });
        } catch (Exception e) {
            runInThisThread = true;
            log.warn("execute callback in executor exception, maybe executor busy", e);
        }

        return runInThisThread;
    }

    private void callInvokeCallback(final ResponseFuture responseFuture) {
        try {
            responseFuture.executeRpcCallback();
        } catch (Throwable e) {
            log.warn("executeInvokeCallback Exception", e);
        } finally {
            responseFuture.release();
        }
    }

}
