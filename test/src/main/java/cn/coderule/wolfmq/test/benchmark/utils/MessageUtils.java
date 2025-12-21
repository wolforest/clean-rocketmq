package cn.coderule.wolfmq.test.benchmark.utils;

import org.apache.rocketmq.client.apis.message.Message;
import cn.coderule.wolfmq.test.manager.ClientManager;
import java.util.Arrays;

public class MessageUtils {
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

    {
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

    public static Message createMessage(String topic, int messageSize) {
        return ClientManager.getProvider()
            .newMessageBuilder()
            .setTopic(topic)
            .setBody(getBody(messageSize))
            .build();
    }

    private static byte[] getBody(int messageSize) {
        return switch (messageSize) {
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
