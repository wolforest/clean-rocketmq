package com.wolf.minimq.domain.utils;

import com.wolf.minimq.domain.config.MessageConfig;
import com.wolf.minimq.domain.enums.EnqueueStatus;
import com.wolf.minimq.domain.model.Message;
import com.wolf.minimq.domain.model.dto.EnqueueResult;
import com.wolf.minimq.domain.model.bo.MessageBO;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.UnpooledByteBufAllocator;
import java.nio.charset.StandardCharsets;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class MessageEncoder {
    private final MessageConfig messageConfig;
    private final ByteBuf buffer;

    public MessageEncoder(MessageConfig messageConfig) {
        this.messageConfig = messageConfig;

        ByteBufAllocator allocator = UnpooledByteBufAllocator.DEFAULT;
        buffer = allocator.directBuffer(messageConfig.getMaxSize());
    }

    public EnqueueResult encode(MessageBO messageBO) {
        buffer.clear();

        byte[] properties = null;
        int propertiesLen = 0;

        byte[] topic = messageBO.getTopic().getBytes(StandardCharsets.UTF_8);
        int topicLen = topic.length;
        int bodyLen = messageBO.getBody().length;
        int messageLen = MessageUtils.calculateMessageLength(
            messageBO.getVersion(), bodyLen, topicLen, propertiesLen
        );

        if (messageLen > messageConfig.getMaxSize() || bodyLen > messageConfig.getMaxBodySize()) {
            return new EnqueueResult(EnqueueStatus.MESSAGE_ILLEGAL);
        }


        return null;
    }



}
