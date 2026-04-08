package cn.coderule.wolfmq.rpc.common.core.relay.request;

import cn.coderule.wolfmq.domain.domain.cluster.RequestContext;
import java.io.Serializable;
import lombok.Data;

@Data
public class ConsumeRequest implements Serializable {
    private RequestContext context;
}
