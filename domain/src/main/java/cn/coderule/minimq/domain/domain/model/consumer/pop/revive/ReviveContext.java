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
package cn.coderule.minimq.domain.domain.model.consumer.pop.revive;

import cn.coderule.minimq.domain.domain.model.consumer.pop.checkpoint.PopCheckPoint;
import java.io.Serializable;
import java.util.HashMap;
import lombok.Data;

@Data
public class ReviveContext implements Serializable {
    private final ReviveObj reviveObj;
    private final HashMap<String, PopCheckPoint> mockPointMap;
    private final long startTime;

    private long endTime;
    private long firstRt;
    private int noMsgCount;

    public ReviveContext() {
        this.reviveObj = new ReviveObj();
        this.mockPointMap = new HashMap<>();

        this.startTime = System.currentTimeMillis();
        this.endTime = 0;
        this.firstRt = 0;
        this.noMsgCount = 0;
    }

    public void increaseNoMsgCount() {
        this.noMsgCount++;
    }

    public HashMap<String, PopCheckPoint> getMap() {
        return reviveObj.getMap();
    }

}
