package cn.coderule.minimq.store.infra.file;

import cn.coderule.common.util.lang.string.StringUtil;
import cn.coderule.minimq.domain.domain.store.infra.MappedFile;
import cn.coderule.minimq.domain.domain.store.infra.MappedFileQueue;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Multi-directory MappedFileQueue implementation.
 * <p>
 * Composes multiple {@link DefaultMappedFileQueue} instances (one per directory),
 * unified behind the {@link MappedFileQueue} interface with a pluggable
 * directory selection strategy for new file creation.
 * <p>
 * All MappedFiles across directories share a global offset space.
 * The default strategy is round-robin.
 */
@Slf4j
public class MultiDirMappedFileQueue implements MappedFileQueue {
    private final List<String> dirs;
    private final int fileSize;

    /**
     * -- GETTER --
     *  Get the sub-queues (for testing or advanced usage).
     */
    @Getter private final List<DefaultMappedFileQueue> queues;
    private final AtomicInteger roundRobinIndex = new AtomicInteger(0);

    public MultiDirMappedFileQueue(List<String> dirs, int fileSize) {
        this(dirs, fileSize, null);
    }

    public MultiDirMappedFileQueue(List<String> dirs, int fileSize,
        AllocateMappedFileService allocateMappedFileService) {

        if (dirs == null || dirs.isEmpty()) {
            throw new IllegalArgumentException("dirs must not be empty");
        }

        this.dirs = dirs;
        this.fileSize = fileSize;

        this.queues = new ArrayList<>(dirs.size());
        for (String dir : dirs) {
            queues.add(new DefaultMappedFileQueue(dir, fileSize, allocateMappedFileService));
        }
    }

    // ==================== Lifecycle ====================

    @Override
    public String getRootDir() {
        return StringUtil.joinWith(StringUtil.COLON, dirs);
    }

