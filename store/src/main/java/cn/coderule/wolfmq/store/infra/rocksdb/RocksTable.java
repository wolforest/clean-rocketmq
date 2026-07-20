package cn.coderule.wolfmq.store.infra.rocksdb;

import cn.coderule.wolfmq.domain.domain.store.infra.kv.KVBatch;
import cn.coderule.wolfmq.domain.domain.store.infra.kv.KVTable;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.function.BiConsumer;
import lombok.Getter;

public class RocksTable implements KVTable {
    @Getter
    private final String name;
    private final RocksConnection connection;

    public RocksTable(RocksConnection connection, String name) {
        this.connection = connection;
        this.name = name;
    }

    @Override
    public boolean contains(ByteBuffer key) {
        return false;
    }

    @Override
    public ByteBuffer get(ByteBuffer key) {
        return null;
    }

    @Override
    public int get(ByteBuffer key, ByteBuffer value) {
        return 0;
    }

    @Override
    public List<ByteBuffer> multiGet(List<ByteBuffer> keys) {
        return List.of();
    }

    @Override
    public void put(ByteBuffer key, ByteBuffer value) {

    }

    @Override
    public void delete(ByteBuffer key) {

    }

    @Override
    public void flush() {

    }

    @Override
    public KVBatch newBatch() {
        return null;
    }

    @Override
    public void iterate(byte[] prefix, BiConsumer<byte[], byte[]> callback) {

    }
}
