package cn.coderule.minimq.domain.config;

import cn.coderule.common.util.lang.SystemUtil;
import cn.coderule.common.util.net.NetworkUtil;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BrokerConfig extends ServerIdentity implements Serializable {
    private String host = NetworkUtil.getLocalAddress();
    private Integer port = 8080;

    private boolean enableTrace = false;
    private boolean enableEmbedStore = true;
    private boolean enableRemoteStore = true;

    private long channelExpireTime = 120_000;
    private long subscriptionExpireTime = 600_000;

    private int producerThreadNum = SystemUtil.getProcessorNumber();
    private int producerQueueCapacity = 10000;
    private int consumerThreadNum = SystemUtil.getProcessorNumber();
    private int consumerQueueCapacity = 10000;

    private String registryAddress = "127.0.0.1:9876";
    // private String registryAddress = System.getProperty(RegistryUtils.NAMESRV_ADDR_PROPERTY, System.getenv(RegistryUtils.NAMESRV_ADDR_ENV));
    private boolean fetchRegistryAddressByDns = false;
    private boolean fetchRegistryAddressByHttp = false;
    private boolean enableRegistryHeartbeat = false;
    private int fetchRegistryAddressInterval = 60 * 1000;
    private int registryTimeout = 24_000;
    private int registryHeartbeatInterval = 1_000;
    private int registryHeartbeatTimeout = 1_000;

    private int syncRouteTimeout = 3_000;
    private int syncRouteInterval = 30_000;

    private long serverReadyTime = 0L;

    private MessageConfig messageConfig;
    private TopicConfig topicConfig;
    private GrpcConfig grpcConfig;
    private TimerConfig timerConfig;
    private TransactionConfig transactionConfig;
}
