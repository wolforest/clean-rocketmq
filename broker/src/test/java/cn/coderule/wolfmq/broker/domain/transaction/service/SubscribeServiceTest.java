package cn.coderule.wolfmq.broker.domain.transaction.service;

import cn.coderule.wolfmq.domain.domain.cluster.RequestContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SubscribeServiceTest {

    @Test
    void testSubscribeDoesNotThrow() {
        SubscribeService service = new SubscribeService();
        RequestContext context = new RequestContext();
        assertDoesNotThrow(() -> service.subscribe(context, "topic", "group"));
    }
}