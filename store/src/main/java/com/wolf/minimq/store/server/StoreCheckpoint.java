/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wolf.minimq.store.server;

import com.wolf.common.util.io.BufferUtil;
import com.wolf.common.util.time.DateUtil;
import com.wolf.minimq.store.infra.file.DefaultMappedFile;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StoreCheckpoint {
    private final FileChannel fileChannel;
    private final MappedByteBuffer mappedByteBuffer;

    @Getter @Setter
    private boolean shutdownSuccessful = true;

    @Getter @Setter
    private volatile long commitLogStoreTime = 0;
    @Getter @Setter
    private volatile long consumeQueueStoreTime = 0;
    @Getter @Setter
    private volatile long indexStoreTime = 0;

    @Getter @Setter
    private volatile long masterOffset = 0;
    /**
     * confirmed commitLog offset
     */
    @Getter @Setter
    private volatile long confirmOffset = 0;

    public StoreCheckpoint(final String scpPath) {
        File file = new File(scpPath);
        boolean fileExists = file.exists();

        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
            this.fileChannel = randomAccessFile.getChannel();
            this.mappedByteBuffer = fileChannel.map(MapMode.READ_WRITE, 0, DefaultMappedFile.OS_PAGE_SIZE);
        } catch (IOException e) {
            throw new com.wolf.common.lang.exception.io.IOException(e.getMessage());
        }

        if (fileExists) {
            log.info("store checkpoint file exists, {}", scpPath);
            this.commitLogStoreTime = this.mappedByteBuffer.getLong(0);
            this.consumeQueueStoreTime = this.mappedByteBuffer.getLong(8);
            this.indexStoreTime = this.mappedByteBuffer.getLong(16);
            this.masterOffset = this.mappedByteBuffer.getLong(24);
            this.confirmOffset = this.mappedByteBuffer.getLong(32);

            log.info("store checkpoint file commitLogStoreTime {}, {}", this.commitLogStoreTime, DateUtil.asLocalDateTime(this.commitLogStoreTime));
            log.info("store checkpoint file consumeQueueStoreTime {}, {}", this.consumeQueueStoreTime, DateUtil.asLocalDateTime(this.consumeQueueStoreTime));
            log.info("store checkpoint file indexStoreTime {}, {}", this.indexStoreTime, DateUtil.asLocalDateTime(this.indexStoreTime));
            log.info("store checkpoint file masterFlushedOffset {}", this.masterOffset);
            log.info("store checkpoint file confirmOffset {}", this.confirmOffset);
        } else {
            log.info("store checkpoint file not exists, {}", scpPath);
        }
    }

    public void shutdown() {
        this.flush();

        // unmap mappedByteBuffer
        BufferUtil.cleanBuffer(this.mappedByteBuffer);

        try {
            this.fileChannel.close();
        } catch (IOException e) {
            log.error("Failed to properly close the channel", e);
        }
    }

    public void flush() {
        this.mappedByteBuffer.putLong(0, this.commitLogStoreTime);
        this.mappedByteBuffer.putLong(8, this.consumeQueueStoreTime);
        this.mappedByteBuffer.putLong(16, this.indexStoreTime);
        this.mappedByteBuffer.putLong(24, this.masterOffset);
        this.mappedByteBuffer.putLong(32, this.confirmOffset);
        this.mappedByteBuffer.force();
    }

    public long getMinTimestampIndex() {
        return Math.min(this.getMinTimestamp(), this.indexStoreTime);
    }

    public long getMinTimestamp() {
        long min = Math.min(this.commitLogStoreTime, this.consumeQueueStoreTime);

        min -= 1000 * 3;
        if (min < 0) {
            min = 0;
        }

        return min;
    }

}
