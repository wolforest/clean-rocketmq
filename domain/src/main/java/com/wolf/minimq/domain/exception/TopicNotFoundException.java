package com.wolf.minimq.domain.exception;

import com.wolf.common.lang.exception.BusinessException;
import com.wolf.minimq.domain.enums.EnqueueStatus;
import lombok.Getter;

@Getter
public class TopicNotFoundException extends BusinessException {

    public TopicNotFoundException(String topicName) {
        super("can't find topic: " + topicName + ", Please create topic");
    }
}
