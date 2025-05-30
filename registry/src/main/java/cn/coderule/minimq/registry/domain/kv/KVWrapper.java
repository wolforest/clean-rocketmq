package cn.coderule.minimq.registry.domain.kv;

import cn.coderule.minimq.rpc.common.rpc.protocol.codec.RpcSerializable;
import java.util.HashMap;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class KVWrapper extends RpcSerializable {
    // namespace -> key -> value
    private HashMap<String, HashMap<String, String>> configTable;
}
