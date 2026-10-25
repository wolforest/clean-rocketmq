package cn.coderule.wolfmq.domain.domain.mq.dto;

import cn.coderule.wolfmq.domain.server.RequestContext;
import cn.coderule.wolfmq.domain.domain.message.MessageBO;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnqueueRequest implements Serializable {
    private RequestContext requestContext;
    private String storeGroup;
    private MessageBO messageBO;

    public static EnqueueRequest create(MessageBO messageBO) {
        return EnqueueRequest.builder()
            .requestContext(RequestContext.create())
            .messageBO(messageBO)
            .build();
    }
}
