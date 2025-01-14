package com.wolf.minimq.domain.service.store.infra;

import com.wolf.minimq.domain.enums.FlushType;
import com.wolf.minimq.domain.model.dto.InsertResult;
import com.wolf.minimq.domain.model.dto.SelectedMappedBuffer;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;

public interface MappedFile {
    /**
     * Returns the file name of the {@code MappedFile}.
     * The offset may store in the file name.
     *
     * @return the file name
     */
    String getFileName();

    /**
     * Returns the file size of the {@code MappedFile}.
     *
     * @return the file size
     */
    int getFileSize();

    /**
     * Returns the global offset of the current {code MappedFile}, it's a long value of the file name.
     *
     * @return the min offset of this file
     */
    long getMinOffset();
    long getMaxOffset();

    /**
     * Returns true if this {@code MappedFile} is full and no new messages can be added.
     *
     * @return true if the file is full
     */
    boolean isFull();

    /**
     *
     * @param size size needed
     * @return true if file has enough space
     */
    boolean hasSpace(int size);

    boolean containsOffset(long offset);

    /**
     * Returns true if this {@code MappedFile} is available.
     * <p>
     * The mapped file will be not available if it's shutdown or destroyed.
     *
     * @return true if the file is available
     */
    boolean isAvailable();

    void setFileMode(int mode);

    /**
     * Appends a raw message data represents by a byte array to the current {@code MappedFile}.
     *
     * @param data the byte array to append
     * @return AppendResult
     */
    InsertResult insert(byte[] data);

    /**
     * Appends a raw message data represents by a byte array to the current {@code MappedFile}.
     *
     * @param data the byte buffer to append
     * @return AppendResult
     */
    InsertResult insert(ByteBuffer data);

    /**
     * Appends a raw message data represents by a byte array to the current {@code MappedFile},
     * starting at the given offset in the array.
     *
     * @param data the byte array to append
     * @param offset the offset within the array of the first byte to be read
     * @param length the number of bytes to be read from the given array
     * @return AppendResult
     */
    InsertResult insert(byte[] data, int offset, int length);

    /**
     * Selects a slice of the mapped byte buffer's subregion behind the mapped file,
     * starting at the given position.
     *
     * @param pos the given position
     * @param size the size of the returned subregion
     * @return a {@code SelectResult} instance contains the selected slice
     */
    SelectedMappedBuffer select(int pos, int size);

    /**
     * Selects a slice of the mapped byte buffer's subregion behind the mapped file,
     * starting at the given position.
     *
     * @param pos the given position
     * @return a {@code SelectResult} instance contains the selected slice
     */
    SelectedMappedBuffer select(int pos);

    /**
     * Get data from FileChannel with pos, size to input ByteBuffer
     * for timer message, which may read data not in page cache
     *
     * @param pos a certain pos offset to get data
     * @param size the size of data
     * @param byteBuffer the data
     * @return true if with data; false if no data;
     */
    boolean selectFromChannel(int pos, int size, ByteBuffer byteBuffer);

    /**
     * Flushes the data in cache to disk immediately.
     * if minPages is 0:
     *  - flush all data in cache
     *  - and store the timestamp
     * if minPages greater than 0:
     *  - if the cache size is greater than minPages * pageSize
     *      - flush data in cache
     *  - else do nothing
     *
     * @param minPages the min pages to flush
     * @return the flushed position after the method call
     */
    int flush(int minPages);

    /**
     * Flushes the data in the secondary cache to page cache or disk immediately.
     *
     * @param minPages the least pages to commit
     * @return the committed position after the method call
     */
    int commit(int minPages);

    /**
     * Returns the mapped byte buffer behind the mapped file.
     *
     * @return the mapped byte buffer
     */
    MappedByteBuffer getMappedByteBuffer();

    /**
     * Returns a slice of the mapped byte buffer behind the mapped file.
     *
     * @return the slice of the mapped byte buffer
     */
    ByteBuffer sliceByteBuffer();

    void warmup(int size);

    void warmup(FlushType flushType, int size);

    /**
     * Destroys the file and delete it from the file system.
     *
     * @param interval If {@code true} then this method will destroy the file forcibly and ignore the reference
     */
    void destroy(long interval);

    /**
     * Shutdowns the file and mark it unavailable.
     *
     * @param interval If {@code true} then this method will shut down the file forcibly and ignore the reference
     */
    void shutdown(long interval);

    /**
     * Decreases the reference count by {@code 1} and clean up the mapped file if the reference count reaches at
     * {@code 0}.
     */
    void release();

    /**
     * Increases the reference count by {@code 1}.
     *
     * @return true if success; false otherwise.
     */
    boolean hold();

    /**
     * get write or commit position
     *  - return commitPosition if using transient store pool
     * @return writePosition | commitPosition
     */
    int getInsertPosition();
    void setInsertPosition(int insertPosition);
    void setInsertOffset(long insertOffset);

    int getWritePosition();
    int getFlushPosition();
    int getCommitPosition();
    long getStoreTimestamp();
}
