package com.wolfmq.common.domain.offset.commitlog;

import com.wolfmq.common.config.store.CommitConfig;

public class OffsetRecorderFactory {
    private final CommitConfig config;
    private final OffsetCodec offsetCodec;

    public OffsetRecorderFactory(CommitConfig config) {
        this.config = config;
        this.offsetCodec = new OffsetCodec(
            config.getShardingNumber(),
            config.getMaxShardingNumber()
        );
    }

    public OffsetRecorder create() {
        return new OffsetRecorder(config, offsetCodec);
    }
}
