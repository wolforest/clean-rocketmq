package cn.coderule.wolfmq.domain.domain.meta.order;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class OrderConverterTest {

    @Test
    void toOrderInfo_mapsFields() {
        OrderRequest request = OrderRequest.builder()
            .attemptId("attempt1")
            .dequeueTime(2000L)
            .invisibleTime(30000L)
            .offsetList(Arrays.asList(100L, 105L, 110L))
            .build();

        OrderInfo info = OrderConverter.toOrderInfo(request);

        assertEquals("attempt1", info.getAttemptId());
        assertEquals(2000L, info.getPopTime());
        assertEquals(30000L, info.getInvisibleTime());
        assertNotNull(info.getOffsetList());
        assertEquals(3, info.getOffsetList().size());
        assertEquals(100L, info.getOffsetList().get(0));
        assertEquals(0, info.getCommitOffsetBit());
    }

    @Test
    void toOrderInfo_singleOffset() {
        OrderRequest request = OrderRequest.builder()
            .attemptId("attempt1")
            .dequeueTime(2000L)
            .invisibleTime(30000L)
            .offsetList(java.util.Collections.singletonList(100L))
            .build();

        OrderInfo info = OrderConverter.toOrderInfo(request);
        assertEquals(1, info.getOffsetList().size());
        assertEquals(100L, info.getOffsetList().get(0));
    }
}