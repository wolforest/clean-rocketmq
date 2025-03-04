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

package cn.coderule.minimq.rpc.common.core;

import io.netty.channel.Channel;
import lombok.Data;

@Data
public class RequestTask implements Runnable {
    private final Runnable runnable;
    private final long createTimestamp = System.currentTimeMillis();
    private final Channel channel;
    private final RpcCommand request;
    private volatile boolean stopRun = false;

    public RequestTask(final Runnable runnable, final Channel channel, final RpcCommand request) {
        this.runnable = runnable;
        this.channel = channel;
        this.request = request;
    }

    @Override
    public void run() {
        if (!this.stopRun)
            this.runnable.run();
    }

}
