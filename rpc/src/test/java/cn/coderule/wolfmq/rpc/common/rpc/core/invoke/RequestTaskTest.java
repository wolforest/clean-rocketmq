package cn.coderule.wolfmq.rpc.common.rpc.core.invoke;

import io.netty.channel.Channel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RequestTaskTest {

    @Test
    void testConstructor_setsFields() {
        Runnable runnable = mock(Runnable.class);
        Channel channel = mock(Channel.class);
        RpcCommand request = mock(RpcCommand.class);

        RequestTask task = new RequestTask(runnable, channel, request);

        assertEquals(runnable, task.getRunnable());
        assertEquals(channel, task.getChannel());
        assertEquals(request, task.getRequest());
    }

    @Test
    void testCreateTimestamp_isSet() {
        long before = System.currentTimeMillis();
        RequestTask task = new RequestTask(() -> {}, null, null);
        long after = System.currentTimeMillis();

        assertTrue(task.getCreateTimestamp() >= before);
        assertTrue(task.getCreateTimestamp() <= after);
    }

    @Test
    void testRun_executesRunnable() {
        Runnable runnable = mock(Runnable.class);
        RequestTask task = new RequestTask(runnable, null, null);

        task.run();

        verify(runnable).run();
    }

    @Test
    void testRun_doesNotExecuteWhenStopRunIsTrue() {
        Runnable runnable = mock(Runnable.class);
        RequestTask task = new RequestTask(runnable, null, null);
        task.setStopRun(true);

        task.run();

        verify(runnable, never()).run();
    }

    @Test
    void testStopRun_defaultIsFalse() {
        RequestTask task = new RequestTask(() -> {}, null, null);
        assertFalse(task.isStopRun());
    }

    @Test
    void testStopRun_canBeSetToTrue() {
        RequestTask task = new RequestTask(() -> {}, null, null);
        task.setStopRun(true);
        assertTrue(task.isStopRun());
    }
}