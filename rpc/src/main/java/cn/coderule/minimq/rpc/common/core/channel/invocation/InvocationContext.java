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

package cn.coderule.minimq.rpc.common.core.channel.invocation;

import cn.coderule.minimq.rpc.common.rpc.core.invoke.RpcCommand;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class InvocationContext implements InvocationContextInterface {
    private final CompletableFuture<RpcCommand> response;
    private final long timestamp = System.currentTimeMillis();

    public InvocationContext(CompletableFuture<RpcCommand> resp) {
        this.response = resp;
    }

    public boolean expired(long expiredTimeSec) {
        return System.currentTimeMillis() - timestamp >= Duration.ofSeconds(expiredTimeSec).toMillis();
    }

    public CompletableFuture<RpcCommand> getResponse() {
        return response;
    }

    public void handle(RpcCommand RpcCommand) {
        response.complete(RpcCommand);
    }
}
