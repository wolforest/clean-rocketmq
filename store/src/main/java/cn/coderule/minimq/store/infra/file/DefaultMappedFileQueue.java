package cn.coderule.minimq.store.infra.file;

import cn.coderule.common.util.io.FileUtil;
import cn.coderule.minimq.domain.service.store.infra.MappedFile;
import cn.coderule.minimq.domain.service.store.infra.MappedFileQueue;
import cn.coderule.minimq.domain.utils.StoreUtils;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultMappedFileQueue implements MappedFileQueue {
    private final String rootDir;
    private final int fileSize;

    private final AllocateMappedFileService allocateMappedFileService;
    @Getter
    private final CopyOnWriteArrayList<MappedFile> mappedFiles = new CopyOnWriteArrayList<>();

    @Getter
    private long flushPosition;
    @Getter
    private long commitPosition;

    /**
     * updated after flushing
     */
    @Getter
    private volatile long storeTimestamp;

    public DefaultMappedFileQueue(String rootDir, int fileSize) {
        this(rootDir, fileSize, null);
    }

    public DefaultMappedFileQueue(String rootDir, int fileSize, AllocateMappedFileService allocateMappedFileService) {
        this.rootDir = rootDir;
        this.fileSize = fileSize;
        this.allocateMappedFileService = allocateMappedFileService;
    }

    @Override
    public boolean load() {
        File dir = new File(this.rootDir);
        File[] ls = dir.listFiles();
        if (ls != null) {
            return loadFiles(Arrays.asList(ls));
        }
        return true;
    }

    @Override
    public void checkSelf() {
        List<MappedFile> mappedFiles = new ArrayList<>(this.mappedFiles);
        if (mappedFiles.isEmpty()) {
            return;
        }

        Iterator<MappedFile> iterator = mappedFiles.iterator();
        MappedFile pre = null;
        while (iterator.hasNext()) {
            MappedFile cur = iterator.next();
            checkFileOffset(pre, cur);

            pre = cur;
        }
    }

    @Override
    public void shutdown(long interval) {
        for (MappedFile mf : this.mappedFiles) {
            mf.shutdown(interval);
        }
    }

    @Override
    public void destroy() {
        for (MappedFile mf : this.mappedFiles) {
            mf.destroy(1000 * 3);
        }
        this.mappedFiles.clear();
        this.flushPosition = 0;

        FileUtil.deleteQuietly(rootDir);
    }

    @Override
    public boolean isEmpty() {
        return this.mappedFiles.isEmpty();
    }

    @Override
    public int size() {
        return this.mappedFiles.size();
    }

    @Override
    public void setFileMode(int mode) {
        this.mappedFiles.forEach(mf -> mf.setFileMode(mode));
    }

    @Override
    public MappedFile getMappedFileForSize(int messageSize) {
        if (isEmpty()) return createMappedFile(0);

        MappedFile last = getLastMappedFile();
        if (last.hasSpace(messageSize)) {
            return last;
        }

        long nextOffset = last.getMinOffset() + this.fileSize;
        return createMappedFile(nextOffset);
    }

    @Override
    public MappedFile getMappedFileByOffset(long offset) {
        if (isEmpty()) return null;
        if (!isOffsetValid(offset)) return null;

        MappedFile targetFile = getByIndexOfOffset(offset);
        if (targetFile != null) {
            return targetFile;
        }

        for (MappedFile tmpMappedFile : this.mappedFiles) {
            if (isOffsetInFile(offset, tmpMappedFile)) {
                return tmpMappedFile;
            }
        }

        return null;
    }

    @Override
    public MappedFile getFirstMappedFile() {
        if (this.mappedFiles.isEmpty()) {
            return null;
        }

        try {
            return this.mappedFiles.getFirst();
        } catch (Exception e) {
            log.error("getFirstMappedFile has exception.", e);
        }

        return null;
    }

    @Override
    public MappedFile getLastMappedFile() {
        if (this.mappedFiles.isEmpty()) {
            return null;
        }

        try {
            return this.mappedFiles.getLast();
        } catch (Exception e) {
            log.error("getFirstMappedFile has exception.", e);
        }

        return null;
    }

    @Override
    public MappedFile getMappedFileForOffset(long offset) {
        long fileOffset = -1;
        MappedFile last = getLastMappedFile();

        // if offset greater than max offset of last mappedFile
        // this will cause service can't restart
        if (null == last || !last.containsOffset(offset)) {
            fileOffset = offset - offset % fileSize;
            return createMappedFile(fileOffset);
        }

        if (!last.isFull()) {
            return last;
        }

        fileOffset = last.getMinOffset() + this.fileSize;
        return createMappedFile(fileOffset);
    }

    @Override
    public MappedFile createMappedFile(long createOffset) {
        String file = this.rootDir + File.separator + StoreUtils.offsetToFileName(createOffset);
        String nextFile = this.rootDir + File.separator + StoreUtils.offsetToFileName(createOffset + this.fileSize);
        MappedFile mappedFile;

        if (null != allocateMappedFileService) {
            mappedFile = allocateMappedFileService.enqueue(file, nextFile, fileSize);
        } else {
            mappedFile = createMappedFile(file);
        }

        if (mappedFile == null) {
            return null;
        }

        this.mappedFiles.add(mappedFile);
        return mappedFile;
    }

    @Override
    public void removeMappedFile(MappedFile mappedFile) {
        if (!this.mappedFiles.contains(mappedFile)) {
            return;
        }

        if (this.mappedFiles.remove(mappedFile)) {
            mappedFile.destroy(1000 * 3);
        }
    }

    @Override
    public void removeMappedFiles(List<MappedFile> files) {
        for (MappedFile mappedFile : files) {
            removeMappedFile(mappedFile);
        }
    }

    @Override
    public MappedFile getMappedFileByIndex(int index) {
        if (index > size()) {
            return null;
        }

        return this.mappedFiles.get(index);
    }

    private MappedFile createMappedFile(String file) {
        try {
            return new DefaultMappedFile(file, this.fileSize);
        } catch (IOException e) {
            log.error("create mappedFile exception", e);
        }

        return null;
    }

    @Override
    public long getMinOffset() {
        MappedFile mappedFile = getFirstMappedFile();
        if (mappedFile == null) {
            return 0;
        }

        return mappedFile.getMinOffset();
    }

    @Override
    public long getMaxOffset() {
        MappedFile mappedFile = getLastMappedFile();
        if (mappedFile == null) {
            return 0;
        }

        return mappedFile.getMinOffset() + mappedFile.getInsertPosition();
    }

    @Override
    public long getUnCommittedSize() {
        return getMaxOffset() - this.commitPosition;
    }

    @Override
    public long getUnFlushedSize() {
        return getMaxOffset() - this.flushPosition;
    }

    @Override
    public boolean flush(int minPages) {
        MappedFile mappedFile = getByOffsetOrReturnFirst(flushPosition);
        if (mappedFile == null) {
            return true;
        }

        long timestamp = mappedFile.getStoreTimestamp();
        int offset = mappedFile.flush(minPages);
        long position = mappedFile.getMinOffset() + offset;

        boolean result = position == this.flushPosition;
        this.flushPosition = position;

        if (0 == minPages) {
            this.storeTimestamp = timestamp;
        }

        return result;
    }

    @Override
    public boolean commit(int minPages) {
        MappedFile mappedFile = getByOffsetOrReturnFirst(commitPosition);
        if (mappedFile == null) {
            return true;
        }

        int offset = mappedFile.commit(minPages);
        long position = mappedFile.getMinOffset() + offset;

        boolean result = position == this.commitPosition;
        this.commitPosition = position;

        return result;
    }

    protected boolean loadFiles(List<File> files) {
        // ascending order
        files.sort(Comparator.comparing(File::getName));

        for (int i = 0; i < files.size(); i++) {
            File file = files.get(i);
            if (file.isDirectory()) {
                continue;
            }

            if (file.length() == 0 && i == files.size() - 1) {
                boolean ok = file.delete();
                log.warn("{} size is 0, auto delete. is_ok: {}", file, ok);
                continue;
            }

            if (file.length() != this.fileSize) {
                log.warn("{}\t{} length not matched message store config value, please check it manually", file, file.length());
                return false;
            }

            boolean status = initMappedFile(file.getPath());
            if (!status) {
                return false;
            }
        }
        return true;
    }

    private boolean initMappedFile(String path) {
        try {
            MappedFile mappedFile = new DefaultMappedFile(path, this.fileSize);
            mappedFile.setInsertPosition(this.fileSize);
            this.mappedFiles.add(mappedFile);
            log.info("load {} OK", path);

            return true;
        } catch (IOException e) {
            log.error("load file {} error", path, e);
            return false;
        }
    }

    private MappedFile getByOffsetOrReturnFirst(long offset) {
        if (isEmpty()) return null;

        MappedFile mappedFile = getMappedFileByOffset(offset);
        if (mappedFile == null) {
            mappedFile = getFirstMappedFile();
        }

        return mappedFile;
    }

    private void checkFileOffset(MappedFile pre, MappedFile cur) {
        if (pre == null) {
            return;
        }

        if (cur.getMinOffset() - pre.getMinOffset() == this.fileSize) {
            return;
        }

        log.error("[BUG]The mappedFile queue's data is damaged, the adjacent mappedFile's offset don't match. pre file {}, cur file {}",
            pre.getFileName(), cur.getFileName());
    }

    private MappedFile getByIndexOfOffset(long offset) {
        MappedFile first = this.mappedFiles.getFirst();
        MappedFile targetFile = null;
        int index = (int) ((offset / this.fileSize) - (first.getMinOffset() / this.fileSize));
        try {
            targetFile = this.mappedFiles.get(index);
        } catch (Exception ignored) {
        }

        if (isOffsetInFile(offset, targetFile)) {
            return targetFile;
        }

        return null;
    }

    private boolean isOffsetInFile(long offset, MappedFile file) {
        if (file == null) {
            return false;
        }

        long offsetInFileName = file.getMinOffset();
        if (offset < offsetInFileName) {
            return false;
        }

        return offset <= offsetInFileName + this.fileSize;
    }

    private boolean isOffsetValid(long offset) {
        MappedFile first = this.mappedFiles.getFirst();
        MappedFile last = this.mappedFiles.getLast();

        if (offset < first.getMinOffset()
            || offset > last.getMinOffset() + this.fileSize) {

            log.error("Offset not matched. Request offset: {}, firstOffset: {}, lastOffset: {}, mappedFileSize: {}, mappedFiles count: {}",
                offset,
                first.getMinOffset(),
                last.getMinOffset() + this.fileSize,
                this.fileSize,
                this.mappedFiles.size()
            );
            return false;
        }

        return true;
    }

}
