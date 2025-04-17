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

package cn.coderule.minimq.rpc.common.rpc.core.enums;

import cn.coderule.common.util.lang.StringUtil;
import lombok.Getter;

@Getter
public enum RequestType {

    ONEWAY("oneway"),
    SYNC("sync"),
    ASYNC("async");

    private final String name;

    RequestType(String name) {
        this.name = name;
    }

    public static RequestType of(String name) {
        if (StringUtil.isBlank(name)) {
            return SYNC;
        }

        for (RequestType type : values()) {
            if (type.name.equals(name)) {
                return type;
            }
        }

        return SYNC;
    }
}
