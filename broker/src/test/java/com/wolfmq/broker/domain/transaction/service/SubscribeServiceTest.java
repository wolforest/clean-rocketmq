package com.wolfmq.broker.domain.transaction.service;

import com.wolfmq.domain.server.RequestContext;
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
