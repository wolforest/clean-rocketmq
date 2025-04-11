package cn.coderule.minimq.domain.service.common;

import cn.coderule.minimq.domain.domain.model.MessageQueue;

public interface QueueFilter {
    boolean filter(MessageQueue mq);
}
