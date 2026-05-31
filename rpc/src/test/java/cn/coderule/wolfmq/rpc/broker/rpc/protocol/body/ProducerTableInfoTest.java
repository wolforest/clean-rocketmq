package cn.coderule.wolfmq.rpc.broker.rpc.protocol.body;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ProducerTableInfoTest {

    @Test
    void testConstructor() {
        Map<String, List<ProducerInfo>> data = new HashMap<>();
        ProducerTableInfo table = new ProducerTableInfo(data);
        assertEquals(data, table.getData());
    }

    @Test
    void testSetData() {
        ProducerTableInfo table = new ProducerTableInfo(new HashMap<>());
        Map<String, List<ProducerInfo>> newData = new HashMap<>();
        newData.put("group1", new ArrayList<>());
        table.setData(newData);
        assertEquals(newData, table.getData());
    }
}