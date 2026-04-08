package cn.coderule.wolfmq.store.domain.commitlog.sharding;

public class OffsetCodec {
    private final int shardId;
    private final int maxShardingNumber;

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

        return (offset - shardId) / maxShardingNumber;
    }
}
