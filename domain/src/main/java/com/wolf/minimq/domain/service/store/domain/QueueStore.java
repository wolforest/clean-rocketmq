package com.wolf.minimq.domain.service.store.domain;

import com.wolf.minimq.domain.model.bo.QueueUnit;
import java.util.List;

public interface QueueStore {
    String getTopic();
    int getQueueId();
    QueueUnit fetch(long offset);
    List<QueueUnit> fetch(long offset, int num);
}
