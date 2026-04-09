package cn.coderule.wolfmq.store.server.bootstrap;

import cn.coderule.common.convention.container.ApplicationContext;
import cn.coderule.wolfmq.domain.config.server.StoreConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StoreArgumentTest {

    @Test
    void testConstructorWithArgs() {
        String[] args = new String[]{"arg1", "arg2"};
        StoreArgument storeArgument = new StoreArgument(args);

        assertNotNull(storeArgument);
        assertArrayEquals(args, storeArgument.getArgs());
    }

    @Test
    void testBuilder() {
        String[] args = new String[]{"test"};
        StoreConfig storeConfig = new StoreConfig();
        ApplicationContext monitorContext = new ApplicationContext();

        StoreArgument storeArgument = StoreArgument.builder()
            .args(args)
            .storeConfig(storeConfig)
            .monitorContext(monitorContext)
            .build();

        assertNotNull(storeArgument);
        assertArrayEquals(args, storeArgument.getArgs());
        assertEquals(storeConfig, storeArgument.getStoreConfig());
        assertEquals(monitorContext, storeArgument.getMonitorContext());
    }

    @Test
    void testNoArgsConstructor() {
        StoreArgument storeArgument = new StoreArgument();

        assertNotNull(storeArgument);
        assertNull(storeArgument.getArgs());
        assertNull(storeArgument.getStoreConfig());
        assertNull(storeArgument.getMonitorContext());
    }

    @Test
    void testValidate() {
        StoreArgument storeArgument = new StoreArgument(new String[]{});
        assertDoesNotThrow(storeArgument::validate);
    }

    @Test
    void testSetters() {
        String[] args = new String[]{"setterTest"};
        StoreConfig storeConfig = new StoreConfig();
        ApplicationContext monitorContext = new ApplicationContext();

        StoreArgument storeArgument = new StoreArgument();
        storeArgument.setArgs(args);
        storeArgument.setStoreConfig(storeConfig);
        storeArgument.setMonitorContext(monitorContext);

        assertArrayEquals(args, storeArgument.getArgs());
        assertEquals(storeConfig, storeArgument.getStoreConfig());
        assertEquals(monitorContext, storeArgument.getMonitorContext());
    }
}
