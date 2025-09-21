package cn.coderule.minimq.domain.test;

import cn.coderule.minimq.domain.domain.message.MessageBO;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;

public class MessageMock {
    public static MessageBO createMessage() {
       return createMessage("test", 0, 0);
    }

    public static MessageBO createMessage(String topic, int queueId, long queueOffset) {
        long now = System.currentTimeMillis();
        SocketAddress address = new InetSocketAddress("0.0.0.0", 0);
        return MessageBO.builder()
            .topic(topic)
            .body("test".getBytes())
            .properties(new HashMap<>())

            .queueId(queueId)
            .queueOffset(queueOffset)
            .commitOffset(0)
            .prepareOffset(0)

            .flag(0)
            .sysFlag(0)
            .reconsumeTimes(0)

            .bornTimestamp(now)
            .storeTimestamp(now)
            .bornHost(address)
            .storeHost(address)
            .build();
    }

}
