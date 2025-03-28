package cn.coderule.minimq.domain.config;

import cn.coderule.minimq.domain.constant.PermName;
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

    private String host;
    private int port = 6888;
    private int haPort = 10912;

    /**
     * broker permission
     * default: Readable and writable
     */
    private int permission = PermName.PERM_READ | PermName.PERM_WRITE;



    private String registryAddress = null;
    private boolean fetchRegistryAddressByDns = false;
    private boolean fetchRegistryAddressByHttp = false;
    private int fetchRegistryAddressInterval = 60 * 1000;
    private int registryHeartbeatInterval = 1_000;


    private int syncFlushTimeout = 5 * 1000;
    private String rootDir = System.getProperty("user.home") + File.separator + "mq";

    private int schedulerPoolSize = 1;
    private int schedulerShutdownTimeout = 3;

    private boolean enableTransientPool = false;
    private int transientPoolSize = 5;
    private int transientFileSize = 100 * 1024 * 1024;
    private boolean fastFailIfNotExistInTransientPool = true;

}
