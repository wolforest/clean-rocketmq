package cn.coderule.minimq.store.domain.commitlog;

import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.service.store.domain.commitlog.CommitLog;
import cn.coderule.minimq.domain.service.store.infra.MappedFile;
import cn.coderule.minimq.domain.service.store.infra.MappedFileQueue;
import cn.coderule.minimq.domain.service.store.server.CheckPoint;
import cn.coderule.minimq.domain.service.store.server.Offset;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommitLogRecovery {
    private final CommitLog commitLog;
    private final CheckPoint checkPoint;

    public CommitLogRecovery(CommitLog commitLog, CheckPoint checkPoint) {
        this.commitLog = commitLog;
        this.checkPoint = checkPoint;
    }

    public void recover() {
        if (commitLog.getMappedFileQueue().isEmpty()) {
            log.error("[bug] commit log is empty");
            return;
        }

        recoverMinOffset();
        recoverMaxOffset();
    }

    private void recoverMinOffset() {
        // recover min offset
        // and init checkpoint if needed
    }

    private void recoverMaxOffset() {
        Offset offset = checkPoint.getMaxOffset();
        long maxOffset = null != offset ? offset.getCommitLogOffset() : -1;

        if (-1 != maxOffset && checkPoint.isShutdownSuccessful()) {
            recoverToMaxOffset(maxOffset);
            return;
        }

        if (-1 != maxOffset) {
            recoverFromOffset(maxOffset);
            return;
        }

        recoverLastThreeFiles();
    }

    private void recoverToMaxOffset(long maxOffset) {
        MappedFile mappedFile = commitLog.getMappedFileQueue().getMappedFileByOffset(maxOffset);
        if (mappedFile == null) {
            log.error("[bug] can't find MappedFile for offset: {}", maxOffset);
            return;
        }

        mappedFile.setInsertOffset(maxOffset);
    }

    private void recoverFromOffset(long startOffset) {
        MappedFileQueue mappedFileQueue = commitLog.getMappedFileQueue();
        List<MappedFile> mappedFiles = mappedFileQueue.getMappedFiles();

        Long maxOffset = null;
        boolean findMaxOffset = false;
        MappedFile lastValidFile = null;
        List<MappedFile> dirtyFiles = new ArrayList<>();

        /*
         * find max offset and remove all files after max offset.
         *  the last valid mappedFile maybe
         *  - the last in queue
         *  - not the last, then all files after it are dirty files
         */
        for (MappedFile mappedFile : mappedFiles) {
            // skip the file before startOffset
            if (mappedFile.getMaxOffset() < startOffset) {
                continue;
            }

            // remove all files after max offset
            if (findMaxOffset) {
                dirtyFiles.add(mappedFile);
                continue;
            }

            // scan the MappedFile from startOffset or the start of the file
            Long maxOffsetInFile = findMaxOffsetInFile(mappedFile, startOffset);
            if (null != maxOffsetInFile) {
                maxOffset = maxOffsetInFile;
                lastValidFile = mappedFile;
                continue;
            }

            // remove the invalid mappedFile next to the last valid mappedFile
            findMaxOffset = true;
            dirtyFiles.add(mappedFile);
        }

        if (null != lastValidFile && null != maxOffset) {
            lastValidFile.setInsertOffset(maxOffset);
        }
        mappedFileQueue.removeMappedFiles(dirtyFiles);
    }

    private void recoverLastThreeFiles() {
        MappedFileQueue mappedFileQueue = commitLog.getMappedFileQueue();
        MappedFile mappedFile;
        int size = mappedFileQueue.size();

        if (size > 3) {
            mappedFile = mappedFileQueue.getMappedFileByIndex(size - 3);
        } else if (size > 0) {
            mappedFile = mappedFileQueue.getMappedFileByIndex(0);
        } else {
            return;
        }

        recoverFromOffset(mappedFile.getMinOffset());
    }

    private Long findMaxOffsetInFile(MappedFile mappedFile, Long startOffset) {
        MessageBO messageBO;
        Long processOffset = mappedFile.containsOffset(startOffset)
            ? startOffset
            : mappedFile.getMinOffset();

        while (processOffset < mappedFile.getMaxOffset()) {
            messageBO = commitLog.select(processOffset);
            //dirty file, this should never happen
            if (null == messageBO) {
                processOffset = null;
                log.error("invalid commitLog offset: {}", processOffset);
                break;
            }

            if (!messageBO.isValid()) {
                break;
            }

            processOffset += messageBO.getMessageSize();
        }

        return processOffset;
    }

}
