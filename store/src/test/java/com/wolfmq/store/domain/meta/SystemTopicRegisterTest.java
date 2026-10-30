package com.wolfmq.store.domain.meta;

import com.wolfmq.domain.config.server.StoreConfig;
import com.wolfmq.domain.core.constant.MQConstants;
import com.wolfmq.domain.domain.meta.topic.KeyBuilder;
import com.wolfmq.domain.domain.meta.topic.Topic;
import com.wolfmq.domain.domain.meta.topic.TopicValidator;
import com.wolfmq.domain.domain.meta.service.TopicService;
import com.wolfmq.domain.domain.timer.model.TimerConstants;
import com.wolfmq.domain.test.ConfigMock;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SystemTopicRegisterTest {

    @TempDir
    Path tempDir;

    @Test
    void register_ShouldPutSystemTopicsWithExpectedSettings() {
        StoreConfig storeConfig = ConfigMock.createStoreConfig(tempDir.toString());
        storeConfig.getTopicConfig().setTimerQueueNum(3);
        storeConfig.getTopicConfig().setReviveQueueNum(2);

        TopicService topicService = mock(TopicService.class);
        SystemTopicRegister register = new SystemTopicRegister(storeConfig, topicService);

        register.register();

        ArgumentCaptor<Topic> captor = ArgumentCaptor.forClass(Topic.class);
        verify(topicService, times(10)).putTopic(captor.capture());

        List<Topic> topics = captor.getAllValues();
        assertNotNull(findTopic(topics, TopicValidator.RMQ_SYS_SELF_TEST_TOPIC));
        assertEquals(18, findTopic(topics, TopicValidator.RMQ_SYS_SCHEDULE_TOPIC).getReadQueueNums());
        assertEquals(18, findTopic(topics, TopicValidator.RMQ_SYS_SCHEDULE_TOPIC).getWriteQueueNums());

        String replyTopic = storeConfig.getCluster() + "_" + MQConstants.REPLY_TOPIC_POSTFIX;
        assertNotNull(findTopic(topics, replyTopic));

        Topic timerTopic = findTopic(topics, TimerConstants.TIMER_TOPIC);
        assertEquals(3, timerTopic.getReadQueueNums());
        assertEquals(3, timerTopic.getWriteQueueNums());

        String reviveTopic = KeyBuilder.buildClusterReviveTopic(storeConfig.getCluster());
        Topic revive = findTopic(topics, reviveTopic);
        assertEquals(2, revive.getReadQueueNums());
        assertEquals(2, revive.getWriteQueueNums());
    }

    private Topic findTopic(List<Topic> topics, String topicName) {
        return topics.stream()
            .filter(t -> topicName.equals(t.getTopicName()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("topic not found: " + topicName));
    }
}

