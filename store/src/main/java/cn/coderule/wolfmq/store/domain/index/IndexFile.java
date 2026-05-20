package cn.coderule.wolfmq.store.domain.index;

import cn.coderule.wolfmq.domain.domain.store.infra.MappedFile;
import cn.coderule.wolfmq.store.infra.file.DefaultMappedFile;
import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class IndexFile {

    public static final int HASH_SLOT_SIZE = 4;
    public static final int INDEX_SIZE = 20;
    public static final int INVALID_INDEX = 0;
    public static final int HEADER_SIZE = 40;

    private final int hashSlotNum;
    private final int indexNum;
    private final MappedFile mappedFile;
    private final IndexHeader header;

    public IndexFile(String fileName, int hashSlotNum, int indexNum) throws Exception {
        this.hashSlotNum = hashSlotNum;
        this.indexNum = indexNum;
        int fileTotalSize = HEADER_SIZE + hashSlotNum * HASH_SLOT_SIZE + indexNum * INDEX_SIZE;
        this.mappedFile = new DefaultMappedFile(fileName, fileTotalSize);
        this.header = new IndexHeader();
        this.header.setHashSlotCount(hashSlotNum);
    }

    public IndexFile(MappedFile mappedFile, int hashSlotNum) {
        this.mappedFile = mappedFile;
        this.hashSlotNum = hashSlotNum;
        this.indexNum = (mappedFile.getFileSize() - HEADER_SIZE - hashSlotNum * HASH_SLOT_SIZE) / INDEX_SIZE;
        this.header = readHeader();
    }

    public boolean load() {
        IndexHeader loaded = readHeader();
        if (loaded.getHashSlotCount() != this.hashSlotNum) {
            log.error("index file hashSlotCount not match, expected={}, actual={}, file={}",
                this.hashSlotNum, loaded.getHashSlotCount(), mappedFile.getFileName());
            return false;
        }
        this.header.setBeginTimestamp(loaded.getBeginTimestamp());
        this.header.setEndTimestamp(loaded.getEndTimestamp());
        this.header.setBeginPhyOffset(loaded.getBeginPhyOffset());
        this.header.setEndPhyOffset(loaded.getEndPhyOffset());
        this.header.setHashSlotCount(loaded.getHashSlotCount());
        this.header.setIndexCount(loaded.getIndexCount());
        return true;
    }

    public boolean putKey(String key, long phyOffset, long storeTimestamp) {
        if (key == null || key.isEmpty()) {
            return false;
        }

        int keyHash = indexKeyHash(key);
        int slotPos = keyHash % hashSlotNum;

        int prevSlotValue = readInt(HEADER_SIZE + slotPos * HASH_SLOT_SIZE);

        int currentIndexCount = header.getIndexCount();
        int indexPos = HEADER_SIZE + hashSlotNum * HASH_SLOT_SIZE + currentIndexCount * INDEX_SIZE;

        if (indexPos + INDEX_SIZE > mappedFile.getFileSize()) {
            log.warn("index file is full, fileName={}", mappedFile.getFileName());
            return false;
        }

        int timeDiff = 0;
        if (header.getBeginTimestamp() > 0) {
            timeDiff = (int) ((storeTimestamp - header.getBeginTimestamp()) / 1000);
        } else {
            header.setBeginTimestamp(storeTimestamp);
        }

        MappedByteBuffer byteBuffer = mappedFile.getMappedByteBuffer();

        byteBuffer.putInt(indexPos, keyHash);
        byteBuffer.putLong(indexPos + 4, phyOffset);
        byteBuffer.putInt(indexPos + 12, timeDiff);
        byteBuffer.putInt(indexPos + 16, prevSlotValue);

        // Use 1-based indexing for slot: store currentIndexCount + 1 so INVALID_INDEX (0) means empty
        byteBuffer.putInt(HEADER_SIZE + slotPos * HASH_SLOT_SIZE, currentIndexCount + 1);

        header.incrementIndexCount();
        header.updateTimeDiff(storeTimestamp);
        header.updatePhyOffset(phyOffset);
        writeHeader(byteBuffer);

        return true;
    }

    public List<Long> selectPhyOffset(String key, int maxNum, long begin, long end) {
        if (key == null || key.isEmpty()) {
            return new ArrayList<>();
        }

        int keyHash = indexKeyHash(key);
        int slotPos = keyHash % hashSlotNum;

        int slotValue = readInt(HEADER_SIZE + slotPos * HASH_SLOT_SIZE);
        if (slotValue <= INVALID_INDEX) {
            return new ArrayList<>();
        }

        List<Long> phyOffsets = new ArrayList<>();
        MappedByteBuffer byteBuffer = mappedFile.getMappedByteBuffer();

        int nextSlotValue = slotValue;
        for (int i = 0; i < indexNum; i++) {
            // Convert 1-based slot value to 0-based entry index
            int entryIndex = nextSlotValue - 1;
            int indexPos = HEADER_SIZE + hashSlotNum * HASH_SLOT_SIZE + entryIndex * INDEX_SIZE;

            if (indexPos < 0 || indexPos + INDEX_SIZE > mappedFile.getFileSize()) {
                break;
            }

            int readKeyHash = byteBuffer.getInt(indexPos);
            long readPhyOffset = byteBuffer.getLong(indexPos + 4);
            int readTimeDiff = byteBuffer.getInt(indexPos + 12);
            int readSlotValue = byteBuffer.getInt(indexPos + 16);

            if (readKeyHash == keyHash) {
                long readTimestamp = header.getBeginTimestamp() + (long) readTimeDiff * 1000;
                if (begin <= 0 && end <= 0) {
                    phyOffsets.add(readPhyOffset);
                } else if (readTimestamp >= begin && readTimestamp <= end) {
                    phyOffsets.add(readPhyOffset);
                }
            }

            if (phyOffsets.size() >= maxNum) {
                break;
            }

            if (readSlotValue <= INVALID_INDEX) {
                break;
            }

            nextSlotValue = readSlotValue;
        }

        return phyOffsets;
    }

    public List<Long> scanByTimeRange(long begin, long end) {
        List<Long> phyOffsets = new ArrayList<>();
        MappedByteBuffer byteBuffer = mappedFile.getMappedByteBuffer();

        for (int i = 0; i < header.getIndexCount(); i++) {
            int indexPos = HEADER_SIZE + hashSlotNum * HASH_SLOT_SIZE + i * INDEX_SIZE;
            if (indexPos + INDEX_SIZE > mappedFile.getFileSize()) {
                break;
            }

            int readTimeDiff = byteBuffer.getInt(indexPos + 12);
            long readTimestamp = header.getBeginTimestamp() + (long) readTimeDiff * 1000;

            if (readTimestamp >= begin && readTimestamp <= end) {
                long readPhyOffset = byteBuffer.getLong(indexPos + 4);
                phyOffsets.add(readPhyOffset);
            }
        }

        return phyOffsets;
    }

    public boolean isFull() {
        return header.getIndexCount() >= indexNum;
    }

    public long getBeginTimestamp() {
        return header.getBeginTimestamp();
    }

    public long getEndTimestamp() {
        return header.getEndTimestamp();
    }

    public void flush() {
        writeHeader(mappedFile.getMappedByteBuffer());
        mappedFile.flush(0);
    }

    public void destroy() {
        mappedFile.destroy();
    }

    private int indexKeyHash(String key) {
        int keyHash = key.hashCode();
        return keyHash < 0 ? Math.abs(keyHash) : keyHash;
    }

    private int readInt(int pos) {
        MappedByteBuffer byteBuffer = mappedFile.getMappedByteBuffer();
        return byteBuffer.getInt(pos);
    }

    private void writeHeader(MappedByteBuffer byteBuffer) {
        byteBuffer.putLong(0, header.getBeginTimestamp());
        byteBuffer.putLong(8, header.getEndTimestamp());
        byteBuffer.putLong(16, header.getBeginPhyOffset());
        byteBuffer.putLong(24, header.getEndPhyOffset());
        byteBuffer.putInt(32, header.getHashSlotCount());
        byteBuffer.putInt(36, header.getIndexCount());
    }

    private IndexHeader readHeader() {
        IndexHeader hdr = new IndexHeader();
        MappedByteBuffer byteBuffer = mappedFile.getMappedByteBuffer();
        hdr.setBeginTimestamp(byteBuffer.getLong(0));
        hdr.setEndTimestamp(byteBuffer.getLong(8));
        hdr.setBeginPhyOffset(byteBuffer.getLong(16));
        hdr.setEndPhyOffset(byteBuffer.getLong(24));
        hdr.setHashSlotCount(byteBuffer.getInt(32));
        hdr.setIndexCount(byteBuffer.getInt(36));
        return hdr;
    }
}