package cn.coderule.minimq.store.domain.commitlog;

import cn.coderule.common.util.lang.string.StringUtil;
import cn.coderule.minimq.domain.config.store.CommitConfig;
import cn.coderule.minimq.domain.domain.store.domain.meta.TopicService;

public class TopicPartitioner {
    private final CommitConfig commitConfig;
    private final TopicService topicService;

    private final int maxShardingNumber;

    public TopicPartitioner(CommitConfig commitConfig) {
        this(commitConfig, null);
    }

    public TopicPartitioner(CommitConfig commitConfig, TopicService topicService) {
        this.commitConfig = commitConfig;
        this.topicService = topicService;

        this.maxShardingNumber = commitConfig.getMaxShardingNumber();
    }

    public int partitionByTopic(String topic) {
        if (StringUtil.isBlank(topic)) {
            throw new IllegalArgumentException("topic can't be blank");
        }

        return Math.abs(topic.hashCode()) % maxShardingNumber;
    }

    public int partitionByOffset(long offset) {
        if (offset < 0) {
            throw new IllegalArgumentException("offset must be positive");
        }

        return (int) (offset % maxShardingNumber);
    }
}
