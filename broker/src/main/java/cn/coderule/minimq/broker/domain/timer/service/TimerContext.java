package cn.coderule.minimq.broker.domain.timer.service;

import cn.coderule.minimq.broker.infra.store.MQStore;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.domain.cluster.task.QueueTask;
import cn.coderule.minimq.domain.domain.timer.TimerQueue;
import cn.coderule.minimq.domain.domain.timer.state.TimerState;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimerContext implements Serializable {
    private QueueTask queueTask;

    private BrokerConfig brokerConfig;
    private TimerQueue timerQueue;
    private TimerState timerState;

    private MQStore mqStore;
}
