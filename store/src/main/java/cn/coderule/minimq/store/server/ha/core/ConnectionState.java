package cn.coderule.minimq.store.server.ha.core;

public enum ConnectionState {
    /**
     * Ready to start connection.
     */
    READY,
    /**
     * CommitLog consistency checking.
     */
    HANDSHAKE,
    /**
     * Synchronizing data.
     */
    TRANSFER,
    /**
     * Temporarily stop transferring.
     */
    SUSPEND,
    /**
     * Connection shutdown.
     */
    SHUTDOWN,
}
