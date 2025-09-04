package cn.coderule.minimq.domain.config.server;

import cn.coderule.common.util.lang.SystemUtil;
import cn.coderule.common.util.net.NetworkUtil;
import cn.coderule.minimq.domain.config.ConfigAttribute;
import cn.coderule.minimq.domain.config.business.MessageConfig;
import cn.coderule.minimq.domain.config.business.TimerConfig;
import cn.coderule.minimq.domain.config.business.TopicConfig;
import cn.coderule.minimq.domain.config.network.RpcClientConfig;
import cn.coderule.minimq.domain.config.store.CommitConfig;
import cn.coderule.minimq.domain.config.store.ConsumeQueueConfig;
import cn.coderule.minimq.domain.config.store.MetaConfig;
import cn.coderule.minimq.domain.core.constant.PermName;
import java.io.File;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class StoreConfig extends ServerIdentity {
    private String host = NetworkUtil.getLocalAddress();
    private int port = ConfigAttribute.STORE_PORT;

    /**
     *
     */
    private long serverReadyTime = 0L;


    /**
     * broker permission
     * default: Readable and writable
     */
    private int permission = PermName.PERM_READ | PermName.PERM_WRITE;


    private String registryAddress = "127.0.0.1:9876";
    // private String registryAddress = System.getProperty(RegistryUtils.NAMESRV_ADDR_PROPERTY, System.getenv(RegistryUtils.NAMESRV_ADDR_ENV));

    private boolean fetchRegistryAddressByDns = false;
    private boolean fetchRegistryAddressByHttp = false;
    private boolean enableRegistryHeartbeat = false;
    private int fetchRegistryAddressInterval = 60 * 1000;
    private int registryTimeout = 24_000;
    private int registryInterval = 30_000;
    private int registryHeartbeatInterval = 1_000;
    private int registryHeartbeatTimeout = 1_000;

    private int bossThreadNum = 1;
    private int workerThreadNum = 3;
    private int businessThreadNum = 8;
    private int callbackThreadNum = 0;

    private int enqueueThreadNum = Math.min(4, SystemUtil.getProcessorNumber());
    private int enqueueQueueCapacity = 10000;
    private int pullThreadNum = SystemUtil.getProcessorNumber() * 2;
    private int pullQueueCapacity = 10000;
    private int adminThreadNum = Math.min(4, SystemUtil.getProcessorNumber());
    private int adminQueueCapacity = 10000;

    private int syncFlushTimeout = 5 * 1000;
    private String rootDir = System.getProperty("user.home") + File.separator + "mq";

    private int schedulerPoolSize = 1;
    private int schedulerShutdownTimeout = 3;

    private boolean enableTransientPool = false;
    private int transientPoolSize = 5;
    private int transientFileSize = 100 * 1024 * 1024;
    private boolean fastFailIfNotExistInTransientPool = true;

    private long stateMachineVersion = 0;

    private boolean enableHA = false;
    private String masterAddress;
    private boolean enableMasterElection = false;

    private boolean refreshMasterAddress = false;
    private boolean refreshHaAddress = false;

    private String haAddress;
    private int haPort = ConfigAttribute.HA_PORT;
    private int haHeartbeatInterval = 5_000;
    private int haHouseKeepingInterval = 20_000;
    private int slaveTimeout = 3_000;

    private boolean enableHaFlowControl = false;
    private long maxHaTransferBytesPerSecond = 100 * 1024 * 1024;
    private int maxSlaveGap = 256 * 1024 * 1024;
    private long maxSlaveDelayTime = 15_000;
    private int maxHaTransferSize = 32 * 1024;

    private MessageConfig messageConfig;
    private TopicConfig topicConfig;

    private CommitConfig commitConfig;
    private ConsumeQueueConfig consumeQueueConfig;
    private TimerConfig timerConfig;
    private MetaConfig metaConfig;

    private RpcClientConfig rpcClientConfig;

    public SocketAddress getHostAddress() {
        return new InetSocketAddress(host, port);
    }
}
