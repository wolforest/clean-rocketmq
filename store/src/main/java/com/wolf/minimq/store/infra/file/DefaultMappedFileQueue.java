package com.wolf.minimq.store.infra.file;

import com.wolf.common.util.io.DirUtil;
import com.wolf.common.util.io.FileUtil;
import com.wolf.minimq.domain.service.store.infra.MappedFile;
import com.wolf.minimq.domain.service.store.infra.MappedFileQueue;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultMappedFileQueue implements MappedFileQueue {
    private final String rootDir;
    private final int fileSize;

    private final AllocateMappedFileService allocateMappedFileService;

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
    public MappedFile getAvailableMappedFile(int messageSize) {
        return null;
    }

    @Override
    public MappedFile getMappedFileByOffset(long offset) {
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
    public MappedFile createMappedFileForOffset(long offset) {
        return null;
    }

    @Override
    public long getMinOffset() {
        MappedFile mappedFile = getFirstMappedFile();
        if (mappedFile == null) {
            return 0;
        }

        return mappedFile.getOffsetInFileName();
    }

    @Override
    public long getMaxOffset() {
        MappedFile mappedFile = getLastMappedFile();
        if (mappedFile == null) {
            return 0;
        }

        return mappedFile.getOffsetInFileName() + mappedFile.getWriteOrCommitPosition();
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
        return false;
    }

    @Override
    public boolean commit(int minPages) {
        return false;
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

            mappedFile.setWritePosition(this.fileSize);
            mappedFile.setFlushPosition(this.fileSize);
            mappedFile.setCommitPosition(this.fileSize);
            this.mappedFiles.add(mappedFile);
            log.info("load {} OK", path);

            return true;
        } catch (IOException e) {
            log.error("load file {} error", path, e);
            return false;
        }
    }

    private void checkFileOffset(MappedFile pre, MappedFile cur) {
        if (pre == null) {
            return;
        }

        if (cur.getOffsetInFileName() - pre.getOffsetInFileName() == this.fileSize) {
            return;
        }

        log.error("[BUG]The mappedFile queue's data is damaged, the adjacent mappedFile's offset don't match. pre file {}, cur file {}",
            pre.getFileName(), cur.getFileName());
    }
}
