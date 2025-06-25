package cn.coderule.minimq.domain.service.common;

import cn.coderule.minimq.domain.domain.MessageQueue;

public interface QueueFilter {
    boolean filter(MessageQueue mq);
}
