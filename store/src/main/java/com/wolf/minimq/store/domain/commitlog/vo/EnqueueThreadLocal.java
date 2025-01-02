package com.wolf.minimq.store.domain.commitlog.vo;

import com.wolf.minimq.domain.config.MessageConfig;
import com.wolf.minimq.domain.model.bo.MessageBO;
import com.wolf.minimq.domain.utils.MessageEncoder;

public class EnqueueThreadLocal {
    private final MessageEncoder encoder;

    public EnqueueThreadLocal(MessageConfig config) {
        encoder = new MessageEncoder(config);
    }

    public MessageEncoder getEncoder(MessageBO messageBO) {
        encoder.setMessage(messageBO);
        return encoder;
    }
}
