package cn.coderule.wolfmq.broker.server.bootstrap;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BrokerArgumentTest {

    @Test
    void testBuilder() {
        BrokerArgument argument = BrokerArgument.builder()
            .rootPath("/tmp/wolfmq")
            .nameServ("127.0.0.1:9876")
            .build();

        assertNotNull(argument);
        assertEquals("/tmp/wolfmq", argument.getRootPath());
        assertEquals("127.0.0.1:9876", argument.getNameServ());
    }

    @Test
    void testDefaultConstructor() {
        BrokerArgument argument = new BrokerArgument();
        
        assertNotNull(argument);
        assertNull(argument.getRootPath());
        assertNull(argument.getNameServ());
    }

    @Test
    void testAllArgsConstructor() {
        BrokerArgument argument = new BrokerArgument("/tmp/wolfmq", "127.0.0.1:9876");
        
        assertEquals("/tmp/wolfmq", argument.getRootPath());
        assertEquals("127.0.0.1:9876", argument.getNameServ());
    }

    @Test
    void testSettersAndGetters() {
        BrokerArgument argument = new BrokerArgument();
        argument.setRootPath("/data/wolfmq");
        argument.setNameServ("192.168.1.1:9876");

        assertEquals("/data/wolfmq", argument.getRootPath());
        assertEquals("192.168.1.1:9876", argument.getNameServ());
    }

    @Test
    void testValidate() {
        BrokerArgument argument = BrokerArgument.builder()
            .rootPath("/tmp")
            .nameServ("127.0.0.1:9876")
            .build();

        // validate method is currently empty, but should not throw
        assertDoesNotThrow(argument::validate);
    }

    @Test
    void testEqualsAndHashCode() {
        BrokerArgument arg1 = BrokerArgument.builder()
            .rootPath("/tmp")
            .nameServ("127.0.0.1:9876")
            .build();
        
        BrokerArgument arg2 = BrokerArgument.builder()
            .rootPath("/tmp")
            .nameServ("127.0.0.1:9876")
            .build();

        assertEquals(arg1, arg2);
        assertEquals(arg1.hashCode(), arg2.hashCode());
    }
}
