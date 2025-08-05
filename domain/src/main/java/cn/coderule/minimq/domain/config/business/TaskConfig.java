package cn.coderule.minimq.domain.config.business;

import java.io.Serializable;
import lombok.Data;

@Data
public class TaskConfig implements Serializable {
    // embed | shardByQueue | shardByServer
    // bind | preempt | assign
    /**
     * taskMode:
     * 1. embed:
     * 2. binding:
     * 3. sharding:
     */
    private String taskMode = "";
}
