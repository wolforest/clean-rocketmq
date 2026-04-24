package cn.coderule.wolfmq.registry.domain.store.model;

import cn.coderule.wolfmq.domain.domain.cluster.server.GroupInfo;
import cn.coderule.wolfmq.domain.domain.cluster.server.StoreInfo;
import cn.coderule.wolfmq.domain.domain.meta.topic.Topic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class RouteTest {

    private Route route;

    @BeforeEach
    void setUp() {
        route = new Route();
    }

    @Test
    void testConstructor() {
        assertNotNull(route);
        assertTrue(route.isTopicEmpty());
        assertTrue(route.isGroupEmpty());
        assertTrue(route.isClusterEmpty());
        assertTrue(route.isFilterEmpty());
        assertTrue(route.isQueueMapEmpty());
    }

    @Test
    void testGetOrCreateGroup() {
        GroupInfo groupInfo = route.getOrCreateGroup("zone1", "cluster1", "broker-a", false);
        
        assertNotNull(groupInfo);
        assertEquals("broker-a", groupInfo.getBrokerName());
        assertTrue(route.containsGroup("broker-a"));
    }

    @Test
    void testGetOrCreateGroupReturnsExisting() {
        GroupInfo group1 = route.getOrCreateGroup("zone1", "cluster1", "broker-a", false);
        GroupInfo group2 = route.getOrCreateGroup("zone1", "cluster1", "broker-a", false);
        
        assertSame(group1, group2);
    }

    @Test
    void testAddGroupToCluster() {
        route.addGroupToCluster("cluster1", "broker-a");
        route.addGroupToCluster("cluster1", "broker-b");
        
        Set<String> groups = route.getGroupInCluster("cluster1");
        assertNotNull(groups);
        assertEquals(2, groups.size());
        assertTrue(groups.contains("broker-a"));
        assertTrue(groups.contains("broker-b"));
    }

    @Test
    void testRemoveGroup() {
        route.getOrCreateGroup("zone1", "cluster1", "broker-a", false);
        
        assertTrue(route.containsGroup("broker-a"));
        
        GroupInfo removed = route.removeGroup("broker-a");
        
        assertNotNull(removed);
        assertFalse(route.containsGroup("broker-a"));
    }

    @Test
    void testRemoveGroupNotFound() {
        GroupInfo removed = route.removeGroup("non-existent");
        assertNull(removed);
    }

    @Test
    void testRemoveGroupInCluster() {
        route.addGroupToCluster("cluster1", "broker-a");
        route.addGroupToCluster("cluster1", "broker-b");
        
        route.removeGroupInCluster("cluster1", "broker-a");
        
        Set<String> groups = route.getGroupInCluster("cluster1");
        assertEquals(1, groups.size());
        assertFalse(groups.contains("broker-a"));
    }

    @Test
    void testRemoveGroupInClusterRemovesClusterWhenEmpty() {
        route.addGroupToCluster("cluster1", "broker-a");
        
        route.removeGroupInCluster("cluster1", "broker-a");
        
        assertNull(route.getGroupInCluster("cluster1"));
    }

    @Test
    void testSaveAndGetTopic() {
        Topic topic = new Topic();
        topic.setTopicName("test-topic");
        topic.setReadQueueNums(8);
        topic.setWriteQueueNums(8);
        topic.setPerm(6);
        
        route.saveTopic("broker-a", topic);
        
        Topic retrieved = route.getTopic("broker-a", "test-topic");
        assertNotNull(retrieved);
        assertEquals("test-topic", retrieved.getTopicName());
        assertEquals(8, retrieved.getReadQueueNums());
    }

    @Test
    void testGetTopicNotFound() {
        Topic retrieved = route.getTopic("broker-a", "non-existent");
        assertNull(retrieved);
    }

    @Test
    void testContainsTopic() {
        Topic topic = new Topic();
        topic.setTopicName("test-topic");
        route.saveTopic("broker-a", topic);
        
        assertTrue(route.containsTopic("broker-a", "test-topic"));
        assertFalse(route.containsTopic("broker-b", "test-topic"));
        assertFalse(route.containsTopic("broker-a", "non-existent"));
    }

    @Test
    void testRemoveTopicByName() {
        Topic topic = new Topic();
        topic.setTopicName("test-topic");
        route.saveTopic("broker-a", topic);
        
        route.removeTopic("test-topic");
        
        assertFalse(route.containsTopic("broker-a", "test-topic"));
        assertTrue(route.isTopicEmpty());
    }

    @Test
    void testRemoveTopicByGroupAndName() {
        Topic topic = new Topic();
        topic.setTopicName("test-topic");
        route.saveTopic("broker-a", topic);
        
        route.removeTopic("broker-a", "test-topic");
        
        assertFalse(route.containsTopic("broker-a", "test-topic"));
    }

    @Test
    void testGetGroupByTopic() {
        Topic topic = new Topic();
        topic.setTopicName("test-topic");
        route.saveTopic("broker-a", topic);
        route.saveTopic("broker-b", topic);
        
        Set<String> groups = route.getGroupByTopic("test-topic");
        
        assertEquals(2, groups.size());
        assertTrue(groups.contains("broker-a"));
        assertTrue(groups.contains("broker-b"));
    }

    @Test
    void testGetTopicByGroup() {
        Topic topic1 = new Topic();
        topic1.setTopicName("topic1");
        route.saveTopic("broker-a", topic1);
        
        Topic topic2 = new Topic();
        topic2.setTopicName("topic2");
        route.saveTopic("broker-a", topic2);
        
        Set<String> topics = route.getTopicByGroup("broker-a");
        
        assertEquals(2, topics.size());
        assertTrue(topics.contains("topic1"));
        assertTrue(topics.contains("topic2"));
    }

    @Test
    void testIsEmptyMethods() {
        assertTrue(route.isTopicEmpty());
        assertTrue(route.isGroupEmpty());
        assertTrue(route.isClusterEmpty());
        assertTrue(route.isFilterEmpty());
        assertTrue(route.isQueueMapEmpty());
    }

    @Test
    void testSaveFilterAndGet() {
        StoreInfo storeInfo = StoreInfo.builder()
            .clusterName("cluster1")
            .groupName("broker-a")
            .address("127.0.0.1:10911")
            .build();
        
        List<String> filterList = List.of("filter1", "filter2");
        route.saveFilter(storeInfo, filterList);
        
        List<String> retrieved = route.getFilter(storeInfo);
        assertNotNull(retrieved);
        assertEquals(2, retrieved.size());
        assertFalse(route.isFilterEmpty());
    }

    @Test
    void testRemoveFilter() {
        StoreInfo storeInfo = StoreInfo.builder()
            .clusterName("cluster1")
            .groupName("broker-a")
            .address("127.0.0.1:10911")
            .build();
        
        List<String> filterList = List.of("filter1", "filter2");
        route.saveFilter(storeInfo, filterList);
        
        route.removeFilter(storeInfo);
        
        assertNull(route.getFilter(storeInfo));
        assertTrue(route.isFilterEmpty());
    }

    @Test
    void testSaveQueueMap() {
        cn.coderule.wolfmq.domain.domain.meta.statictopic.TopicQueueMappingInfo mappingInfo = 
            new cn.coderule.wolfmq.domain.domain.meta.statictopic.TopicQueueMappingInfo();
        mappingInfo.setBname("broker-a");
        
        route.saveQueueMap("test-topic", mappingInfo);
        
        Map<String, cn.coderule.wolfmq.domain.domain.meta.statictopic.TopicQueueMappingInfo> queueMap = 
            route.getQueueMap("test-topic");
        
        assertNotNull(queueMap);
        assertTrue(queueMap.containsKey("broker-a"));
        assertFalse(route.isQueueMapEmpty());
    }
}