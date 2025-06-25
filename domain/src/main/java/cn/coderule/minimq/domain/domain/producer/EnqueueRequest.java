package cn.coderule.minimq.domain.domain.producer;

import cn.coderule.minimq.domain.domain.cluster.RequestContext;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EnqueueRequest implements Serializable {
    private RequestContext requestContext;
    private MessageBO messageBO;

    public static EnqueueRequest create(MessageBO messageBO) {
        return new EnqueueRequest(RequestContext.create(), messageBO);
    }
}
