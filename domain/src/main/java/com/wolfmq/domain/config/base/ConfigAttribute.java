package com.wolfmq.domain.config.base;

import com.wolfmq.domain.domain.commitlog.flush.FlushType;

/**
 * static attribute for config
 */
public class ConfigAttribute {
    /* socket port attributes */
    public static int RPC_PORT = 8080;
    public static int GRPC_PORT = 8081;
    public static int AMQP_PORT = 5672;
    public static int STORE_PORT = 6888;
    public static int HA_PORT = 10912;

    /* commitlog attributes */
    public static FlushType FLUSH_TYPE = FlushType.SYNC;
    public static boolean ENABLE_COMMITLOG_SHARDING = false;
    public static int COMMITLOG_SHARDING_NUM = 2;
    public static boolean ENABLE_COMMITLOG_CACHE = false;

    /* special attributes*/
    public static boolean ENABLE_DISRUPTOR = false;
    public static int MMAP_FILE_SIZE = 1024 * 1024 * 1024;
}
