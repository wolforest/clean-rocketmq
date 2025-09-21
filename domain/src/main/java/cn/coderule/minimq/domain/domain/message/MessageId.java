package cn.coderule.minimq.domain.domain.message;

import java.net.SocketAddress;

public class MessageId {
    private SocketAddress address;
    private long offset;

    public MessageId(SocketAddress address, long offset) {
        this.address = address;
        this.offset = offset;
    }

    public SocketAddress getAddress() {
        return address;
    }

    public void setAddress(SocketAddress address) {
        this.address = address;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }
}
