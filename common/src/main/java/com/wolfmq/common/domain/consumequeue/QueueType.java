
package com.wolfmq.common.domain.consumequeue;

public enum QueueType {
    FILE,
    BATCH, // used in v4.*, useless after v5.*
    ROCKSDB,
    ERROR,
}
