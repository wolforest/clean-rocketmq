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

    @Builder.Default
    private boolean cleanOffset = false;

    public static SubscriptionRequest build(SubscriptionGroup group) {
        return SubscriptionRequest.builder()
            .group(group)
            .build();
    }

}
