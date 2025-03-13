package cn.coderule.minimq.domain.config;

import java.io.Serializable;
import lombok.Data;

@Data
public class RegistryConfig implements Serializable {
    private String host = "0.0.0.0";
    private Integer port = 9876;

    private int bossThreadNum = 1;
    private int workerThreadNum = 3;
    private int businessThreadNum = 8;
    private int callbackThreadNum = 0;

    private int routeThreadNum = 8;
    private int processThreadNum = 16;
}