    @Override
    public boolean load() {
        for (DefaultMappedFileQueue queue : queues) {
            if (!queue.load()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void checkSelf() {
        for (DefaultMappedFileQueue queue : queues) {
            queue.checkSelf();
        }
    }

    @Override
    public void shutdown(long interval) {
        for (DefaultMappedFileQueue queue : queues) {
            queue.shutdown(interval);
        }
    }

    @Override
    public void destroy() {
        for (DefaultMappedFileQueue queue : queues) {
            queue.destroy();
        }
    }

    // ==================== Collection ====================

    @Override
    public boolean isEmpty() {
        for (DefaultMappedFileQueue queue : queues) {
            if (!queue.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int size() {
        int total = 0;
        for (DefaultMappedFileQueue queue : queues) {
            total += queue.size();
        }
        return total;
    }

    @Override
    public List<MappedFile> getMappedFiles() {
        List<MappedFile> all = new ArrayList<>();
        for (DefaultMappedFileQueue queue : queues) {
            all.addAll(queue.getMappedFiles());
        }
        all.sort(Comparator.comparingLong(MappedFile::getMinOffset));
        return all;
    }

    @Override
    public MappedFile getMappedFileByIndex(int index) {
        List<MappedFile> all = getMappedFiles();
        if (index < 0 || index >= all.size()) {
            return null;
        }
        return all.get(index);
    }

    @Override
    public void setFileMode(int mode) {
        for (DefaultMappedFileQueue queue : queues) {
            queue.setFileMode(mode);
        }
    }

    // ==================== Create & Get ====================

    @Override
    public MappedFile createMappedFileByStartOffset(long startOffset) {
        DefaultMappedFileQueue target = nextQueue();
        return target.createMappedFileByStartOffset(startOffset);
    }

    @Override
    public MappedFile getOrCreateMappedFileForSize(int messageSize) {
        MappedFile last = getLastMappedFile();
        if (last != null && last.canWrite(messageSize)) {
            return last;
        }

        long nextOffset = last == null ? 0 : last.getMinOffset() + this.fileSize;
        DefaultMappedFileQueue target = nextQueue();
        return target.createMappedFileByStartOffset(nextOffset);
    }

    @Override
    public MappedFile getOrCreateMappedFileForOffset(long offset) {
        MappedFile last = getLastMappedFile();

        if (null == last || !last.containsOffset(offset)) {
            long fileOffset = offset - offset % fileSize;
            DefaultMappedFileQueue target = nextQueue();
            return target.createMappedFileByStartOffset(fileOffset);
        }

        if (!last.isFull()) {
            return last;
        }

        long fileOffset = last.getMinOffset() + this.fileSize;
        DefaultMappedFileQueue target = nextQueue();
        return target.createMappedFileByStartOffset(fileOffset);
    }

    @Override
    public MappedFile getMappedFileByOffset(long offset) {
        for (DefaultMappedFileQueue queue : queues) {
            MappedFile file = queue.getMappedFileByOffset(offset);
            if (file != null) {
                return file;
            }
        }
        return null;
    }

    @Override
    public MappedFile getFirstMappedFile() {
        MappedFile first = null;
        for (DefaultMappedFileQueue queue : queues) {
            MappedFile queueFirst = queue.getFirstMappedFile();
            if (queueFirst == null) {
                continue;
            }
            if (first == null || queueFirst.getMinOffset() < first.getMinOffset()) {
                first = queueFirst;
            }
        }
        return first;
    }

    @Override
    public MappedFile getLastMappedFile() {
        MappedFile last = null;
        for (DefaultMappedFileQueue queue : queues) {
            MappedFile queueLast = queue.getLastMappedFile();
            if (queueLast == null) {
                continue;
            }
            if (last == null || queueLast.getMinOffset() > last.getMinOffset()) {
                last = queueLast;
            }
        }
        return last;
    }

    // ==================== Remove ====================

    @Override
    public void removeMappedFile(MappedFile mappedFile) {
        for (DefaultMappedFileQueue queue : queues) {
            if (queue.getMappedFiles().contains(mappedFile)) {
                queue.removeMappedFile(mappedFile);
                return;
            }
        }
    }

    @Override
    public void removeMappedFiles(List<MappedFile> files) {
        for (MappedFile file : files) {
            removeMappedFile(file);
        }
    }

    // ==================== Offset ====================

    @Override
    public long getMinOffset() {
        MappedFile first = getFirstMappedFile();
        return first == null ? 0 : first.getMinOffset();
    }

    @Override
    public long getMaxOffset() {
        MappedFile last = getLastMappedFile();
        if (last == null) {
            return 0;
        }
        return last.getMinOffset() + last.getInsertPosition();
    }

    @Override
    public long getCommitPosition() {
        long minCommit = Long.MAX_VALUE;
        for (DefaultMappedFileQueue queue : queues) {
            if (!queue.isEmpty()) {
                minCommit = Math.min(minCommit, queue.getCommitPosition());
            }
        }
        return minCommit == Long.MAX_VALUE ? 0 : minCommit;
    }

    @Override
    public long getFlushPosition() {
        long minFlush = Long.MAX_VALUE;
        for (DefaultMappedFileQueue queue : queues) {
            if (!queue.isEmpty()) {
                minFlush = Math.min(minFlush, queue.getFlushPosition());
            }
        }
        return minFlush == Long.MAX_VALUE ? 0 : minFlush;
    }

    @Override
    public long getStoreTimestamp() {
        long maxTimestamp = 0;
        for (DefaultMappedFileQueue queue : queues) {
            maxTimestamp = Math.max(maxTimestamp, queue.getStoreTimestamp());
        }
        return maxTimestamp;
    }

    @Override
    public long getUnCommittedSize() {
        return getMaxOffset() - getCommitPosition();
    }

    @Override
    public long getUnFlushedSize() {
        return getMaxOffset() - getFlushPosition();
    }

    // ==================== Flush & Commit ====================

    @Override
    public boolean flush(int minPages) {
        boolean allFlushed = true;
        for (DefaultMappedFileQueue queue : queues) {
            if (!queue.isEmpty()) {
                boolean result = queue.flush(minPages);
                allFlushed = allFlushed && result;
            }
        }
        return allFlushed;
    }

    @Override
    public boolean commit(int minPages) {
        boolean allCommitted = true;
        for (DefaultMappedFileQueue queue : queues) {
            if (!queue.isEmpty()) {
                boolean result = queue.commit(minPages);
                allCommitted = allCommitted && result;
            }
        }
        return allCommitted;
    }

    // ==================== Internal ====================

    /**
     * Round-robin directory selection for new file creation.
     */
    private DefaultMappedFileQueue nextQueue() {
        int index = Math.abs(roundRobinIndex.getAndIncrement() % queues.size());
        return queues.get(index);
    }

}
