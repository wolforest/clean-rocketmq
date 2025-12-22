package cn.coderule.minimq.domain.test;

import cn.coderule.minimq.domain.domain.message.MessageBO;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.HashMap;

public class MessageMock {
    private static final int DEFAULT_MESSAGE_SIZE = 100;
    private static final String DEFAULT_TOPIC = "MQT_TEST";

    private static final byte[] H1 = new byte[100];
    private static final byte[] K1 = new byte[1024];
    private static final byte[] K2 = new byte[1024 * 2];
    private static final byte[] K3 = new byte[1024 * 3];
    private static final byte[] K4 = new byte[1024 * 4];
    private static final byte[] K5 = new byte[1024 * 5];
    private static final byte[] K6 = new byte[1024 * 6];
    private static final byte[] K7 = new byte[1024 * 7];
    private static final byte[] K8 = new byte[1024 * 8];
    private static final byte[] K9 = new byte[1024 * 9];
    private static final byte[] K10 = new byte[1024 * 10];

    static {
        Arrays.fill(H1, (byte) 1);
        Arrays.fill(K1, (byte) 1);
        Arrays.fill(K2, (byte) 2);
        Arrays.fill(K3, (byte) 3);
        Arrays.fill(K4, (byte) 4);
        Arrays.fill(K5, (byte) 5);
        Arrays.fill(K6, (byte) 6);
        Arrays.fill(K7, (byte) 7);
        Arrays.fill(K8, (byte) 8);
        Arrays.fill(K9, (byte) 9);
        Arrays.fill(K10, (byte) 10);
    }

    public static MessageBO createMessage() {
       return createMessage(DEFAULT_TOPIC, DEFAULT_MESSAGE_SIZE, 0, 0);
    }

    public static MessageBO createMessage(String topic, int messageSize) {
        return createMessage(topic, messageSize, 0, 0);
    }

    public static MessageBO createMessage(int messageSize) {
        return createMessage(DEFAULT_TOPIC, messageSize, 0, 0);
    }

    public static MessageBO createMessage(String topic, int queueId, long queueOffset) {
        return createMessage(topic, DEFAULT_MESSAGE_SIZE, queueId, queueOffset);
    }

    public static MessageBO createMessage(String topic, int messageSize, int queueId, long queueOffset) {
        long now = System.currentTimeMillis();
        SocketAddress address = new InetSocketAddress("0.0.0.0", 0);
        return MessageBO.builder()
            .topic(topic)
            .body(getBody(messageSize))
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

    private static byte[] getBody(int messageSize) {
        return switch (messageSize) {
            case 100 -> H1;
            case 1024 -> K1;
            case 1024 * 2 -> K2;
            case 1024 * 3 -> K3;
            case 1024 * 4 -> K4;
            case 1024 * 5 -> K5;
            case 1024 * 6 -> K6;
            case 1024 * 7 -> K7;
            case 1024 * 8 -> K8;
            case 1024 * 9 -> K9;
            case 1024 * 10 -> K10;
            default -> new byte[messageSize];
        };
    }

}
