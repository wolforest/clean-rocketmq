package cn.coderule.minimq.domain.utils.test;

import cn.coderule.common.util.lang.string.StringUtil;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;

public class MessageMock {
    private static final String TOPIC_PREFIX = "MQT_";

    public static String createTopic() {
        return TOPIC_PREFIX + StringUtil.uuid();
    }

    public static MessageBO createMessage() {
        long now = System.currentTimeMillis();
        SocketAddress address = new InetSocketAddress("0.0.0.0", 0);
        return MessageBO.builder()
            .topic("test")
            .body("test".getBytes())
            .properties(new HashMap<>())

            .queueId(0)
            .queueOffset(0)
            .commitOffset(0)
            .preparedTransactionOffset(0)

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
