package cn.coderule.minimq.broker.server.grpc.interceptor;

import cn.coderule.minimq.domain.domain.enums.consume.ConsumerEvent;
import cn.coderule.minimq.domain.service.broker.listener.ConsumerListener;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GrpcConsumerListener implements ConsumerListener {
    @Override
    public void handle(ConsumerEvent event, String group, Object... args) {

    }

    @Override
    public void shutdown() {

    }
}
