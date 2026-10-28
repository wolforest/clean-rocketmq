package com.wolfmq.domain.server.heartbeat;

import com.wolfmq.domain.domain.meta.DataVersion;
import com.wolfmq.domain.server.registry.ServerInfo;
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
