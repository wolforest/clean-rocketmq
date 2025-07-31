package cn.coderule.minimq.domain.config.server;

import cn.coderule.common.util.lang.SystemUtil;
import cn.coderule.common.util.lang.string.StringUtil;
import cn.coderule.common.util.net.NetworkUtil;
import cn.coderule.minimq.domain.config.message.MessageConfig;
import cn.coderule.minimq.domain.config.TimerConfig;
import cn.coderule.minimq.domain.config.message.TopicConfig;
import cn.coderule.minimq.domain.config.TransactionConfig;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@EqualsAndHashCode(callSuper = true)
public class BrokerConfig extends ServerIdentity implements Serializable {
    private String host = NetworkUtil.getLocalAddress();
    private Integer port = 8080;

    private boolean enableTrace = false;
    private boolean enableEmbedStore = true;
    private boolean enableRemoteStore = true;

    private long channelExpireTime = 120_000;
    private long subscriptionExpireTime = 600_000;
    private int maxChannelFetchTimes = 3;
    private int maxChannelRetryTimes = 3;

    private int scanIdleChannelsInterval = 10_000;
    private int scanIdleChannelsDelay = 10_000;

    private int producerThreadNum = SystemUtil.getProcessorNumber();
    private int producerQueueCapacity = 10000;
    private int consumerThreadNum = SystemUtil.getProcessorNumber();
    private int consumerQueueCapacity = 10000;

    private boolean enableBrokerRegister = false;
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
    private TaskConfig taskConfig;

    public boolean isEnableRegister() {
        if (!enableBrokerRegister) {
            return false;
        }

        if (StringUtil.notBlank(registryAddress)) {
            return true;
        }

        log.error("registryAddress for broker can't be blank");
        return false;
    }
}
