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

/**
 * $Id: GetAllTopicConfigResponseHeader.java 1835 2013-05-16 02:00:50Z vintagewang@apache.org $
 */
package cn.coderule.minimq.rpc.store.protocol.header;

import cn.coderule.minimq.rpc.rpc.core.annotation.RocketMQAction;
import cn.coderule.minimq.rpc.rpc.core.enums.Action;
import cn.coderule.minimq.rpc.rpc.core.enums.ResourceType;
import cn.coderule.minimq.rpc.rpc.core.exception.RemotingCommandException;
import cn.coderule.minimq.rpc.rpc.protocol.code.RequestCode;
import cn.coderule.minimq.rpc.rpc.protocol.header.CommandHeader;


@RocketMQAction(value = RequestCode.GET_ALL_TOPIC_CONFIG, resource = ResourceType.TOPIC, action = Action.LIST)
public class GetAllTopicConfigResponseHeader implements CommandHeader {

    private Boolean lo;

    @Override
    public void checkFields() throws RemotingCommandException {
    }
}
