package com.wolf.minimq.domain.service.store.infra;

import com.wolf.minimq.domain.model.vo.AppendResult;
import com.wolf.minimq.domain.model.vo.SelectedMappedBuffer;
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
     * Returns the global offset of the current {code MappedFile}, it's a long value of the file name.
     *
     * @return the offset of this file
     */
    long getOffsetInFileName();

    /**
     * Returns the file size of the {@code MappedFile}.
     *
     * @return the file size
     */
    int getFileSize();

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
    boolean hasEnoughSpace(int size);

    /**
     * Returns true if this {@code MappedFile} is available.
     * <p>
     * The mapped file will be not available if it's shutdown or destroyed.
     *
     * @return true if the file is available
     */
    boolean isAvailable();

    /**
     * get write or commit position
     *  - return commitPosition if using transient store pool
     * @return writePosition | commitPosition
     */
    int getWritePosition();

    /**
     * Appends a raw message data represents by a byte array to the current {@code MappedFile}.
     *
     * @param data the byte array to append
     * @return AppendResult
     */
    AppendResult append(byte[] data);

    /**
     * Appends a raw message data represents by a byte array to the current {@code MappedFile}.
     *
     * @param data the byte buffer to append
     * @return AppendResult
     */
    AppendResult append(ByteBuffer data);

    /**
     * Appends a raw message data represents by a byte array to the current {@code MappedFile},
     * starting at the given offset in the array.
     *
     * @param data the byte array to append
     * @param offset the offset within the array of the first byte to be read
     * @param length the number of bytes to be read from the given array
     * @return AppendResult
     */
    AppendResult append(byte[] data, int offset, int length);

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
     * Get data from a certain pos offset with size byte
     *
     * @param pos a certain pos offset to get data
     * @param size the size of data
     * @param byteBuffer the data
     * @return true if with data; false if no data;
     */
    boolean select(int pos, int size, ByteBuffer byteBuffer);

    /**
     * Flushes the data in cache to disk immediately.
     *
     * @param flushLeastPages the least pages to flush
     * @return the flushed position after the method call
     */
    int flush(int flushLeastPages);

    /**
     * Flushes the data in the secondary cache to page cache or disk immediately.
     *
     * @param commitLeastPages the least pages to commit
     * @return the committed position after the method call
     */
    int commit(int commitLeastPages);

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


    /**
     * Destroys the file and delete it from the file system.
     *
     * @param interval If {@code true} then this method will destroy the file forcibly and ignore the reference
     * @return true if success; false otherwise.
     */
    boolean destroy(long interval);

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

}
