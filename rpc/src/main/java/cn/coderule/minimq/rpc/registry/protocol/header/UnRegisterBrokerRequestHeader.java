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
 * $Id: UnRegisterBrokerRequestHeader.java 1835 2013-05-16 02:00:50Z vintagewang@apache.org $
 */
package cn.coderule.minimq.rpc.registry.protocol.header;

import cn.coderule.minimq.rpc.rpc.core.annotation.CFNotNull;
import cn.coderule.minimq.rpc.rpc.core.annotation.RocketMQAction;
import cn.coderule.minimq.rpc.rpc.core.annotation.RocketMQResource;
import cn.coderule.minimq.rpc.rpc.core.enums.Action;
import cn.coderule.minimq.rpc.rpc.core.enums.ResourceType;
import cn.coderule.minimq.rpc.rpc.core.exception.RemotingCommandException;
import cn.coderule.minimq.rpc.rpc.protocol.code.RequestCode;
import cn.coderule.minimq.rpc.rpc.protocol.header.CommandHeader;
import lombok.Data;

@Data
@RocketMQAction(value = RequestCode.UNREGISTER_BROKER, resource = ResourceType.CLUSTER, action = Action.UPDATE)
public class UnRegisterBrokerRequestHeader implements CommandHeader {
    @CFNotNull
    private String brokerName;
    @CFNotNull
    private String brokerAddr;
    @CFNotNull
    @RocketMQResource(ResourceType.CLUSTER)
    private String clusterName;
    @CFNotNull
    private Long brokerId;

    @Override
    public void checkFields() throws RemotingCommandException {
    }

}
