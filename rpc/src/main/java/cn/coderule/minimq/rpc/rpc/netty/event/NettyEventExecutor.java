package cn.coderule.minimq.rpc.rpc.netty.event;

import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.minimq.rpc.rpc.RpcListener;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyEventExecutor extends ServiceThread {
    private final LinkedBlockingQueue<NettyEvent> eventQueue = new LinkedBlockingQueue<>();
    @Getter
    private final RpcListener rpcListener;

    public NettyEventExecutor(RpcListener rpcListener) {
        this.rpcListener = rpcListener;
    }

    @Override
    public String getServiceName() {
        return NettyEventExecutor.class.getSimpleName();
    }

    public void putNettyEvent(final NettyEvent event) {
        int currentSize = this.eventQueue.size();
        int maxSize = 10000;

        if (currentSize <= maxSize) {
            this.eventQueue.add(event);
        } else {
            log.warn("event queue size [{}] over the limit [{}], so drop this event {}", currentSize, maxSize, event.toString());
        }
    }

    @Override
    public void run() {
        if (null == this.rpcListener) {
            return;
        }

        log.info("{} service started", this.getServiceName());

        while (!this.isStopped()) {
            processEvent();
        }

        log.info("{} service end", this.getServiceName());
    }

    private void processEvent() {
        try {
            NettyEvent event = this.eventQueue.poll(3000, TimeUnit.MILLISECONDS);
            RpcListener listener = this.rpcListener;
            if (event == null || listener == null) {
                return;
            }

            switch (event.getType()) {
                case IDLE:
                    listener.onIdle(event.getAddress(), event.getChannel());
                    break;
                case CLOSE:
                    listener.onClose(event.getAddress(), event.getChannel());
                    break;
                case CONNECT:
                    listener.onConnect(event.getAddress(), event.getChannel());
                    break;
                case EXCEPTION:
                    listener.onException(event.getAddress(), event.getChannel());
                    break;
                case ACTIVE:
                    listener.onActive(event.getAddress(), event.getChannel());
                    break;
            }
        } catch (Exception e) {
            log.warn("{} service has exception. ", this.getServiceName(), e);
        }
    }

}
