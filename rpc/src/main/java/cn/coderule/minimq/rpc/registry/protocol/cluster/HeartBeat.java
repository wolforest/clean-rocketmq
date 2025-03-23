package cn.coderule.minimq.rpc.registry.protocol.cluster;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class HeartBeat extends ServerInfo {
    private String role;

    private int timeout;
}
