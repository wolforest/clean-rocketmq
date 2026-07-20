package cn.coderule.wolfmq.store.infra.rocksdb;

import java.io.Serializable;
import lombok.Data;
import org.rocksdb.CompactionOptions;
import org.rocksdb.DBOptions;
import org.rocksdb.FlushOptions;
import org.rocksdb.ReadOptions;
import org.rocksdb.RocksDB;
import org.rocksdb.WriteOptions;

@Data
public class RocksConnection implements Serializable {
    private RocksDB db;
    private DBOptions dbOptions;

    private WriteOptions writeOptions;
    private WriteOptions writeWithWALOptions;

    private ReadOptions readOptions;
    private ReadOptions readByOrderOptions;

    private CompactionOptions compactionOptions;
    private CompactionOptions compactionForRangeOptions;

    private FlushOptions flushOptions;
}
