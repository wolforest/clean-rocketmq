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

import cn.coderule.common.util.io.DirUtil;
import java.io.File;

public class StorePath {
    private static String ROOT_PATH;

    public static void setRootPath(String rootPath) {
        ROOT_PATH = rootPath;
    }

    public static void initPath() {
        DirUtil.createIfNotExists(ROOT_PATH);
        DirUtil.createIfNotExists(getCommitLogPath());
        DirUtil.createIfNotExists(getConsumeQueuePath());
        DirUtil.createIfNotExists(getIndexPath());
    }

    public static String getCommitLogPath() {
        return ROOT_PATH + File.separator + "commitlog";
    }

    public static String getConsumeQueuePath() {
        return ROOT_PATH + File.separator + "consumequeue";
    }

    public static String getConsumeQueueExtPath() {
        return ROOT_PATH + File.separator + "consumequeue_ext";
    }
    public static String getBatchConsumeQueuePath() {
        return ROOT_PATH + File.separator + "batchconsumequeue";
    }

    public static String getIndexPath() {
        return ROOT_PATH + File.separator + "index";
    }

    public static String getCheckpointPath() {
        return ROOT_PATH + File.separator + "checkpoint";
    }

    public static String getAbortFile() {
        return ROOT_PATH + File.separator + "abort";
    }

    public static String getLockFile() {
        return ROOT_PATH + File.separator + "lock";
    }

    public static String getDelayOffsetPath() {
        return ROOT_PATH + File.separator + "config" + File.separator + "delayOffset.json";
    }

    public static String getTransactionStateTablePath() {
        return ROOT_PATH + File.separator + "transaction" + File.separator + "statetable";
    }

    public static String getTransactionRedoLogPath() {
        return ROOT_PATH + File.separator + "transaction" + File.separator + "redolog";
    }

    public static String getTopicPath() {
        return ROOT_PATH + File.separator + "config" + File.separator + "topics.json";
    }

    public static String getTopicQueueMappingPath() {
        return ROOT_PATH + File.separator + "config" + File.separator + "topicQueueMapping.json";
    }

    public static String getConsumerOffsetPath() {
        return ROOT_PATH + File.separator + "config" + File.separator + "consumerOffset.json";
    }

    public static String getLmqConsumerOffsetPath() {
        return ROOT_PATH + File.separator + "config" + File.separator + "lmqConsumerOffset.json";
    }

    public static String getConsumerOrderInfoPath() {
        return ROOT_PATH + File.separator + "config" + File.separator + "consumerOrderInfo.json";
    }

    public static String getSubscriptionGroupPath() {
        return ROOT_PATH + File.separator + "config" + File.separator + "subscriptionGroup.json";
    }
    public static String getTimerCheckPath() {
        return ROOT_PATH + File.separator + "config" + File.separator + "timercheck";
    }
    public static String getTimerMetricsPath() {
        return ROOT_PATH + File.separator + "config" + File.separator + "timermetrics";
    }
    public static String getTransactionMetricsPath() {
        return ROOT_PATH + File.separator + "config" + File.separator + "transactionMetrics";
    }

    public static String getConsumerFilterPath() {
        return ROOT_PATH + File.separator + "config" + File.separator + "consumerFilter.json";
    }

    public static String getMessageRequestModePath() {
        return ROOT_PATH + File.separator + "config" + File.separator + "messageRequestMode.json";
    }

}
