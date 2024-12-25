package com.wolf.minimq.store.infra.file;

import com.wolf.common.util.io.DirUtil;
import com.wolf.minimq.domain.service.store.infra.MappedFile;
import com.wolf.minimq.domain.service.store.infra.MappedFileQueue;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
    private final String dir;
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

    public DefaultMappedFileQueue(String dir, int fileSize, AllocateMappedFileService allocateMappedFileService) {
        this.dir = dir;
        this.fileSize = fileSize;
        this.allocateMappedFileService = allocateMappedFileService;
    }

    @Override
    public boolean load() {
        String[] paths = dir.split(DirUtil.MULTI_PATH_SEPARATOR);
        Set<String> pathSet = new HashSet<>(Arrays.asList(paths));

        List<File> files = new ArrayList<>();
        for (String path : pathSet) {
            File dir = new File(path);
            File[] ls = dir.listFiles();
            if (ls != null) {
                Collections.addAll(files, ls);
            }
        }

        return loadFiles(files);
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

    }

    @Override
    public void destroy() {

    }

    @Override
    public boolean isEmpty() {
        return false;
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
        return null;
    }

    @Override
    public MappedFile getLastMappedFile() {
        return null;
    }

    @Override
    public MappedFile createMappedFileForOffset(long offset) {
        return null;
    }

    @Override
    public boolean resetOffset(long offset) {
        return false;
    }

    @Override
    public long getMinOffset() {
        return 0;
    }

    @Override
    public long getMaxOffset() {
        return 0;
    }

    @Override
    public long getUnCommittedSize() {
        return 0;
    }

    @Override
    public long getUnFlushedSize() {
        return 0;
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

            try {
                MappedFile mappedFile = new DefaultMappedFile(file.getPath(), fileSize);

                mappedFile.setWritePosition(this.fileSize);
                mappedFile.setFlushPosition(this.fileSize);
                mappedFile.setCommitPosition(this.fileSize);
                this.mappedFiles.add(mappedFile);
                log.info("load {} OK", file.getPath());
            } catch (IOException e) {
                log.error("load file {} error", file, e);
                return false;
            }
        }
        return true;
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
