package com.wolfmq.rpc.rpc.core.invoke;

import com.wolfmq.rpc.common.rpc.core.invoke.RpcCommand;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RpcCommandTest {

    @Test
    public void testGetOpaque() {
        RpcCommand c1 = new RpcCommand();
        RpcCommand c2 = new RpcCommand();

        assertNotSame(c1.getOpaque(), c2.getOpaque(), "testGetOpaque failed");
        assertTrue(c2.getOpaque() > c1.getOpaque(), "testGetOpaque failed");

    }
}
