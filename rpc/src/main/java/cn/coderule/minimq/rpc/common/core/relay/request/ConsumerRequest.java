package cn.coderule.minimq.rpc.common.core.relay.request;

import cn.coderule.minimq.domain.domain.cluster.RequestContext;
import java.io.Serializable;
import lombok.Data;

@Data
public class ConsumerRequest implements Serializable {
    private RequestContext context;
}
