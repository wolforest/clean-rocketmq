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
package cn.coderule.minimq.store.server.bootstrap;

import cn.coderule.common.util.io.FileUtil;
import cn.coderule.common.util.lang.StringUtil;
import cn.coderule.common.util.lang.string.JSONUtil;
import cn.coderule.minimq.domain.service.store.server.CheckPoint;
import cn.coderule.minimq.domain.service.store.server.Offset;
import java.io.File;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StoreCheckpoint implements CheckPoint {
    private static final String MIN_OFFSET_FILE = "minOffset.json";
    private static final String MAX_OFFSET_FILE = "maxOffset.json";
    private static final String TRY_SUFFIX = ".try";
    private static final String COMMIT_SUFFIX = ".commit";

    private final String minOffsetPath;
    private final String maxOffsetPath;

    private Offset minOffset = new Offset();
    private Offset minCopy;
    private Offset maxOffset = new Offset();

    @Getter @Setter
    private boolean shutdownSuccessful = false;
    private boolean minOffsetLoaded = false;
    private boolean maxOffsetLoaded = false;

    public StoreCheckpoint(String storePath) {
        this.minOffsetPath = storePath + File.separator + MIN_OFFSET_FILE;
        this.maxOffsetPath = storePath + File.separator + MAX_OFFSET_FILE;
    }

    @Override
    public void load() {
        loadMinOffset();
        loadMaxOffset();
    }

    @Override
    public void save() {
        this.saveMaxOffset();
    }

    @Override
    public Offset getMinOffset() {
        if (!minOffsetLoaded) {
            return null;
        }

        if (minCopy != null) {
            return minCopy;
        }
        minCopy = minOffset.deepCopy();
        return minCopy;
    }

    @Override
    public synchronized Offset tryMinOffset() {
        String commitPath = minOffsetPath + COMMIT_SUFFIX;
        if (FileUtil.exists(commitPath)) {
            commitMinOffset();
        }

        if (minCopy != null) {
            return minCopy;
        }

        String tryPath = minOffsetPath + TRY_SUFFIX;
        if (FileUtil.exists(tryPath)) {
            FileUtil.delete(tryPath);
        }

        Offset tmp = minOffset.deepCopy();
        String data = JSONUtil.toJSONString(tmp);
        FileUtil.stringToFile(data, tryPath);

        minCopy = tmp;

        return minCopy;
    }

    @Override
    public synchronized void commitMinOffset() {
        String tryPath = minOffsetPath + TRY_SUFFIX;
        String commitPath = minOffsetPath + COMMIT_SUFFIX;

        if (!FileUtil.exists(tryPath) && !FileUtil.exists(commitPath)) {
            return;
        }

        if (FileUtil.exists(tryPath)) {
            FileUtil.rename(tryPath, commitPath);
        }

        if (null != this.minCopy) {
            this.minOffset = this.minCopy;
            this.minCopy = null;
        }

        String data = JSONUtil.toJSONString(this.minOffset);
        FileUtil.stringToFile(data, commitPath);

        FileUtil.rename(commitPath, minOffsetPath);
    }

    @Override
    public synchronized void cancelMinOffset() {
        String tryPath = minOffsetPath + TRY_SUFFIX;
        String commitPath = minOffsetPath + COMMIT_SUFFIX;

        this.minCopy = null;

        if (FileUtil.exists(tryPath)) {
            FileUtil.delete(tryPath);
        }

        if (FileUtil.exists(commitPath)) {
            FileUtil.delete(commitPath);
        }
    }

    @Override
    public Offset getMaxOffset() {
        if (!maxOffsetLoaded) {
            return null;
        }

        return maxOffset;
    }

    @Override
    public void saveMaxOffset() {
        String data = JSONUtil.toJSONString(maxOffset);
        FileUtil.stringToFile(data, maxOffsetPath);
    }

    private void loadMinOffset() {
        if (!FileUtil.exists(minOffsetPath)) {
            minOffsetLoaded = false;
            return;
        }

        String data = FileUtil.readString(minOffsetPath);
        if (StringUtil.isBlank(data)) {
            return;
        }

        minOffset = JSONUtil.parse(data, Offset.class);
        minOffsetLoaded = true;
    }

    private void loadMaxOffset() {
        if (!FileUtil.exists(maxOffsetPath)) {
            maxOffsetLoaded = false;
            return;
        }

        String data = FileUtil.readString(maxOffsetPath);
        if (StringUtil.isBlank(data)) {
            return;
        }

        maxOffset = JSONUtil.parse(data, Offset.class);
        maxOffsetLoaded = true;
    }

}
