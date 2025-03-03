package cn.coderule.minimq.rpc.registry.protocol;

import cn.coderule.minimq.rpc.common.protocol.RpcSerializable;
import java.util.Map;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ClusterInfo extends RpcSerializable {
    private String clusterName;
    private Map<String/* storeName */, GroupInfo> brokerAddrTable;
    private Map<String/* clusterName */, Set<String/* storeName */>> clusterAddrTable;

}
