package cn.coderule.minimq.broker.server.grpc.interceptor;

import cn.coderule.minimq.domain.domain.enums.produce.ProducerEvent;
import cn.coderule.minimq.domain.domain.model.cluster.ClientChannelInfo;
import cn.coderule.minimq.domain.service.broker.listener.ProducerListener;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GrpcProducerListener implements ProducerListener {
    @Override
    public void handle(ProducerEvent event, String group, ClientChannelInfo clientChannelInfo) {

    }
}
