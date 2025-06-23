package cn.coderule.minimq.domain.config.server;

import java.io.Serializable;
import lombok.Data;

@Data
public class TaskConfig implements Serializable {
    // embed | shardByQueue | shardByServer
    // bind | preempt | assign
    /**
     * taskMode:
     * 1. bind:
     *   1.1 embed
     *   1.2 static binding
     *   1.3 dynamic binding
     * 2. preempt:
     * 3. assign:
     */
    private String taskMode = "";
}
