package cn.coderule.minimq.domain.config.server;

import java.io.Serializable;
import lombok.Data;

@Data
public class TaskConfig implements Serializable {
    // embed | shardByQueue | shardByServer
    private String taskMode = "";
}
