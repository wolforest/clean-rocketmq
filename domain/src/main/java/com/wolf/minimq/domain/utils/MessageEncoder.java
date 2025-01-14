package com.wolf.minimq.domain.utils;

import com.wolf.minimq.domain.config.MessageConfig;
import com.wolf.minimq.domain.enums.EnqueueStatus;
import com.wolf.minimq.domain.exception.EnqueueException;
import com.wolf.minimq.domain.model.bo.MessageBO;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.UnpooledByteBufAllocator;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MessageEncoder {
    private final MessageConfig messageConfig;

    private final ByteBuf buffer;

    private MessageBO messageBO;
    @Getter
    private int messageLength = 0;

    public MessageEncoder(MessageConfig messageConfig) {
        this.messageConfig = messageConfig;

        ByteBufAllocator allocator = UnpooledByteBufAllocator.DEFAULT;
        buffer = allocator.directBuffer(messageConfig.getMaxSize());
    }

    public void setMessage(MessageBO messageBO) {
        buffer.clear();
        this.messageBO = messageBO;
    }

    public ByteBuffer encode() {

        byte[] properties = null;
        int propertiesLen = 0;

        byte[] topic = messageBO.getTopic().getBytes(StandardCharsets.UTF_8);
        int topicLen = topic.length;
        int bodyLen = messageBO.getBody().length;
        this.messageLength = MessageUtils.calculateMessageLength(
            messageBO.getVersion(), bodyLen, topicLen, propertiesLen
        );

        if (messageLength > messageConfig.getMaxSize() || bodyLen > messageConfig.getMaxBodySize()) {
            throw new EnqueueException(EnqueueStatus.MESSAGE_ILLEGAL);
        }

        return getByteBuffer();
    }

    private ByteBuffer getByteBuffer() {
        return this.buffer.nioBuffer(0, this.messageLength);
    }
}
