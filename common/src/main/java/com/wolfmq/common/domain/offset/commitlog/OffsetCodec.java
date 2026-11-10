package com.wolfmq.common.domain.offset.commitlog;

import lombok.Getter;

public class OffsetCodec {
    @Getter
    private final int shardId;
    private final int maxShardingNumber;

    public OffsetCodec(int maxShardingNumber) {
        this(0, maxShardingNumber);
    }

    public OffsetCodec(int shardId, int maxShardingNumber) {
        this.shardId = shardId;
        this.maxShardingNumber = maxShardingNumber;
    }

    public long encode(long offset) {
        if (offset < 0) {
            throw new IllegalArgumentException("offset can't be negative");
        }

        return offset * maxShardingNumber + shardId;
    }

    public long decode(long offset) {
        if (offset < 0) {
            throw new IllegalArgumentException("offset can't be negative");
        }

        return offset / maxShardingNumber;
    }

    /**
     * @renamed from getShardId to shardIdOf
     */
    public int shardIdOf(long offset) {
        if (offset < 0) {
            throw new IllegalArgumentException("offset can't be negative");
        }

        return (int) (offset % maxShardingNumber);
    }
}
