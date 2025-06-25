
package cn.coderule.minimq.domain.domain.consumer.receipt;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import io.netty.channel.Channel;

public class ReceiptHandleGroupKey {
    protected final Channel channel;
    protected final String group;

    public ReceiptHandleGroupKey(Channel channel, String group) {
        this.channel = channel;
        this.group = group;
    }

    protected String getChannelId() {
        return channel.id().asLongText();
    }

    public String getGroup() {
        return group;
    }

    public Channel getChannel() {
        return channel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReceiptHandleGroupKey key = (ReceiptHandleGroupKey) o;
        return Objects.equal(getChannelId(), key.getChannelId()) && Objects.equal(group, key.group);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getChannelId(), group);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("channelId", getChannelId())
            .add("group", group)
            .toString();
    }
}
