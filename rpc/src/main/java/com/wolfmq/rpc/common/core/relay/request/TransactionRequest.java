package com.wolfmq.rpc.common.core.relay.request;

import com.wolfmq.domain.server.RequestContext;
import com.wolfmq.domain.domain.message.MessageBO;
import java.io.Serializable;
import lombok.Data;

@Data
public class TransactionRequest implements Serializable {
    private RequestContext context;
    private MessageBO messageBO;

    public static TransactionRequest build(MessageBO messageBO) {
        TransactionRequest request = new TransactionRequest();
        request.setContext(RequestContext.create());
        request.setMessageBO(messageBO);
        return request;
    }
}
