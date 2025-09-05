package cn.coderule.minimq.domain.utils.test;

import cn.coderule.minimq.domain.domain.message.MessageBO;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;

public class MessageMock {
    public static MessageBO createMessage() {
        long now = System.currentTimeMillis();
        SocketAddress address = new InetSocketAddress("0.0.0.0", 0);
        return MessageBO.builder()
            .topic("test")
            .body("test".getBytes())
            .flag(0)
            .queueId(0)
            .commitOffset(0)
            .sysFlag(0)
            .bornTimestamp(now)
            .storeTimestamp(now)
            .bornHost(address)
            .storeHost(address)
            .reconsumeTimes(0)
            .preparedTransactionOffset(0)
            .properties(new HashMap<>())
            .build();
    }

}
