package com.wolf.minimq.domain.service.store.infra;

import com.wolf.minimq.domain.vo.AppendResult;
import com.wolf.minimq.domain.vo.SelectedMappedBuffer;
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
     * Appends a raw message data represents by a byte array to the current {@code MappedFile}.
     *
     * @param data the byte array to append
     * @return true if success; false otherwise.
     */
    boolean append(byte[] data);

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
     * Flushes the data in cache to disk immediately.
     *
     * @param flushLeastPages the least pages to flush
     * @return the flushed position after the method call
     */
    int flush(int flushLeastPages);

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



}
