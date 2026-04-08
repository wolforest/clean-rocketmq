package cn.coderule.wolfmq.domain.domain.timer;

import cn.coderule.wolfmq.domain.core.constant.MQConstants;
import cn.coderule.wolfmq.domain.core.constant.MessageConst;
import cn.coderule.wolfmq.domain.domain.meta.topic.TopicValidator;

public class TimerConstants {
    public static final String TIMER_TOPIC = TopicValidator.SYSTEM_TOPIC_PREFIX + "wheel_timer";
    public static final String TIMER_GROUP = MQConstants.CID_RMQ_SYS_PREFIX + "TIMER_GROUP";

    public static final int INITIAL = 0, RUNNING = 1, HALT = 2, SHUTDOWN = 3;
    public static final int MAGIC_DEFAULT = 1;
    public static final int MAGIC_ROLL = 1 << 1;
    public static final int MAGIC_DELETE = 1 << 2;
    public static final int PUT_OK = 0, PUT_NEED_RETRY = 1, PUT_NO_RETRY = 2, PUT_FAILED = -1;

    public static final String TIMER_ENQUEUE_MS = MessageConst.PROPERTY_TIMER_ENQUEUE_MS;
    public static final String TIMER_DEQUEUE_MS = MessageConst.PROPERTY_TIMER_DEQUEUE_MS;
    public static final String TIMER_ROLL_TIMES = MessageConst.PROPERTY_TIMER_ROLL_TIMES;
    public static final String TIMER_DELETE_UNIQUE_KEY = MessageConst.PROPERTY_TIMER_DEL_UNIQKEY;

    public static final int TIMER_BLANK_SLOTS = 60;
}
