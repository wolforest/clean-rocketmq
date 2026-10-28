package com.wolfmq.rpc.registry.protocol.body;

import com.wolfmq.rpc.common.rpc.protocol.codec.RpcSerializable;
import java.util.HashMap;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class KVTable extends RpcSerializable {
    private HashMap<String, String> table = new HashMap<>();
}
