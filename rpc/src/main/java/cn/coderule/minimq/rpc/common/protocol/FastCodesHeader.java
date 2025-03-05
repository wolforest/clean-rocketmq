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

package cn.coderule.minimq.rpc.common.protocol;

import cn.coderule.minimq.rpc.common.exception.RemotingCommandException;
import cn.coderule.minimq.rpc.common.netty.codec.RocketMQSerializable;
import io.netty.buffer.ByteBuf;
import java.util.HashMap;

public interface FastCodesHeader {

    default String getAndCheckNotNull(HashMap<String, String> fields, String field) {
        String value = fields.get(field);
        if (value == null) {
            String headerClass = this.getClass().getSimpleName();
            // no exception throws, keep compatible with RemotingCommand.decodeCommandCustomHeader
        }
        return value;
    }

    default void writeIfNotNull(ByteBuf out, String key, Object value) {
        if (value != null) {
            RocketMQSerializable.writeStr(out, true, key);
            RocketMQSerializable.writeStr(out, false, value.toString());
        }
    }

    void encode(ByteBuf out);

    void decode(HashMap<String, String> fields) throws RemotingCommandException;


}
