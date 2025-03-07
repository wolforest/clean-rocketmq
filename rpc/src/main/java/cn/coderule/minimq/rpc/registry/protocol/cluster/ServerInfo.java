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
    private String cluster;
    private String group;
    private long groupNo;

    private String address;
    private String zoneName;
}
