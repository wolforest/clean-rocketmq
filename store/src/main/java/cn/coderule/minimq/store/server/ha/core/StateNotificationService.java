package cn.coderule.minimq.store.server.ha.core;

import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.store.server.ha.HAClient;
import cn.coderule.minimq.store.server.ha.HAConnection;
import cn.coderule.minimq.store.server.ha.HAServer;
import java.net.InetSocketAddress;
import lombok.extern.slf4j.Slf4j;


/**
 * Service to periodically check and notify for certain connection state.
 */
@Slf4j
public class StateNotificationService extends ServiceThread {
    private static final long CONNECTION_ESTABLISH_TIMEOUT = 10 * 1000;

    private final StoreConfig storeConfig;

    private volatile StateNotificationRequest request;
    private volatile long lastCheckTimeStamp = -1;
    private final HAServer haServer;
    private final HAClient haClient;

    public StateNotificationService(StoreConfig storeConfig, HAServer haServer, HAClient haClient) {
        this.storeConfig = storeConfig;
        this.haServer = haServer;
        this.haClient = haClient;
    }

    @Override
    public String getServiceName() {
        return StateNotificationService.class.getSimpleName();
    }

    public synchronized void setRequest(StateNotificationRequest request) {
        if (this.request != null) {
            this.request.getRequestFuture().cancel(true);
        }
        this.request = request;
        lastCheckTimeStamp = System.currentTimeMillis();
    }

    private synchronized void doWaitConnectionState() {
        if (this.request == null || this.request.getRequestFuture().isDone()) {
            return;
        }

        if (!storeConfig.isMaster()) {
            if (haClient.getConnectionState() == this.request.getExpectState()) {
                this.request.getRequestFuture().complete(true);
                this.request = null;
            } else if (haClient.getConnectionState() == ConnectionState.READY) {
                if ((System.currentTimeMillis() - lastCheckTimeStamp) > CONNECTION_ESTABLISH_TIMEOUT) {
                    log.error("Slave Wait HA connection establish with {} timeout", this.request.getRemoteAddr());
                    this.request.getRequestFuture().complete(false);
                    this.request = null;
                }
            } else {
                lastCheckTimeStamp = System.currentTimeMillis();
            }
        } else {
            boolean connectionFound = false;
            for (HAConnection connection : haServer.getConnectionList()) {
                if (checkConnectionStateAndNotify(connection)) {
                    connectionFound = true;
                }
            }

            if (connectionFound) {
                lastCheckTimeStamp = System.currentTimeMillis();
            }

            if (!connectionFound && (System.currentTimeMillis() - lastCheckTimeStamp) > CONNECTION_ESTABLISH_TIMEOUT) {
                log.error("Master Wait HA connection establish with {} timeout", this.request.getRemoteAddr());
                this.request.getRequestFuture().complete(false);
                this.request = null;
            }
        }
    }

    /**
     * Check if connection matched and notify request.
     *
     * @param connection connection to check.
     * @return if connection remote address match request.
     */
    public synchronized boolean checkConnectionStateAndNotify(HAConnection connection) {
        if (this.request == null || connection == null) {
            return false;
        }

        String remoteAddress;
        try {
            remoteAddress = ((InetSocketAddress) connection.getSocketChannel().getRemoteAddress())
                .getAddress().getHostAddress();
            if (remoteAddress.equals(request.getRemoteAddr())) {
                ConnectionState connState = connection.getConnectionState();

                if (connState == this.request.getExpectState()) {
                    this.request.getRequestFuture().complete(true);
                    this.request = null;
                } else if (this.request.isNotifyWhenShutdown() && connState == ConnectionState.SHUTDOWN) {
                    this.request.getRequestFuture().complete(false);
                    this.request = null;
                }
                return true;
            }
        } catch (Exception e) {
            log.error("Check connection address exception: ", e);
        }

        return false;
    }

    @Override
    public void run() {
        log.info("{} service started", this.getServiceName());

        while (!this.isStopped()) {
            try {
                this.await(1_000);
                this.doWaitConnectionState();
            } catch (Exception e) {
                log.warn("{} service has exception. ", this.getServiceName(), e);
            }
        }

        log.info("{} service end", this.getServiceName());
    }
}
