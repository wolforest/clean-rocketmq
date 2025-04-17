package cn.coderule.minimq.rpc.rpc.netty.service;

import cn.coderule.common.util.net.NetworkUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ChannelWrapper {
    private final AddressInvoker invoker;
    private final ReentrantReadWriteLock lock;
    @Getter
    private final String address;

    @Getter
    private long lastResponseTime;

    private ChannelFuture channelFuture;
    // only affected by sync or async request, oneway is not included.
    private ChannelFuture channelToClose;
    private volatile long lastReconnectTime = 0L;

    public ChannelWrapper(AddressInvoker invoker, ChannelFuture channelFuture, String address) {
        this.invoker = invoker;
        this.channelFuture = channelFuture;
        this.address = address;
        this.lock = new ReentrantReadWriteLock();
    }

    public boolean isOK() {
        return getChannel() != null && getChannel().isActive();
    }

    public boolean isWritable() {
        return getChannel().isWritable();
    }

    public Channel getChannel() {
        return getChannelFuture().channel();
    }

    public ChannelFuture getChannelFuture() {
        lock.readLock().lock();
        try {
            return this.channelFuture;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void updateLastResponseTime() {
        this.lastResponseTime = System.currentTimeMillis();
    }

    private boolean shouldReconnect() {
        if (lastReconnectTime == 0L) {
            return true;
        }

        long interval = System.currentTimeMillis() - lastReconnectTime;
        return  interval > invoker.getConfig().getMaxReconnectTimeout();
    }

    public boolean reconnect() {
        if (!lock.writeLock().tryLock()) {
            return false;
        }

        try {
            if (!shouldReconnect()) {
                return false;
            }

            channelToClose = channelFuture;

            String[] hostAndPort = NetworkUtil.getHostAndPort(address);
            channelFuture = invoker
                .getBootstrap(address)
                .connect(hostAndPort[0], Integer.parseInt(hostAndPort[1]));

            lastReconnectTime = System.currentTimeMillis();
            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean tryClose(Channel channel) {
        try {
            lock.readLock().lock();
            if (channelFuture == null) {
                return false;
            }

            if (channelFuture.channel().equals(channel)) {
                return true;
            }
        } finally {
            lock.readLock().unlock();
        }
        return false;
    }

    public void close() {
        try {
            lock.writeLock().lock();
            if (channelFuture != null) {
                invoker.closeChannel(channelFuture.channel());
            }

            if (channelToClose != null) {
                invoker.closeChannel(channelToClose.channel());
            }
        } finally {
            lock.writeLock().unlock();
        }
    }
}
