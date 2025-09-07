package cn.coderule.minimq.store.domain.consumequeue.service;

import cn.coderule.minimq.domain.config.store.ConsumeQueueConfig;
import cn.coderule.minimq.domain.domain.cluster.store.QueueUnit;
import cn.coderule.minimq.domain.domain.cluster.store.domain.consumequeue.ConsumeQueue;
import cn.coderule.minimq.domain.domain.cluster.store.infra.MappedFile;
import cn.coderule.minimq.domain.domain.cluster.store.infra.MappedFileQueue;
import cn.coderule.minimq.domain.domain.cluster.store.server.CheckPoint;
import cn.coderule.minimq.domain.domain.cluster.store.server.Offset;
import cn.coderule.minimq.store.domain.consumequeue.queue.ConsumeQueueRegistry;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConsumeQueueRecovery implements ConsumeQueueRegistry {
    private final ConsumeQueueConfig config;
    private final CheckPoint checkPoint;

    private final Set<ConsumeQueue> queueSet = new LinkedHashSet<>(128);

    public ConsumeQueueRecovery(ConsumeQueueConfig config, CheckPoint checkPoint) {
        this.config = config;
        this.checkPoint = checkPoint;
    }
    @Override
    public void register(ConsumeQueue queue) {
        queueSet.add(queue);
    }

    public void recover() {
        if (queueSet.isEmpty()) return;

        queueSet.forEach(queue -> {
            if (queue.getMappedFileQueue().isEmpty()) {
                log.error("[bug] consume queue is empty");
                return;
            }

            recoverMinOffset(queue);
            recoverMaxOffset(queue);
        });
    }

    private void recoverMinOffset(ConsumeQueue queue) {

    }

    private void recoverMaxOffset(ConsumeQueue queue) {
        Offset offset = checkPoint.getMaxOffset();
        Long maxOffset = null != offset
            ? offset.getQueueOffset(queue.getTopic(), queue.getQueueId())
            : null;

        if (null != maxOffset && checkPoint.isShutdownSuccessful()) {
            recoverToMaxOffset(queue, maxOffset);
            return;
        }

        if (null != maxOffset) {
            recoverFromOffset(queue, maxOffset);
            return;
        }

        recoverLastThreeFiles(queue);
    }

    private void recoverToMaxOffset(ConsumeQueue queue, long maxOffset) {
        MappedFile mappedFile = queue.getMappedFileQueue().getMappedFileByOffset(maxOffset);
        if (mappedFile == null) {
            log.error("[bug] can't find MappedFile for offset: {}", maxOffset);
            return;
        }

        QueueUnit unit = queue.get(maxOffset);
        if (unit == null) {
            log.error("[bug] can't find QueueUnit for offset: {}", maxOffset);
            return;
        }

        mappedFile.setInsertOffset(maxOffset);
        queue.setMaxCommitLogOffset(unit.getCommitLogOffset());
    }

    private void recoverFromOffset(ConsumeQueue queue, long startOffset) {
        MappedFileQueue mappedFileQueue = queue.getMappedFileQueue();
        List<MappedFile> mappedFiles = mappedFileQueue.getMappedFiles();

        QueueUnit lastUnit = null;
        QueueUnit tmpUnit;
        boolean findMaxOffset = false;
        MappedFile lastValidFile = null;
        List<MappedFile> dirtyFiles = new ArrayList<>();

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
            tmpUnit = findLastQueueUnit(queue, mappedFile, startOffset);
            if (null != tmpUnit) {
                lastUnit = tmpUnit;
                lastValidFile = mappedFile;
                continue;
            }

            findMaxOffset = true;
            dirtyFiles.add(mappedFile);
        }

        if (null != lastValidFile && null != lastUnit) {
            lastValidFile.setInsertOffset(lastUnit.getQueueOffset() + queue.getUnitSize());
            queue.setMaxCommitLogOffset(lastUnit.getCommitLogOffset());
        }

        mappedFileQueue.removeMappedFiles(dirtyFiles);
    }

    private void recoverLastThreeFiles(ConsumeQueue queue) {
        MappedFileQueue mappedFileQueue = queue.getMappedFileQueue();
        MappedFile mappedFile;
        int size = mappedFileQueue.size();

        if (size > 3) {
            mappedFile = mappedFileQueue.getMappedFileByIndex(size - 3);
        } else if (size > 0) {
            mappedFile = mappedFileQueue.getMappedFileByIndex(0);
        } else {
            return;
        }

        recoverFromOffset(queue, mappedFile.getMinOffset());
    }

    private QueueUnit findLastQueueUnit(ConsumeQueue queue, MappedFile mappedFile, Long startOffset) {
        QueueUnit tmp;
        QueueUnit last = null;
        long processOffset = mappedFile.containsOffset(startOffset)
            ? startOffset
            : mappedFile.getMinOffset();

        while (processOffset < mappedFile.getMaxOffset()) {
            tmp = queue.get(processOffset);
            //dirty file, this should never happen
            if (null == tmp) {
                log.error("invalid commitLog offset: {}", processOffset);
                break;
            }

            if (!tmp.isValid()) {
                break;
            }

            last = tmp;
            processOffset += tmp.getUnitSize();
        }

        return last;
    }
}
