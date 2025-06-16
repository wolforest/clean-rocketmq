package cn.coderule.minimq.rpc.broker.rpc.protocol.body;

import cn.coderule.minimq.domain.domain.model.producer.ProducerInfo;
import cn.coderule.minimq.rpc.common.rpc.protocol.codec.RpcSerializable;
import java.util.List;
import java.util.Map;

public class ProducerTableInfo extends RpcSerializable {
    public ProducerTableInfo(Map<String, List<ProducerInfo>> data) {
        this.data = data;
    }

    private Map<String, List<ProducerInfo>> data;

    public Map<String, List<ProducerInfo>> getData() {
        return data;
    }

    public void setData(Map<String, List<ProducerInfo>> data) {
        this.data = data;
    }
}
