package com.wolfmq.common.server.store.checkpoint.store;

import com.wolfmq.common.server.store.checkpoint.model.CommitCheckpoint;
import com.wolfmq.common.server.store.checkpoint.model.DispatcherCheckpoint;
import com.wolfmq.common.server.store.checkpoint.model.IndexCheckpoint;
import com.wolfmq.common.server.store.checkpoint.model.MasterCheckpoint;
import com.wolfmq.common.server.store.checkpoint.model.QueueCheckpoint;
import java.io.Serializable;
import java.util.TreeMap;
import lombok.Data;

@Data
public class MemoryCheckpointStore implements Serializable {
    private CommitCheckpoint commit;
    private DispatcherCheckpoint dispatcher;
    private MasterCheckpoint master;
    private IndexCheckpoint index;

    private TreeMap<String, TreeMap<Integer, QueueCheckpoint>> queueTree = new TreeMap<>();

    public QueueCheckpoint getQueue(String topic, int queueId) {
        if (!queueTree.containsKey(topic)) {
            throw new IllegalArgumentException("topic " + topic + " not exist");
        }

        if (!queueTree.get(topic).containsKey(queueId)) {
            throw new IllegalArgumentException("queue " + queueId + " not exist");
        }

        QueueCheckpoint checkpoint = queueTree.get(topic).get(queueId);
        if (checkpoint == null) {
            throw new IllegalArgumentException("queue " + queueId + " not exist");
        }

        return checkpoint;
    }

}
