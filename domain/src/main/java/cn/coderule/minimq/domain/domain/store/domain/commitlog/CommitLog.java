package cn.coderule.minimq.domain.domain.store.domain.commitlog;

import cn.coderule.minimq.domain.domain.store.infra.MappedFileQueue;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.domain.store.domain.mq.EnqueueFuture;
import cn.coderule.minimq.domain.domain.store.infra.InsertResult;
import cn.coderule.minimq.domain.domain.store.infra.SelectedMappedBuffer;

public interface CommitLog {
    MappedFileQueue getMappedFileQueue();

    EnqueueFuture insert(MessageBO messageBO);

    MessageBO select(long offset, int size);

    MessageBO select(long offset);

    /**
     * get min offset, which is the start offset of the first mappedFile
     * @return minOffset
     */
    long getMinOffset();

    /**
     * get max offset, which is
     *  - the write position in default setting
     *  - or the commit position if enable write cache
     * @return maxOffset
     */
    long getMaxOffset();

    /**
     * assign commit offset
     *  - get current MappedFile
     *  - minOffset + writePosition
     * @param messageBO message
     *  - messageLength can't be null
     */
    void assignCommitOffset(MessageBO messageBO);

    /**
     * get flushed offset
     * @return flushedPosition of the mappedFileQueue
     */
    long getFlushedOffset();

    /**
     * get unFlushed size
     * @return unFlushedSize of the mappedFileQueue
     */
    long getUnFlushedSize();

    /**
     * Raw data insertion for M/S mode
     *
     * @param offset commitLog offset
     * @param data data
     * @param start data start position
     * @param size data size
     * @return insert result
     */
    InsertResult insert(long offset, byte[] data, int start, int size);

    /**
     * Raw data selection for M/S mode
     *
     * @param offset commitLog offset
     * @return selected mapped buffer
     */
    SelectedMappedBuffer selectBuffer(long offset);

    /**
     * Raw data selection for M/S mode
     *
     * @param offset commitLog offset
     * @param size selected size
     * @return selected mapped buffer
     */
    SelectedMappedBuffer selectBuffer(long offset, int size);

    void destroy();
}
