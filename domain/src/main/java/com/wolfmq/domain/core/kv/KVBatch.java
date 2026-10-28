package com.wolfmq.domain.core.kv;

import java.nio.ByteBuffer;

public interface KVBatch extends AutoCloseable{
    KVBatch put(ByteBuffer key, ByteBuffer value);
    KVBatch merge(ByteBuffer key, ByteBuffer value);

    KVBatch delete(ByteBuffer key);
    KVBatch deleteRange(ByteBuffer startKey, ByteBuffer endKey);

    void commit();
}
