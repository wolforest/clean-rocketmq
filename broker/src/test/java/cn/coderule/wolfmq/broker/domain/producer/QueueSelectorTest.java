package cn.coderule.wolfmq.broker.domain.producer;

import cn.coderule.wolfmq.broker.domain.meta.RouteService;
import cn.coderule.wolfmq.domain.core.exception.BrokerException;
import cn.coderule.wolfmq.domain.domain.MessageQueue;
import cn.coderule.wolfmq.domain.domain.message.MessageBO;
import cn.coderule.wolfmq.domain.domain.cluster.RequestContext;
import cn.coderule.wolfmq.domain.domain.cluster.selector.MessageQueueView;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class QueueSelectorTest {

    @Test
    void testSelectNullRoute() {
        RouteService routeService = mock(RouteService.class);
        when(routeService.getQueueView(any(), anyString())).thenReturn(null);
        QueueSelector selector = new QueueSelector(routeService);

        RequestContext context = mock(RequestContext.class);
        MessageBO msg = mock(MessageBO.class);
        when(msg.getTopic()).thenReturn("testTopic");

        assertThrows(BrokerException.class, () -> selector.select(context, msg));
    }
}