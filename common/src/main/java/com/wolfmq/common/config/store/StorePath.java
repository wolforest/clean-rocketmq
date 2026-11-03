package com.wolfmq.common.config.store;

import cn.coderule.common.util.io.DirUtil;
import java.io.File;
import lombok.Data;

@Data
public class StorePath {
    private String rootPath;

    public StorePath(String rootPath) {
        this.rootPath = rootPath;

        createDirectories();
    }

    private void createDirectories() {
        DirUtil.createIfNotExists(rootPath);
        DirUtil.createIfNotExists(getMetaPath());
        DirUtil.createIfNotExists(getIndexPath());
        DirUtil.createIfNotExists(getCommitLogPath());
        DirUtil.createIfNotExists(getCheckpointPath());
        DirUtil.createIfNotExists(getConsumeQueuePath());
    }

    public String getMetaPath() {
        return rootPath + File.separator + "config";
    }

    public String getCommitLogPath() {
        return rootPath + File.separator + "commitlog";
    }

    public String getConsumeQueuePath() {
        return rootPath + File.separator + "consumequeue";
    }

    public String getConsumeQueueExtPath() {
        return rootPath + File.separator + "consumequeue_ext";
    }

    public String getBatchConsumeQueuePath() {
        return rootPath + File.separator + "batchconsumequeue";
    }

    public String getIndexPath() {
        return rootPath + File.separator + "index";
    }

    public String getCheckpointPath() {
        return rootPath + File.separator + "checkpoint";
    }

    public String getAbortFile() {
        return rootPath + File.separator + "abort";
    }

    public String getLockFile() {
        return rootPath + File.separator + "lock";
    }

    public String getDelayOffsetPath() {
        return rootPath + File.separator + "config" + File.separator + "delayOffset.json";
    }

    public String getTransactionStateTablePath() {
        return rootPath + File.separator + "transaction" + File.separator + "statetable";
    }

    public String getTransactionRedoLogPath() {
        return rootPath + File.separator + "transaction" + File.separator + "redolog";
    }

    public String getTopicPath() {
        return rootPath + File.separator + "config" + File.separator + "topics.json";
    }

    public String getTopicQueueMappingPath() {
        return rootPath + File.separator + "config" + File.separator + "topicQueueMapping.json";
    }

    public String getConsumerOffsetPath() {
        return rootPath + File.separator + "config" + File.separator + "consumerOffset.json";
    }

    public String getLmqConsumerOffsetPath() {
        return rootPath + File.separator + "config" + File.separator + "lmqConsumerOffset.json";
    }

    public String getConsumerOrderInfoPath() {
        return rootPath + File.separator + "config" + File.separator + "consumerOrderInfo.json";
    }

    public String getSubscriptionGroupPath() {
        return rootPath + File.separator + "config" + File.separator + "subscriptionGroup.json";
    }

    public String getTimerCheckPath() {
        return rootPath + File.separator + "config" + File.separator + "timercheck";
    }

    public String getTimerLogPath() {
        return rootPath + File.separator + "timerlog";
    }

    public String getTimerWheelPath() {
        return rootPath + File.separator + "timerwheel";
    }

    public String getTimerMetricsPath() {
        return rootPath + File.separator + "config" + File.separator + "timermetrics";
    }

    public String getTransactionMetricsPath() {
        return rootPath + File.separator + "config" + File.separator + "transactionMetrics";
    }

    public String getConsumerFilterPath() {
        return rootPath + File.separator + "config" + File.separator + "consumerFilter.json";
    }

    public String getMessageRequestModePath() {
        return rootPath + File.separator + "config" + File.separator + "messageRequestMode.json";
    }
}
