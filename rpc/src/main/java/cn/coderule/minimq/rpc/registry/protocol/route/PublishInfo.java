package cn.coderule.minimq.rpc.registry.protocol.route;

import cn.coderule.common.lang.concurrent.thread.local.ThreadLocalSequence;
import cn.coderule.common.util.lang.collection.CollectionUtil;
import cn.coderule.minimq.domain.domain.model.MessageQueue;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class PublishInfo implements Serializable {
    private boolean ordered = false;
    private boolean hasRoute = false;

    private RouteInfo routeInfo;
    private List<MessageQueue> queueList;

    private volatile ThreadLocalSequence sequence;

    public PublishInfo() {
        this.queueList = new ArrayList<>();
        this.sequence = new ThreadLocalSequence();
    }

    public void resetSequence() {
        this.sequence.reset();
    }

    public boolean isOk() {
        return CollectionUtil.notEmpty(queueList);
    }



}
