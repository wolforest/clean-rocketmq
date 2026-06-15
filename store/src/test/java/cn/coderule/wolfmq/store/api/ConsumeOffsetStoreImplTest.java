package cn.coderule.wolfmq.store.api;

import cn.coderule.wolfmq.domain.domain.meta.offset.GroupResult;
import cn.coderule.wolfmq.domain.domain.meta.offset.OffsetFilter;
import cn.coderule.wolfmq.domain.domain.meta.offset.OffsetRequest;
import cn.coderule.wolfmq.domain.domain.meta.offset.OffsetResult;
import cn.coderule.wolfmq.domain.domain.meta.offset.TopicResult;
import cn.coderule.wolfmq.domain.domain.store.domain.meta.ConsumeOffsetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConsumeOffsetStoreImplTest {

    private ConsumeOffsetService offsetService;
    private ConsumeOffsetStoreImpl store;

    @BeforeEach
    void setUp() {
        offsetService = mock(ConsumeOffsetService.class);
        store = new ConsumeOffsetStoreImpl(offsetService);
    }

    @Test
    void getOffset_ShouldDelegateToService() {
        when(offsetService.getOffset("g1", "t1", 0)).thenReturn(100L);
        OffsetRequest request = OffsetRequest.builder()
            .consumerGroup("g1")
            .topicName("t1")
            .queueId(0)
            .build();

        OffsetResult result = store.getOffset(request);

        assertEquals(100L, result.getOffset());
        verify(offsetService).getOffset("g1", "t1", 0);
    }

    @Test
    void getAndRemove_ShouldDelegateToService() {
        when(offsetService.getAndRemove("g1", "t1", 0)).thenReturn(200L);
        OffsetRequest request = OffsetRequest.builder()
            .consumerGroup("g1")
            .topicName("t1")
            .queueId(0)
            .build();

        OffsetResult result = store.getAndRemove(request);

        assertEquals(200L, result.getOffset());
        verify(offsetService).getAndRemove("g1", "t1", 0);
    }

    @Test
    void putOffset_ShouldDelegateToService() {
        OffsetRequest request = OffsetRequest.builder()
            .consumerGroup("g1")
            .topicName("t1")
            .queueId(0)
            .newOffset(300L)
            .build();

        store.putOffset(request);

        verify(offsetService).putOffset("g1", "t1", 0, 300L);
    }

    @Test
    void deleteByTopic_ShouldDelegateToService() {
        OffsetFilter filter = new OffsetFilter();
        filter.setTopic("t1");

        store.deleteByTopic(filter);

        verify(offsetService).deleteByTopic("t1");
    }

    @Test
    void deleteByGroup_ShouldDelegateToService() {
        OffsetFilter filter = new OffsetFilter();
        filter.setGroup("g1");

        store.deleteByGroup(filter);

        verify(offsetService).deleteByGroup("g1");
    }

    @Test
    void findTopicByGroup_ShouldDelegateToService() {
        when(offsetService.findTopicByGroup("g1")).thenReturn(Set.of("t1", "t2"));
        OffsetFilter filter = new OffsetFilter();
        filter.setGroup("g1");

        TopicResult result = store.findTopicByGroup(filter);

        assertEquals(Set.of("t1", "t2"), result.getTopicSet());
        verify(offsetService).findTopicByGroup("g1");
    }

    @Test
    void findGroupByTopic_ShouldDelegateToService() {
        when(offsetService.findGroupByTopic("t1")).thenReturn(Set.of("g1"));
        OffsetFilter filter = new OffsetFilter();
        filter.setTopic("t1");

        GroupResult result = store.findGroupByTopic(filter);

        assertEquals(Set.of("g1"), result.getGroupSet());
        verify(offsetService).findGroupByTopic("t1");
    }

    @Test
    void getAllOffsetJson_ShouldReturnEmpty() {
        assertEquals("", store.getAllOffsetJson());
    }
}