package cn.coderule.wolfmq.domain.server.heartbeat;

import cn.coderule.wolfmq.domain.domain.meta.DataVersion;
import cn.coderule.wolfmq.domain.server.registry.ServerInfo;
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
