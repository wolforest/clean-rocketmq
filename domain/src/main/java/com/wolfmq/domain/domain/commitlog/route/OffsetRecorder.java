package com.wolfmq.domain.domain.commitlog.route;

import com.wolfmq.domain.config.store.CommitConfig;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class OffsetRecorder {
    private final CommitConfig config;
    private final OffsetCodec offsetCodec;
    private final ConcurrentMap<Integer, Long> maxOffsetMap;
    private final ConcurrentMap<Integer, Long> minOffsetMap;

    public OffsetRecorder(CommitConfig config) {
        this(config, new OffsetCodec(
                config.getShardingNumber(),
                config.getMaxShardingNumber()
            )
        );
    }

    public OffsetRecorder(CommitConfig config, OffsetCodec offsetCodec) {
        this.config = config;

        this.offsetCodec = offsetCodec;

        this.maxOffsetMap = new ConcurrentHashMap<>();
        this.minOffsetMap = new ConcurrentHashMap<>();
        initOffsetMap();
    }



    public void setMaxOffset(long maxOffset) {
        int shardId = offsetCodec.getShardId(maxOffset);
        maxOffsetMap.merge(shardId, maxOffset, Math::max);
    }

    public void setMinOffset(long minOffset) {
        int shardId = offsetCodec.getShardId(minOffset);
        minOffsetMap.merge(shardId, minOffset, Math::min);
    }

    public boolean belowMinOffset(long offset) {
        int shardId = offsetCodec.getShardId(offset);
        long minOffset = minOffsetMap.get(shardId);
        return offset < minOffset;
    }

    public boolean exceedsMaxOffset(long offset) {
        int shardId = offsetCodec.getShardId(offset);
        long maxOffset = maxOffsetMap.get(shardId);

        return offset > maxOffset;
    }

    private void initOffsetMap() {
        for (int i = 0; i < config.getShardingNumber(); i++) {
            maxOffsetMap.put(i, 0L);
            minOffsetMap.put(i, 0L);
        }
    }
}
