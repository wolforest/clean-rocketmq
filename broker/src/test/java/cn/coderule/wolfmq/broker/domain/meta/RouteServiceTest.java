package cn.coderule.wolfmq.broker.domain.meta;

import cn.coderule.common.util.net.NetworkUtil;
import cn.coderule.wolfmq.domain.config.server.BrokerConfig;
import cn.coderule.wolfmq.domain.core.exception.InvalidConfigException;
import cn.coderule.wolfmq.domain.domain.cluster.RequestContext;
import cn.coderule.wolfmq.domain.domain.cluster.route.RouteInfo;
import cn.coderule.wolfmq.rpc.registry.route.RouteLoader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RouteServiceTest {

    @Test
    void testConstructorThrowsWhenBothNull() {
        BrokerConfig brokerConfig = mock(BrokerConfig.class);
        assertThrows(InvalidConfigException.class,
            () -> new RouteService(brokerConfig, null, null));
    }

    @Test
    void testGetWithRouteInfoNull() {
        BrokerConfig brokerConfig = mock(BrokerConfig.class);
        RouteLoader routeLoader = mock(RouteLoader.class);
        when(routeLoader.getRoute(anyString())).thenReturn(null);

        RouteService service = new RouteService(brokerConfig, routeLoader, null);
        RequestContext context = mock(RequestContext.class);
        assertNull(service.get(context, "topic"));
    }

    @Test
    void testGetQueueViewReturnsNullWhenRouteNull() {
        BrokerConfig brokerConfig = mock(BrokerConfig.class);
        RouteLoader routeLoader = mock(RouteLoader.class);
        when(routeLoader.getRoute(anyString())).thenReturn(null);

        RouteService service = new RouteService(brokerConfig, routeLoader, null);
        RequestContext context = mock(RequestContext.class);
        assertNull(service.getQueueView(context, "topic"));
    }
}