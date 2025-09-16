package cn.coderule.minimq.store.domain.meta;

import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.domain.meta.topic.Topic;
import cn.coderule.minimq.domain.domain.store.domain.meta.TopicService;
import cn.coderule.minimq.domain.domain.meta.topic.KeyBuilder;
import cn.coderule.minimq.domain.domain.meta.topic.TopicValidator;

public class SystemTopicRegister {
    private static final int SCHEDULE_TOPIC_QUEUE_NUM = 18;

    private final StoreConfig storeConfig;
    private final TopicService topicService;

    public SystemTopicRegister(StoreConfig storeConfig, TopicService topicService) {
        this.storeConfig = storeConfig;
        this.topicService = topicService;
    }

    public void register() {
        addSystemTopic(TopicValidator.RMQ_SYS_SELF_TEST_TOPIC, 1, 1);
        addSystemTopic(TopicValidator.RMQ_SYS_BENCHMARK_TOPIC, 1024, 1024);
        addSystemTopic(TopicValidator.RMQ_SYS_OFFSET_MOVED_EVENT, 1, 1);
        addSystemTopic(TopicValidator.RMQ_SYS_SCHEDULE_TOPIC, SCHEDULE_TOPIC_QUEUE_NUM, SCHEDULE_TOPIC_QUEUE_NUM);

         // PopAckConstants.REVIVE_TOPIC
        addSystemTopic(KeyBuilder.buildClusterReviveTopic(storeConfig.getCluster()), 8, 8);

        // sync broker member group topic
        addSystemTopic(TopicValidator.SYNC_BROKER_MEMBER_GROUP_PREFIX + storeConfig.getCluster(), 1, 1);

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

        topicService.putTopic(topic);
    }

}
