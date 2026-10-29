package com.wolfmq.domain.config.base;

import com.wolfmq.domain.domain.commitlog.flush.FlushType;
import lombok.extern.slf4j.Slf4j;

/**
 * convert Common template to ConfigAttribute
 *  - apply non-null fields to static ConfigAttribute
 */
@Slf4j
public class CommonConverter {

    public static void convert(Common common) {
        if (null == common) {
            return;
        }

        convertPortInfo(common);
        convertCommitLogInfo(common);
        convertSpecialInfo(common);
    }

    private static void convertPortInfo(Common common) {
        if (null != common.getRpcPort()) {
            ConfigAttribute.RPC_PORT = common.getRpcPort();
        }
        if (null != common.getGrpcPort()) {
            ConfigAttribute.GRPC_PORT = common.getGrpcPort();
        }
        if (null != common.getAmqpPort()) {
            ConfigAttribute.AMQP_PORT = common.getAmqpPort();
        }
        if (null != common.getStorePort()) {
            ConfigAttribute.STORE_PORT = common.getStorePort();
        }
        if (null != common.getHaPort()) {
            ConfigAttribute.HA_PORT = common.getHaPort();
        }
    }

    private static void convertCommitLogInfo(Common common) {
        if (null != common.getFlushType()) {
            ConfigAttribute.FLUSH_TYPE = parseFlushType(common.getFlushType());
        }
        if (null != common.getEnableCommitLogSharding()) {
            ConfigAttribute.ENABLE_COMMITLOG_SHARDING = common.getEnableCommitLogSharding();
        }
        if (null != common.getCommitLogShardingNum()) {
            ConfigAttribute.COMMITLOG_SHARDING_NUM = common.getCommitLogShardingNum();
        }
        if (null != common.getEnableCommitLogCache()) {
            ConfigAttribute.ENABLE_COMMITLOG_CACHE = common.getEnableCommitLogCache();
        }
    }

    private static void convertSpecialInfo(Common common) {
        if (null != common.getEnableDisruptor()) {
            ConfigAttribute.ENABLE_DISRUPTOR = common.getEnableDisruptor();
        }
        if (null != common.getMmapFileSize()) {
            ConfigAttribute.MMAP_FILE_SIZE = common.getMmapFileSize();
        }
    }

    private static FlushType parseFlushType(String flushType) {
        try {
            return FlushType.valueOf(flushType.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("invalid flushType: {}, keep default: {}", flushType, ConfigAttribute.FLUSH_TYPE);
            return ConfigAttribute.FLUSH_TYPE;
        }
    }
}
