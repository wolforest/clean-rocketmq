package com.wolfmq.broker.domain.producer;

import com.wolfmq.broker.domain.meta.RouteService;
import com.wolfmq.common.core.exception.BrokerException;
import com.wolfmq.common.domain.message.MessageBO;
import com.wolfmq.common.server.broker.RequestContext;
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
