package cn.coderule.minimq.domain.core.exception;

import cn.coderule.common.lang.exception.BusinessException;
import lombok.Getter;

@Getter
public class TopicNotFoundException extends BusinessException {

    public TopicNotFoundException(String topicName) {
        super("can't find topic: " + topicName + ", Please create topic");
    }
}
