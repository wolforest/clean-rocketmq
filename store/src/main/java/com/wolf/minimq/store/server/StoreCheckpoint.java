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
    private boolean normalExit = true;

    @Getter @Setter
    private volatile long physicMsgTimestamp = 0;
    @Getter @Setter
    private volatile long logicsMsgTimestamp = 0;
    @Getter @Setter
    private volatile long indexMsgTimestamp = 0;

    @Getter @Setter
    private volatile long masterOffset = 0;
    /**
     * confirmed commitLog offset
     */
    @Getter @Setter
    private volatile long commitLogOffset = 0;

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
            this.physicMsgTimestamp = this.mappedByteBuffer.getLong(0);
            this.logicsMsgTimestamp = this.mappedByteBuffer.getLong(8);
            this.indexMsgTimestamp = this.mappedByteBuffer.getLong(16);
            this.masterOffset = this.mappedByteBuffer.getLong(24);
            this.commitLogOffset = this.mappedByteBuffer.getLong(32);

            log.info("store checkpoint file physicMsgTimestamp {}, {}", this.physicMsgTimestamp, DateUtil.asLocalDateTime(this.physicMsgTimestamp));
            log.info("store checkpoint file logicsMsgTimestamp {}, {}", this.logicsMsgTimestamp, DateUtil.asLocalDateTime(this.logicsMsgTimestamp));
            log.info("store checkpoint file indexMsgTimestamp {}, {}", this.indexMsgTimestamp, DateUtil.asLocalDateTime(this.indexMsgTimestamp));
            log.info("store checkpoint file masterFlushedOffset {}", this.masterOffset);
            log.info("store checkpoint file confirmPhyOffset {}", this.commitLogOffset);
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
        this.mappedByteBuffer.putLong(0, this.physicMsgTimestamp);
        this.mappedByteBuffer.putLong(8, this.logicsMsgTimestamp);
        this.mappedByteBuffer.putLong(16, this.indexMsgTimestamp);
        this.mappedByteBuffer.putLong(24, this.masterOffset);
        this.mappedByteBuffer.putLong(32, this.commitLogOffset);
        this.mappedByteBuffer.force();
    }

    public long getMinTimestampIndex() {
        return Math.min(this.getMinTimestamp(), this.indexMsgTimestamp);
    }

    public long getMinTimestamp() {
        long min = Math.min(this.physicMsgTimestamp, this.logicsMsgTimestamp);

        min -= 1000 * 3;
        if (min < 0) {
            min = 0;
        }

        return min;
    }

}
