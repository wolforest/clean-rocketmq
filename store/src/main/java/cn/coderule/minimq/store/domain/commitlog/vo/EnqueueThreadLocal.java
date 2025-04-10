package cn.coderule.minimq.store.domain.commitlog.vo;

import cn.coderule.minimq.domain.config.MessageConfig;
import cn.coderule.minimq.domain.domain.model.message.MessageBO;
import cn.coderule.minimq.domain.utils.message.MessageEncoder;

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
