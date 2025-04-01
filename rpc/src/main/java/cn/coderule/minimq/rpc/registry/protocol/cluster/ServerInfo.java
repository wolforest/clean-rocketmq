package cn.coderule.minimq.rpc.registry.protocol.cluster;

import cn.coderule.minimq.rpc.common.protocol.codec.RpcSerializable;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ServerInfo implements Serializable {
    protected String clusterName;
    protected String groupName;
    protected long groupNo;

    protected String address;
    protected String zoneName;

    protected Integer heartbeatInterval;
    protected Integer heartbeatTimeout;

    protected boolean inContainer = false;
}
