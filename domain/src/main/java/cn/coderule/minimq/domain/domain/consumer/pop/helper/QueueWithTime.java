package cn.coderule.minimq.domain.domain.consumer.pop.helper;

import java.io.Serializable;
import java.util.concurrent.LinkedBlockingDeque;
import lombok.Getter;
import lombok.Setter;

@Getter
public class QueueWithTime<T> implements Serializable {
    private final LinkedBlockingDeque<T> queue;
    @Setter
    private long time;

    public QueueWithTime() {
        this.queue = new LinkedBlockingDeque<>();
        this.time = System.currentTimeMillis();
    }

}
