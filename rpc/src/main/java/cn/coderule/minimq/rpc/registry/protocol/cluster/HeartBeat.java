package cn.coderule.minimq.rpc.registry.protocol.cluster;

import cn.coderule.minimq.domain.domain.model.meta.DataVersion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class HeartBeat extends ServerInfo {
    private String role;
    private DataVersion version;

    private int timeout;
}
