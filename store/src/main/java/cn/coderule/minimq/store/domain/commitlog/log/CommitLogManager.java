package cn.coderule.minimq.store.domain.commitlog.log;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.common.util.lang.collection.CollectionUtil;
import cn.coderule.minimq.domain.config.store.CommitConfig;
import cn.coderule.minimq.domain.core.exception.EnqueueException;
import cn.coderule.minimq.domain.core.exception.InvalidConfigException;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.domain.store.domain.commitlog.CommitLog;
import cn.coderule.minimq.domain.domain.store.domain.mq.EnqueueFuture;
import cn.coderule.minimq.domain.domain.store.infra.InsertResult;
import cn.coderule.minimq.domain.domain.store.infra.SelectedMappedBuffer;
import cn.coderule.minimq.store.domain.commitlog.sharding.TopicPartitioner;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.extern.slf4j.Slf4j;

/**
 * @renamed from CommitLogFacade to CommitLogManager
 */
@Slf4j
public class CommitLogManager implements Lifecycle {
    private final CommitConfig config;
    private final TopicPartitioner partitioner;

    private final CommitLog[] commitLogArray;
    private final ThreadLocal<CommitLog> localCommitLog;

    private List<CommitLog> commitLogList = null;

    public CommitLogManager(CommitConfig config, TopicPartitioner partitioner) {
        this.config = config;
        this.partitioner = partitioner;

        commitLogArray = new CommitLog[config.getShardingNumber()];
        localCommitLog = ThreadLocal.withInitial(this::bindShardingWithCpu);
    }

    public synchronized CommitLog bindShardingWithCpu() {
        if (!config.isBindShardingWithCpu()) return null;

        initCommitLogList();

        if (commitLogList.isEmpty()) {
            return selectByRandom();
        }

        CommitLog commitLog = commitLogList.remove(0);
        if (commitLog != null) {
            return commitLog;
        }

        return selectByRandom();
    }

    public void addCommitLog(CommitLog commitLog) {
        commitLogArray[commitLog.getShardId()] = commitLog;
    }

    public void addCommitLog(List<CommitLog> logList) {
        if (CollectionUtil.isEmpty(logList)) return;

        for (CommitLog commitLog : logList) {
            addCommitLog(commitLog);
        }
    }

    public EnqueueFuture insert(MessageBO messageBO) {
        try {
            return selectByMessage(messageBO)
                .insert(messageBO);
        } catch (EnqueueException e) {
            log.error("[CommitLogManager]insert message failed: {}",  e.getMessage());
            return EnqueueFuture.failure(e.getStatus());
        }
    }

    public MessageBO select(long offset, int size) {
        try {
            int shardId = offsetToShardId(offset);
            CommitLog commitLog = selectByShardId(shardId);
            return commitLog.select(offset, size);
        } catch (EnqueueException e) {
            log.error("[CommitLogManager]select message failed: {}",  e.getMessage());
            return MessageBO.notFound();
        }
    }

    public MessageBO select(long offset) {
        try {
            int shardId = offsetToShardId(offset);
            CommitLog commitLog = selectByShardId(shardId);
            return commitLog.select(offset);
        } catch (EnqueueException e) {
            return MessageBO.notFound();
        }
    }

    public InsertResult insert(long offset, byte[] data, int start, int size) {
        int shardId = offsetToShardId(offset);
        CommitLog commitLog = selectByShardId(shardId);
        return commitLog.insert(offset, data, start, size);
    }

    public SelectedMappedBuffer selectBuffer(long offset) {
        int shardId = offsetToShardId(offset);
        CommitLog commitLog = selectByShardId(shardId);
        return commitLog.selectBuffer(offset);
    }

    public SelectedMappedBuffer selectBuffer(long offset, int size) {
        int shardId = offsetToShardId(offset);
        CommitLog commitLog = selectByShardId(shardId);
        return commitLog.selectBuffer(offset, size);
    }

    public CommitLog selectByShardId(int shardId) {
        CommitLog commitLog = commitLogArray[shardId];
        if (commitLog == null) {
            log.error("[CommitLogManager]shardId of commitLog not found: {}", shardId);
            throw new IllegalArgumentException("shardId of commitLog not found: " + shardId);
        }

        return commitLog;
    }

    public long getMinOffset(int shardId) {
        return selectByShardId(shardId).getMinOffset();
    }

    public long getMaxOffset(int shardId) {
        return selectByShardId(shardId).getMaxOffset();
    }

    public long getFlushedOffset(int shardId) {
        return selectByShardId(shardId).getFlushedOffset();
    }

    public long getUnFlushedSize(int shardId) {
        return selectByShardId(shardId).getUnFlushedSize();
    }

    public int offsetToShardId(long offset) {
        if (offset < 0) {
            throw new IllegalArgumentException("offset must be positive");
        }

        return (int) (offset % config.getMaxShardingNumber());
    }

    @Override
    public void start() throws Exception {
        for (CommitLog commitLog : commitLogArray) {
            if (commitLog == null) {
                continue;
            }

            commitLog.start();
        }
    }

    @Override
    public void shutdown() throws Exception {
        for (CommitLog commitLog : commitLogArray) {
            if (commitLog == null) {
                continue;
            }

            commitLog.shutdown();
        }
    }

    @Override
    public void initialize() throws Exception {
        for (CommitLog commitLog : commitLogArray) {
            if (commitLog == null) {
                continue;
            }

            commitLog.initialize();
        }
    }

    private CommitLog selectByMessage(MessageBO messageBO) {
        if (1 == commitLogArray.length) {
            return commitLogArray[0];
        }

        if (config.isBindShardingWithCpu()) {
            return selectByThreadId();
        }

        return selectByRandom();

//        if (localCommitLog.get() != null) {
//            return localCommitLog.get();
//        }
//
//        int shardId = partitioner.partitionByTopic(messageBO.getTopic());
//        return selectByShardId(shardId);
    }

    private void initCommitLogList() {
        if (null != commitLogList) {
            return;
        }

        if (commitLogArray.length == 0) {
            throw new InvalidConfigException("invalid config: bindShardingWithCpu"
                + " empty commitLog list");
        }

        commitLogList = new ArrayList<>(List.of(commitLogArray));
    }

    private CommitLog selectByThreadId() {
        int index = calculateThreadId();
        if (index < 0) {
            return selectByRandom();
        }

        index = index % commitLogArray.length;
        return commitLogArray[index];
    }

    private int calculateThreadId() {
        String threadName = Thread.currentThread().getName();
        int length = threadName.length();
        char one = threadName.charAt(length - 1);

        if (one < '0' || one > '9') {
            return -1;
        }

        int index = one - '0';
        char ten = threadName.charAt(length - 2);
        if (ten >= '0' && ten <= '9') {
            index = (ten - '0') * 10 + index;
        }

        return index;
    }

    private CommitLog selectByRandom() {
        int index = ThreadLocalRandom.current().nextInt(commitLogArray.length);
        return commitLogArray[index];
    }

}
