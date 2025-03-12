package cn.coderule.minimq.domain.config;

import cn.coderule.common.util.net.NetworkUtil;
import java.io.Serializable;
import lombok.Data;

@Data
public class RegistryConfig implements Serializable {
    private String host = NetworkUtil.getLocalAddress();
    private Integer port = 8081;
}
