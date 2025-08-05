package cn.coderule.minimq.domain.config;

import cn.coderule.minimq.domain.core.enums.store.FlushType;

/**
 * static attribute for config
 */
public class ConfigAttribute {
    public static int RPC_PORT = 8080;
    public static int GRPC_PORT = 8081;
    public static int STORE_PORT = 6888;
    public static int HA_PORT = 10912;

    public static FlushType FLUSH_TYPE = FlushType.SYNC;
    public static int MMAP_FILE_SIZE = 100 * 1024 * 1024;
}
