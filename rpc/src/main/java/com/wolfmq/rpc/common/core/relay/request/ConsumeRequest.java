package com.wolfmq.rpc.common.core.relay.request;

import com.wolfmq.common.server.broker.RequestContext;
import java.io.Serializable;
import lombok.Data;

@Data
public class ConsumeRequest implements Serializable {
    private RequestContext context;
}
