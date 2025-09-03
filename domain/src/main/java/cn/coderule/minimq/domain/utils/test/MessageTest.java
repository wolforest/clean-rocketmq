package cn.coderule.minimq.domain.utils.test;

import cn.coderule.minimq.domain.domain.message.MessageBO;

public class MessageTest {
    public static MessageBO createMessage() {
        return MessageBO.builder()
            .topic("test")
            .body("test".getBytes())
            .build();
    }
}
