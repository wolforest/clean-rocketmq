package com.wolfmq.rpc.common.core.relay.request;

import com.wolfmq.domain.server.RequestContext;
import java.io.Serializable;
import lombok.Data;

@Data
public class ConsumeRequest implements Serializable {
    private RequestContext context;
}
