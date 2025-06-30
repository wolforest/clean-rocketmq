package cn.coderule.minimq.domain.domain.meta.subscription;

import cn.coderule.minimq.domain.domain.cluster.RequestContext;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionRequest implements Serializable {
    private RequestContext requestContext;

    private String topicName;
    private String groupName;
    private SubscriptionGroup group;
    private SubscriptionMap subscriptionMap;

    public static SubscriptionRequest build(SubscriptionGroup topic) {
        return SubscriptionRequest.builder()
            .group(topic)
            .build();
    }

}
