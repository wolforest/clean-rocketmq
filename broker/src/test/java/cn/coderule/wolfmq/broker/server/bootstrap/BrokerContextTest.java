package cn.coderule.wolfmq.broker.server.bootstrap;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BrokerContextTest {

    @Test
    void testRegisterAndGetBean() {
        String testBean = "testBean-" + System.nanoTime();
        BrokerContext.register(testBean);

        String retrieved = BrokerContext.getBean(String.class);
        assertNotNull(retrieved);
        assertTrue(retrieved.startsWith("testBean-"));
    }

    @Test
    void testRegisterWithClass() {
        Object testBean = "testBean-class-" + System.nanoTime();
        BrokerContext.register(testBean, String.class);

        String retrieved = BrokerContext.getBean(String.class);
        assertNotNull(retrieved);
    }

    @Test
    void testGetBeanNotFound() {
        // Use a unique class that's unlikely to be registered
        class UniqueTestClass {}
        Object bean = BrokerContext.getBean(UniqueTestClass.class, false);
        assertNull(bean);
    }

    @Test
    void testRegisterAndGetAPI() {
        String testAPI = "testAPI-" + System.nanoTime();
        BrokerContext.registerAPI(testAPI);

        String retrieved = BrokerContext.getAPI(String.class);
        assertNotNull(retrieved);
    }

    @Test
    void testRegisterAPIWithClass() {
        Object testAPI = "testAPI-class-" + System.nanoTime();
        BrokerContext.registerAPI(testAPI, String.class);

        String retrieved = BrokerContext.getAPI(String.class);
        assertNotNull(retrieved);
    }

    @Test
    void testRegisterAndGetMonitor() {
        String testMonitor = "testMonitor-" + System.nanoTime();
        BrokerContext.registerMonitor(testMonitor);

        String retrieved = BrokerContext.getMonitor(String.class);
        assertNotNull(retrieved);
    }

    @Test
    void testRegisterMonitorWithClass() {
        Object testMonitor = "testMonitor-class-" + System.nanoTime();
        BrokerContext.registerMonitor(testMonitor, String.class);

        String retrieved = BrokerContext.getMonitor(String.class);
        assertNotNull(retrieved);
    }

    @Test
    void testRegisterContext() {
        cn.coderule.common.convention.container.ApplicationContext subContext = 
            new cn.coderule.common.convention.container.ApplicationContext();
        String testBean = "testBean-context-" + System.nanoTime();
        subContext.register(testBean);

        BrokerContext.registerContext(subContext);

        String retrieved = BrokerContext.getBean(String.class);
        assertNotNull(retrieved);
    }
}
