package cn.coderule.wolfmq.store.domain.index;

import cn.coderule.common.util.lang.string.StringUtil;
import cn.coderule.wolfmq.domain.config.server.StoreConfig;
import cn.coderule.wolfmq.domain.config.store.IndexConfig;
import cn.coderule.wolfmq.domain.config.store.StorePath;
import cn.coderule.wolfmq.domain.core.constant.MessageConst;
import cn.coderule.wolfmq.domain.domain.store.domain.index.IndexService;
import cn.coderule.wolfmq.domain.domain.store.domain.index.QueryOffsetResult;
import cn.coderule.wolfmq.store.server.bootstrap.StoreContext;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultIndexService implements IndexService {

    private static final int MAX_TRY_IDX_CREATE = 3;

    private final int maxHashSlotNum;
    private final int maxIndexNum;
    private final String storePath;

    private final ArrayList<IndexFile> indexFileList = new ArrayList<>();
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    public DefaultIndexService() {
        StoreConfig storeConfig = StoreContext.getBean(StoreConfig.class);
        IndexConfig indexConfig = storeConfig.getIndexConfig();
        this.maxHashSlotNum = indexConfig.getMaxHashSlotNum();
        this.maxIndexNum = indexConfig.getMaxIndexNum();
        this.storePath = StorePath.getIndexPath();
    }

    public DefaultIndexService(int maxHashSlotNum, int maxIndexNum, String storePath) {
        this.maxHashSlotNum = maxHashSlotNum;
        this.maxIndexNum = maxIndexNum;
        this.storePath = storePath;
    }

    @Override
    public void buildIndex(String topic, String keys, long phyOffset, long storeTimestamp) {
        if (StringUtil.isBlank(keys)) {
            return;
        }

        String[] keyArray = keys.split(MessageConst.KEY_SEPARATOR);
        for (String key : keyArray) {
            if (StringUtil.isBlank(key)) {
                continue;
            }
            String indexKey = topic + "#" + key.trim();
            IndexFile indexFile = getOrCreateIndexFile(storeTimestamp);
            if (indexFile == null) {
                log.error("buildIndex error, cannot create index file");
                return;
            }
            indexFile.putKey(indexKey, phyOffset, storeTimestamp);
        }
    }

    @Override
    public QueryOffsetResult queryOffset(String topic, String key, int maxNum, long begin, long end) {
        if (StringUtil.isBlank(key)) {
            return new QueryOffsetResult(Collections.emptyList(), 0, 0);
        }

        String indexKey = topic + "#" + key.trim();
        List<Long> result = new ArrayList<>();
        long indexLastUpdateTimestamp = 0;
        long indexLastUpdatePhyOffset = 0;

        readWriteLock.readLock().lock();
        try {
            for (int i = indexFileList.size() - 1; i >= 0; i--) {
                IndexFile indexFile = indexFileList.get(i);

                if (begin > 0 && indexFile.getEndTimestamp() < begin) {
                    continue;
                }
                if (end > 0 && indexFile.getBeginTimestamp() > end) {
                    continue;
                }

                List<Long> offsets = indexFile.selectPhyOffset(indexKey, maxNum - result.size(), begin, end);
                result.addAll(offsets);

                if (indexFile.getEndTimestamp() > indexLastUpdateTimestamp) {
                    indexLastUpdateTimestamp = indexFile.getEndTimestamp();
                    indexLastUpdatePhyOffset = indexFile.getHeader().getEndPhyOffset();
                }

                if (result.size() >= maxNum) {
                    break;
                }
            }
        } finally {
            readWriteLock.readLock().unlock();
        }

        return new QueryOffsetResult(result, indexLastUpdateTimestamp, indexLastUpdatePhyOffset);
    }

    @Override
    public void start() {
        load(true);
    }

    @Override
    public void shutdown() {
        readWriteLock.writeLock().lock();
        try {
            for (IndexFile indexFile : indexFileList) {
                indexFile.flush();
            }
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    @Override
    public boolean load(boolean lastExitOK) {
        File dir = new File(storePath);
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            log.info("no existing index files in {}", storePath);
            return true;
        }

        Arrays.sort(files);

        readWriteLock.writeLock().lock();
        try {
            for (File file : files) {
                if (!file.isFile()) {
                    continue;
                }

                try {
                    IndexFile indexFile = new IndexFile(file.getAbsolutePath(), maxHashSlotNum, maxIndexNum);
                    if (indexFile.load()) {
                        indexFileList.add(indexFile);
                        log.info("loaded index file {}", file.getName());
                    } else {
                        log.error("load index file {} failed, bad data", file.getAbsolutePath());
                        indexFile.destroy();
                    }
                } catch (Exception e) {
                    log.error("load index file {} failed", file.getAbsolutePath(), e);
                }
            }
        } finally {
            readWriteLock.writeLock().unlock();
        }

        return true;
    }

    @Override
    public void deleteExpiredFile(long offset) {
        readWriteLock.writeLock().lock();
        try {
            List<IndexFile> toDelete = new ArrayList<>();
            for (IndexFile indexFile : indexFileList) {
                if (indexFile.getHeader().getEndPhyOffset() < offset) {
                    toDelete.add(indexFile);
                }
            }

            for (IndexFile indexFile : toDelete) {
                indexFileList.remove(indexFile);
                indexFile.destroy();
                log.info("deleted expired index file, endPhyOffset={}", indexFile.getHeader().getEndPhyOffset());
            }
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    @Override
    public void destroy() {
        readWriteLock.writeLock().lock();
        try {
            for (IndexFile indexFile : indexFileList) {
                indexFile.destroy();
            }
            indexFileList.clear();
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    @Override
    public long getTotalSize() {
        readWriteLock.readLock().lock();
        try {
            long totalSize = 0;
            for (IndexFile indexFile : indexFileList) {
                totalSize += indexFile.getMappedFile().getFileSize();
            }
            return totalSize;
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    public List<IndexFile> getIndexFileList() {
        readWriteLock.readLock().lock();
        try {
            return new ArrayList<>(indexFileList);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    private IndexFile getOrCreateIndexFile(long storeTimestamp) {
        readWriteLock.writeLock().lock();
        try {
            if (!indexFileList.isEmpty()) {
                IndexFile lastFile = indexFileList.get(indexFileList.size() - 1);
                if (!lastFile.isFull()) {
                    return lastFile;
                }
            }

            for (int i = 0; i < MAX_TRY_IDX_CREATE; i++) {
                try {
                    String fileName = storePath + File.separator + storeTimestamp;
                    IndexFile indexFile = new IndexFile(fileName, maxHashSlotNum, maxIndexNum);
                    indexFileList.add(indexFile);
                    return indexFile;
                } catch (Exception e) {
                    log.error("create index file error, retry {} times", i + 1, e);
                }
            }
            return null;
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }
}