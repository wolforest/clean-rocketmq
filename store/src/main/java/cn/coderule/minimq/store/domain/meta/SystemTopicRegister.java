package cn.coderule.minimq.store.domain.meta;

import cn.coderule.minimq.domain.model.Topic;
import cn.coderule.minimq.domain.service.store.domain.meta.TopicStore;
import cn.coderule.minimq.domain.utils.topic.KeyBuilder;
import cn.coderule.minimq.domain.utils.topic.TopicValidator;

public class SystemTopicRegister {
    private static final int SCHEDULE_TOPIC_QUEUE_NUM = 18;

    private final TopicStore topicStore;

    public SystemTopicRegister(TopicStore topicStore) {
        this.topicStore = topicStore;
    }

    public void register() {
        addSystemTopic(TopicValidator.RMQ_SYS_SELF_TEST_TOPIC, 1, 1);
        addSystemTopic(TopicValidator.RMQ_SYS_BENCHMARK_TOPIC, 1024, 1024);
        addSystemTopic(TopicValidator.RMQ_SYS_OFFSET_MOVED_EVENT, 1, 1);
        addSystemTopic(TopicValidator.RMQ_SYS_SCHEDULE_TOPIC, SCHEDULE_TOPIC_QUEUE_NUM, SCHEDULE_TOPIC_QUEUE_NUM);

         // PopAckConstants.REVIVE_TOPIC
        addSystemTopic(KeyBuilder.buildClusterReviveTopic("DEFAULT_BROKER"), 8, 8);

        // sync broker member group topic
        addSystemTopic(TopicValidator.SYNC_BROKER_MEMBER_GROUP_PREFIX + "DEFAULT_BROKER", 1, 1);

        // TopicValidator.RMQ_SYS_TRANS_HALF_TOPIC
        addSystemTopic(TopicValidator.RMQ_SYS_TRANS_HALF_TOPIC, 1,1);

        // TopicValidator.RMQ_SYS_TRANS_OP_HALF_TOPIC
        addSystemTopic(TopicValidator.RMQ_SYS_TRANS_OP_HALF_TOPIC, 1, 1);
    }

    private void addSystemTopic(String topicName, Integer readQueueNums, Integer writeQueueNums) {
        addSystemTopic(topicName, readQueueNums, writeQueueNums, null);
    }

    private void addSystemTopic(String topicName, Integer readQueueNums, Integer writeQueueNums, Integer perm) {
        Topic topic = new Topic();
        topic.setTopicName(topicName);
        TopicValidator.addSystemTopic(topicName);

        if (readQueueNums != null) {
            topic.setReadQueueNums(readQueueNums);
        }

        if (writeQueueNums != null) {
            topic.setWriteQueueNums(writeQueueNums);
        }

        if (perm != null) {
            topic.setPerm(perm);
        }

        topicStore.putTopic(topic);
    }

}
