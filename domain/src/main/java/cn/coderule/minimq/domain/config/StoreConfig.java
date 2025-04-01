package cn.coderule.minimq.domain.config;

import cn.coderule.common.util.lang.SystemUtil;
import cn.coderule.minimq.domain.constant.PermName;
import cn.coderule.minimq.domain.utils.RegistryUtils;
import java.io.File;
import java.io.Serializable;
import lombok.Data;

@Data
public class StoreConfig implements Serializable {

    /**
     * store cluster name
     */
    private String cluster;
    /**
     * store (Master/slave) group name
     */
    private String group;
    /**
     * store group no
     *  - -1 : local
     *  - 0 : master
     *  - 1 ... : slave
     */
    private int groupNo;
    private boolean enableMasterElection = false;
    private boolean inContainer = false;

    private String host;
    private int port = 6888;
    private int haPort = 10912;

    /**
     * broker permission
     * default: Readable and writable
     */
    private int permission = PermName.PERM_READ | PermName.PERM_WRITE;


    private String registryAddress = System.getProperty(
        RegistryUtils.NAMESRV_ADDR_PROPERTY,
        System.getenv(RegistryUtils.NAMESRV_ADDR_ENV)
    );

    private boolean fetchRegistryAddressByDns = false;
    private boolean fetchRegistryAddressByHttp = false;
    private int fetchRegistryAddressInterval = 60 * 1000;
    private int registryTimeout = 24_1000;
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

}
