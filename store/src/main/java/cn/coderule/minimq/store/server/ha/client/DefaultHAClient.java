package cn.coderule.minimq.store.server.ha.client;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.common.util.net.NetworkUtil;
import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.service.store.api.CommitLogStore;
import cn.coderule.minimq.rpc.common.rpc.netty.service.helper.NettyHelper;
import cn.coderule.minimq.store.server.ha.core.ConnectionState;
import cn.coderule.minimq.store.server.ha.core.monitor.FlowMonitor;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicReference;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Report header buffer size. Schema: slaveMaxOffset. Format:
 *
 * <pre>
 * ┌───────────────────────────────────────────────┐
 * │                  slaveMaxOffset               │
 * │                    (8bytes)                   │
 * ├───────────────────────────────────────────────┤
 * │                                               │
 * │                  Report Header                │
 * </pre>
 * <p>
 */
@Slf4j
public class DefaultHAClient extends ServiceThread implements HAClient, Lifecycle {
    public static final int REPORT_HEADER_SIZE = 8;

    @Getter
    private final StoreConfig storeConfig;

    private final AtomicReference<String> masterHaAddress = new AtomicReference<>();
    private final AtomicReference<String> masterAddress = new AtomicReference<>();
    @Getter
    private SocketChannel socketChannel;
    @Getter
    private final Selector selector;
    @Getter
    private final FlowMonitor flowMonitor;
    @Getter
    private final CommitLogStore commitLogStore;
    private final CommitLogReceiver commitLogReceiver;

    private volatile ConnectionState state = ConnectionState.READY;

    public DefaultHAClient(StoreConfig storeConfig, CommitLogStore commitLogStore) throws IOException {
        this.storeConfig = storeConfig;
        this.commitLogStore = commitLogStore;

        this.selector = NetworkUtil.openSelector();
        this.flowMonitor = new FlowMonitor(storeConfig);
        this.commitLogReceiver = new CommitLogReceiver(this);
    }

    @Override
    public String getServiceName() {
        return DefaultHAClient.class.getSimpleName();
    }


    @Override
    public void shutdown() throws Exception {
        this.state = ConnectionState.SHUTDOWN;
        this.flowMonitor.shutdown();
        commitLogReceiver.shutdown();

        closeMaster();
        closeSelector();

        super.shutdown();
    }

    @Override
    public void initialize() throws Exception {
        Lifecycle.super.initialize();
    }

    @Override
    public ConnectionState getConnectionState() {
        return state;
    }

    @Override
    public void changeConnectionState(ConnectionState state) {
        this.state = state;
    }

    @Override
    public String getMasterAddress() {
        return masterAddress.get();
    }

    @Override
    public String getMasterHaAddress() {
        return masterHaAddress.get();
    }

    @Override
    public void setMasterAddress(String newAddress) {
        String addr = this.masterAddress.get();
        if (masterAddress.compareAndSet(addr, newAddress)) {
            log.info("update master address from {} to {}",
                addr, newAddress);
        }
    }

    @Override
    public void setMasterHaAddress(String newAddress) {
        String addr = this.masterHaAddress.get();
        if (masterHaAddress.compareAndSet(addr, newAddress)) {
            log.info("update master ha address from {} to {}",
                addr, newAddress);
        }
    }

    @Override
    public boolean connectMaster() throws Exception {
        if (null != socketChannel) {
            return true;
        }

        String addr = this.masterHaAddress.get();
        if (addr != null) {
            SocketAddress socketAddress = NetworkUtil.toSocketAddress(addr);
            this.socketChannel = NettyHelper.connect(socketAddress);
            if (this.socketChannel != null) {
                this.socketChannel.register(this.selector, SelectionKey.OP_READ);
                log.info("HAClient connect to master {}", addr);
                this.state = ConnectionState.TRANSFER;
            }
        }

        commitLogReceiver.initialize();

        return this.socketChannel != null;
    }

    @Override
    public void closeMaster() {
        if (null == this.socketChannel) {
            return;
        }

        try {
            SelectionKey sk = this.socketChannel.keyFor(this.selector);
            if (sk != null) {
                sk.cancel();
            }

            this.socketChannel.close();
            this.socketChannel = null;

            log.info("HAClient close connection with master {}", this.masterHaAddress.get());
            this.state = ConnectionState.READY;
        } catch (IOException e) {
            log.warn("closeMaster exception. ", e);
        }
    }

    @Override
    public void run() {
        log.info("{} service started", this.getServiceName());

        startFlowMonitor();
        startCommitLogReceiver();

        while (!this.isStopped()) {
            try {
                switch (this.state) {
                    case SHUTDOWN:
                        this.flowMonitor.shutdown(true);
                        return;
                    case READY:
                        if (!this.connectMaster()) {
                            log.warn("HAClient connect to master {} failed", this.masterHaAddress.get());
                            this.await(5_000);
                        }
                        continue;
                    case TRANSFER:
                        if (!commitLogReceiver.receive()) {
                            closeMasterAndWait();
                            continue;
                        }
                        break;
                    default:
                        this.await(2_000);
                        continue;
                }

                tryCloseMaster();
            } catch (Exception e) {
                log.warn("{} service has exception. ", this.getServiceName(), e);
                this.closeMasterAndWait();
            }
        }

        this.flowMonitor.shutdown(true);
        log.info("{} service end", this.getServiceName());
    }

    private void startFlowMonitor() {
        try {
            this.flowMonitor.start();
        } catch (Exception e) {
            log.error("{} startFlowMonitor exception.", this.getServiceName(), e);
        }
    }

    private void startCommitLogReceiver() {
        try {
            this.commitLogReceiver.start();
        } catch (Exception e) {
            log.error("{} startCommitLogReceiver exception.", this.getServiceName(), e);
        }
    }

    public void closeMasterAndWait() {
        this.closeMaster();
        this.await(5_000);
    }

    private void tryCloseMaster() {
        long interval = System.currentTimeMillis() - commitLogReceiver.getLastReadTime();
        if (interval <= storeConfig.getHaHouseKeepingInterval()) {
            return;
        }

        log.warn("AutoRecoverHAClient, housekeeping, found this connection[{}] expired, {}",
            this.masterHaAddress, interval);
        this.closeMaster();
        log.warn("AutoRecoverHAClient, master not response some time, so close connection");
    }

    private void closeSelector() {
        try {
            this.selector.close();
        } catch (IOException e) {
            log.warn("Close the selector of AutoRecoverHAClient error, ", e);
        }
    }
}
