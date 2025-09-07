package cn.coderule.minimq.domain.domain.cluster.server;

import cn.coderule.minimq.domain.domain.meta.DataVersion;
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
