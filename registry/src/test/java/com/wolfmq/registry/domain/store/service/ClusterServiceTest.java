package com.wolfmq.registry.domain.store.service;

import com.wolfmq.domain.config.server.RegistryConfig;
import com.wolfmq.domain.server.registry.ClusterInfo;
import com.wolfmq.domain.server.registry.GroupInfo;
import com.wolfmq.registry.domain.store.model.Route;
import com.wolfmq.rpc.registry.protocol.body.BrokerMemberGroup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ClusterServiceTest {

    private Route route;
    private RegistryConfig config;
    private ClusterService clusterService;

    @BeforeEach
    void setUp() {
        route = new Route();
        config = mock(RegistryConfig.class);
        clusterService = new ClusterService(config, route);
    }

    @Test
    void testGetClusterInfo() {
        route.getOrCreateGroup("zone1", "cluster1", "broker-a", false);

        ClusterInfo clusterInfo = clusterService.getClusterInfo();

        assertNotNull(clusterInfo);
        assertNotNull(clusterInfo.getBrokerAddrTable());
        assertNotNull(clusterInfo.getClusterAddrTable());
    }

    @Test
    void testGetGroupInfo() {
        GroupInfo groupInfo = route.getOrCreateGroup("zone1", "cluster1", "broker-a", false);
        groupInfo.getBrokerAddrs().put(0L, "127.0.0.1:10911");

        BrokerMemberGroup memberGroup = clusterService.getGroupInfo("cluster1", "broker-a");

        assertNotNull(memberGroup);
        assertEquals("cluster1", memberGroup.getCluster());
        assertEquals("broker-a", memberGroup.getBrokerName());
        assertNotNull(memberGroup.getBrokerAddrs());
    }

    @Test
    void testGetGroupInfoNotFound() {
        BrokerMemberGroup memberGroup = clusterService.getGroupInfo("cluster1", "non-existent");

        assertNotNull(memberGroup);
        assertTrue(memberGroup.getBrokerAddrs().isEmpty());
    }

    @Test
    void testLogClusterStatusDoesNotThrow() {
        route.getOrCreateGroup("zone1", "cluster1", "broker-a", false);

        assertDoesNotThrow(() -> clusterService.logClusterStatus());
    }
}
