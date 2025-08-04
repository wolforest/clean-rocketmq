package cn.coderule.minimq.domain.config;

import cn.coderule.minimq.domain.core.enums.store.FlushType;

/**
 * static attribute for config
 */
public class Attribute {
    public static int rpcPort = 8080;
    public static int grpcPort = 8081;
    public static int storePort = 6888;
    public static int haPort = 10912;

    public static FlushType flushType;
    public static int mmapFileSize = 100 * 1024 * 1024;
}
