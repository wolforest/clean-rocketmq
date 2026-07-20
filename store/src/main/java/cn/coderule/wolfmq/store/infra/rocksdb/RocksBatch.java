package cn.coderule.wolfmq.store.infra.rocksdb;

import cn.coderule.wolfmq.domain.domain.store.infra.kv.KVBatch;
import java.nio.ByteBuffer;
import org.rocksdb.WriteBatch;

public class RocksBatch implements KVBatch {
    private final WriteBatch batch = new WriteBatch();

    @Override
    public KVBatch put(ByteBuffer key, ByteBuffer value) {
        return null;
    }

    @Override
    public KVBatch merge(ByteBuffer key, ByteBuffer value) {
        return null;
    }

    @Override
    public KVBatch delete(ByteBuffer key) {
        return null;
    }

    @Override
    public KVBatch deleteRange(ByteBuffer startKey, ByteBuffer endKey) {
        return null;
    }

    @Override
    public void commit() {

    }

    @Override
    public void close() throws Exception {
        batch.close();
    }
}
