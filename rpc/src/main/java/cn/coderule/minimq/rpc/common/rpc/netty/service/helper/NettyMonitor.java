package cn.coderule.minimq.rpc.common.rpc.netty.service.helper;

import cn.coderule.common.lang.concurrent.thread.DefaultThreadFactory;
import cn.coderule.common.util.lang.ThreadUtil;
import cn.coderule.minimq.domain.config.network.RpcServerConfig;
import cn.coderule.minimq.rpc.common.rpc.netty.handler.RequestCodeCounter;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyMonitor {
    private final RpcServerConfig config;
    private final RequestCodeCounter requestCodeCounter;
    private final ScheduledExecutorService scheduler;

    public NettyMonitor(RpcServerConfig config, RequestCodeCounter requestCodeCounter) {
        this.config = config;
        this.requestCodeCounter = requestCodeCounter;
        this.scheduler = buildScheduler();
    }

    public void start() {
        if (null == requestCodeCounter) {
            return;
        }

        scheduler.scheduleWithFixedDelay(() -> {
            try {
                monitor();
            } catch (Exception e) {
                log.error("Netty monitor error", e);
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    public void shutdown() {
        scheduler.shutdown();
    }

    private void monitor() {
        if (null == requestCodeCounter) {
            return;
        }

        String inBoundSnapshotString = requestCodeCounter.getInBoundSnapshotString();
        if (inBoundSnapshotString != null) {
            log.info("Port: {}, RequestCode Distribution: {}",
                config.getPort(), inBoundSnapshotString);
        }

        String outBoundSnapshotString = requestCodeCounter.getOutBoundSnapshotString();
        if (outBoundSnapshotString != null) {
            log.info("Port: {}, ResponseCode Distribution: {}",
                config.getPort(), outBoundSnapshotString);
        }
    }

    private ScheduledExecutorService buildScheduler() {
        return ThreadUtil.newScheduledThreadPool(
            1,
            new DefaultThreadFactory("NettyScheduler"),
            new ThreadPoolExecutor.DiscardOldestPolicy());
    }

}
