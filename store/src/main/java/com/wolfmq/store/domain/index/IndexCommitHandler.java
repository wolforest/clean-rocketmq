package com.wolfmq.store.domain.index;

import cn.coderule.common.util.lang.string.StringUtil;
import com.wolfmq.domain.domain.message.MessageBO;
import com.wolfmq.domain.domain.commitlog.dispatch.CommitEvent;
import com.wolfmq.domain.domain.commitlog.dispatch.CommitHandler;
import com.wolfmq.domain.domain.index.IndexService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IndexCommitHandler implements CommitHandler {
    private final IndexService indexService;

    public IndexCommitHandler(IndexService indexService) {
        this.indexService = indexService;
    }

    @Override
    public void handle(CommitEvent event) {
        MessageBO messageBO = event.getMessageBO();
        if (messageBO == null || !messageBO.isNormalOrCommitMessage()) {
            return;
        }

        String topic = messageBO.getTopic();
        long phyOffset = messageBO.getCommitOffset();
        long storeTimestamp = messageBO.getStoreTimestamp();

        if (StringUtil.isBlank(topic) || phyOffset <= 0) {
            return;
        }

        String keys = messageBO.getKeys();
        if (StringUtil.notBlank(keys)) {
            indexService.buildIndex(topic, keys, phyOffset, storeTimestamp);
        }

        String messageId = messageBO.getMessageId();
        if (StringUtil.notBlank(messageId)) {
            indexService.buildIndex(topic, messageId.trim(), phyOffset, storeTimestamp);
        }
    }
}
