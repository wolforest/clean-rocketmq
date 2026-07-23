package cn.coderule.wolfmq.domain.domain.store.infra.kv;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.function.BiConsumer;

public interface KVTable {
    String getName();

    boolean contains(ByteBuffer key);
    ByteBuffer get(ByteBuffer key);
    int get(ByteBuffer key, ByteBuffer value);
    List<ByteBuffer> multiGet(List<ByteBuffer> keys);

    void put(ByteBuffer key, ByteBuffer value);
    void delete(ByteBuffer key);

    void flush();
    KVBatch newBatch();
    void iterate(byte[] prefix, BiConsumer<byte[], byte[]> callback);
}
