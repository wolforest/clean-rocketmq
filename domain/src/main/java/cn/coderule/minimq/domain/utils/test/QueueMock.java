package cn.coderule.minimq.domain.utils.test;

import cn.coderule.common.util.lang.string.StringUtil;
import java.util.concurrent.ThreadLocalRandom;

public class QueueMock {
    private static final String TOPIC_PREFIX = "MQT_";
    private static final String GROUP_PREFIX = "MQG_";

    public static String createTopic() {
        return TOPIC_PREFIX + StringUtil.uuid();
    }

    public static String createGroup() {
        return GROUP_PREFIX + StringUtil.uuid();
    }

    public static int createQueueId() {
        return createQueueId(16);
    }

    public static int createQueueId(int max) {
        return ThreadLocalRandom.current().nextInt(0, max);
    }
}
