package cn.coderule.minimq.store.domain.commitlog.sharding;

import cn.coderule.common.util.lang.string.StringUtil;
import cn.coderule.minimq.domain.config.store.CommitConfig;
import cn.coderule.minimq.domain.domain.store.domain.meta.TopicService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TopicPartitioner {
    private final CommitConfig commitConfig;
    private final TopicService topicService;

    private final int maxShardingNumber;
    private final int shardingNumber;

    public TopicPartitioner(CommitConfig commitConfig) {
        this(commitConfig, null);
    }

    public TopicPartitioner(CommitConfig commitConfig, TopicService topicService) {
        this.commitConfig = commitConfig;
        this.topicService = topicService;

        this.shardingNumber = commitConfig.getShardingNumber();
        this.maxShardingNumber = commitConfig.getMaxShardingNumber();
    }

    public int partitionByTopic(String topic) {
        if (StringUtil.isBlank(topic)) {
            log.error("[TopicPartitioner]topic is blank");
            throw new IllegalArgumentException("topic can't be blank");
        }

        return Math.abs(topic.hashCode()) % shardingNumber;
    }

    public int partitionByOffset(long offset) {
        if (offset < 0) {
            log.error("[TopicPartitioner]offset is negative");
            throw new IllegalArgumentException("offset must be positive");
        }

        return (int) (offset % maxShardingNumber);
    }
}
