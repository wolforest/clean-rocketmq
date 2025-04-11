package cn.coderule.minimq.broker.domain.meta;

import cn.coderule.minimq.domain.domain.model.MessageQueue;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class PublishInfo implements Serializable {
    private boolean orderTopic = false;
    private boolean hasRouteInfo = false;
    private List<MessageQueue> queueList = new ArrayList<>();
}
