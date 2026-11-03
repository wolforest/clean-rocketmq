package com.wolfmq.common.server.registry.heartbeat;

import com.wolfmq.common.domain.meta.DataVersion;
import com.wolfmq.common.server.registry.ServerInfo;
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
