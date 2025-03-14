package cn.coderule.minimq.rpc.registry.protocol.cluster;

import cn.coderule.minimq.rpc.common.protocol.codec.RpcSerializable;
import java.util.Map;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ClusterInfo extends RpcSerializable {
    private String clusterName;
    private Map<String/* groupName */, GroupInfo> groupMap;
    private Map<String/* clusterName */, Set<String/* storeName */>> serverMap;

}
