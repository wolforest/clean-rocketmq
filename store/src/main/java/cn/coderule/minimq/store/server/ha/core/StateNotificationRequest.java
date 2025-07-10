package cn.coderule.minimq.store.server.ha.core;

import java.util.concurrent.CompletableFuture;

public class StateNotificationRequest {
    private final CompletableFuture<Boolean> requestFuture = new CompletableFuture<>();
    private final ConnectionState expectState;
    private final String remoteAddr;
    private final boolean notifyWhenShutdown;

    public StateNotificationRequest(ConnectionState expectState, String remoteAddr, boolean notifyWhenShutdown) {
        this.expectState = expectState;
        this.remoteAddr = remoteAddr;
        this.notifyWhenShutdown = notifyWhenShutdown;
    }

    public CompletableFuture<Boolean> getRequestFuture() {
        return requestFuture;
    }

    public String getRemoteAddr() {
        return remoteAddr;
    }

    public boolean isNotifyWhenShutdown() {
        return notifyWhenShutdown;
    }

    public ConnectionState getExpectState() {
        return expectState;
    }
}
