package com.wolfmq.common.domain.meta.topic.dto;

import com.wolfmq.common.domain.meta.topic.Topic;
import com.wolfmq.common.server.RequestContext;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopicRequest implements Serializable {
    private RequestContext requestContext;

    private String topicName;
    private Topic topic;

    public static TopicRequest build(Topic topic) {
        return TopicRequest.builder()
            .requestContext(RequestContext.create())
            .topicName(topic.getTopicName())
            .topic(topic)
            .build();
    }

}
