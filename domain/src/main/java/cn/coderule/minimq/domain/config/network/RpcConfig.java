package cn.coderule.minimq.domain.config.network;

import cn.coderule.common.util.net.NetworkUtil;
import cn.coderule.minimq.domain.config.ConfigAttribute;
import java.io.Serializable;
import lombok.Data;

@Data
public class RpcConfig implements Serializable {
    private String host = NetworkUtil.getLocalAddress();
    private int port = ConfigAttribute.RPC_PORT;
}
