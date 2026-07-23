package cn.coderule.wolfmq.domain.domain.store.infra.kv;

import java.nio.ByteBuffer;

public interface KVBatch extends AutoCloseable{
    KVBatch put(ByteBuffer key, ByteBuffer value);
    KVBatch merge(ByteBuffer key, ByteBuffer value);

    KVBatch delete(ByteBuffer key);
    KVBatch deleteRange(ByteBuffer startKey, ByteBuffer endKey);

    void commit();
}
