package cn.coderule.minimq.store.server.ha.server;

import cn.coderule.minimq.store.server.ha.server.processor.SlaveMonitor;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionContext implements Serializable {
    private ConnectionPool connectionPool;
    private SlaveMonitor slaveMonitor;
}
