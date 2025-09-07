package cn.coderule.minimq.domain.domain.cluster.selector;

import cn.coderule.minimq.domain.domain.MessageQueue;

public interface QueueFilter {
    boolean filter(MessageQueue mq);
}
