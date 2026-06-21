package cn.coderule.wolfmq.rpc.registry.protocol.body;

import cn.coderule.wolfmq.domain.domain.meta.DataVersion;
import cn.coderule.wolfmq.domain.domain.meta.statictopic.TopicQueueMappingInfo;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TopicConfigAndMappingSerializeWrapperTest {

    @Test
    void defaultConstructor_ShouldInitializeMaps() {
        TopicConfigAndMappingSerializeWrapper wrapper = new TopicConfigAndMappingSerializeWrapper();
        assertNotNull(wrapper.getTopicQueueMappingInfoMap());
        assertNotNull(wrapper.getTopicQueueMappingDetailMap());
        assertNotNull(wrapper.getMappingDataVersion());
    }

    @Test
    void gettersAndSetters() {
        TopicConfigAndMappingSerializeWrapper wrapper = new TopicConfigAndMappingSerializeWrapper();
        Map<String, TopicQueueMappingInfo> map = new HashMap<>();
        wrapper.setTopicQueueMappingInfoMap(map);
        wrapper.setMappingDataVersion(new DataVersion());

        assertEquals(map, wrapper.getTopicQueueMappingInfoMap());
        assertNotNull(wrapper.getMappingDataVersion());
    }

    @Test
    void from_WithSameType_ShouldCast() {
        TopicConfigAndMappingSerializeWrapper wrapper = new TopicConfigAndMappingSerializeWrapper();
        TopicConfigAndMappingSerializeWrapper result = TopicConfigAndMappingSerializeWrapper.from(wrapper);
        assertSame(wrapper, result);
    }
}