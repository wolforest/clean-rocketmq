package cn.coderule.minimq.rpc.rpc.core.invoke;

import org.junit.Test;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

public class RpcCommandTest {

    @Test
    public void testGetOpaque() {
        RpcCommand c1 = new RpcCommand();
        RpcCommand c2 = new RpcCommand();

        assertNotSame("testGetOpaque failed", c1.getOpaque(), c2.getOpaque());
        assertTrue("testGetOpaque failed", c2.getOpaque() > c1.getOpaque());

    }
}
