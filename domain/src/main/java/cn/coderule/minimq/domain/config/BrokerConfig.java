package cn.coderule.minimq.domain.config;

import cn.coderule.common.util.lang.SystemUtil;
import cn.coderule.common.util.net.NetworkUtil;
import java.io.Serializable;
import lombok.Data;

@Data
public class BrokerConfig implements Serializable {
    private String host = NetworkUtil.getLocalAddress();
    private Integer port = 8080;

    /**
     * should be false in production env
     */
    private boolean enableLocalStore = true;

    private int producerThreadNum = SystemUtil.getProcessorNumber();
    private int producerQueueCapacity = 10000;
    private int consumerThreadNum = SystemUtil.getProcessorNumber();
    private int consumerQueueCapacity = 10000;

    private String registryAddress = null;
    private boolean fetchRegistryAddressByDns = false;
    private boolean fetchRegistryAddressByHttp = false;
    private int fetchRegistryAddressInterval = 60 * 1000;
}
