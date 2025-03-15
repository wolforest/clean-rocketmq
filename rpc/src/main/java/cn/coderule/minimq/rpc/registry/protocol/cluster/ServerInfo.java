package cn.coderule.minimq.rpc.registry.protocol.cluster;

import cn.coderule.minimq.rpc.common.protocol.codec.RpcSerializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ServerInfo extends RpcSerializable {
    protected String clusterName;
    protected String groupName;
    protected long groupNo;

    protected String address;
    protected String zoneName;

    protected long heartbeatInterval;
    protected long heartbeatTimeout;
}
