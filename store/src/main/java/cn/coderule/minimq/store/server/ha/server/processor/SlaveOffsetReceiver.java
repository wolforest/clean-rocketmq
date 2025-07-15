package cn.coderule.minimq.store.server.ha.server.processor;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.minimq.store.server.ha.core.HAConnection;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SlaveOffsetReceiver extends ServiceThread implements Serializable, Lifecycle {
    private static final int READ_MAX_BUFFER_SIZE = 1024 * 1024;

    @Getter @Setter
    private volatile long requestOffset = -1;
    @Getter @Setter
    private volatile long ackOffset = -1;

    private Selector selector;
    private SocketChannel socketChannel;
    private ByteBuffer readBuffer;

    private int processPosition;
    private volatile long lastReadTime;
    private HAConnection connection;

    public SlaveOffsetReceiver(HAConnection connection) {
        this.connection = connection;

        this.readBuffer = ByteBuffer.allocate(READ_MAX_BUFFER_SIZE);
        this.lastReadTime = System.currentTimeMillis();
        this.processPosition = 0;
    }

    @Override
    public String getServiceName() {
        return SlaveOffsetReceiver.class.getSimpleName();
    }

    @Override
    public void run() {

    }
}
