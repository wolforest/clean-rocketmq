package cn.coderule.minimq.domain.domain.meta.topic;

import cn.coderule.common.util.lang.bean.BeanUtil;

public class TopicQueueId {
    private final String topic;
    private final int queueId;

    private final int hash;

    public TopicQueueId(String topic, int queueId) {
        this.topic = topic;
        this.queueId = queueId;

        this.hash = BeanUtil.hashCode(topic, queueId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        TopicQueueId broker = (TopicQueueId) o;
        return queueId == broker.queueId && BeanUtil.equals(topic, broker.topic);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MessageQueueInBroker{");
        sb.append("topic='").append(topic).append('\'');
        sb.append(", queueId=").append(queueId);
        sb.append('}');
        return sb.toString();
    }
}
