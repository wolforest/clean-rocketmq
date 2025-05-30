
package cn.coderule.minimq.rpc.common.rpc.protocol.header;

import cn.coderule.minimq.rpc.common.rpc.core.exception.RemotingCommandException;
import cn.coderule.minimq.rpc.common.rpc.protocol.codec.RocketMQSerializable;
import io.netty.buffer.ByteBuf;
import java.util.HashMap;

public interface FastCodesHeader {

    default String getAndCheckNotNull(HashMap<String, String> fields, String field) {
        String value = fields.get(field);
        if (value == null) {
            String headerClass = this.getClass().getSimpleName();
            // no exception throws, keep compatible with RemotingCommand.decodeCommandCustomHeader
        }
        return value;
    }

    default void writeIfNotNull(ByteBuf out, String key, Object value) {
        if (value != null) {
            RocketMQSerializable.writeStr(out, true, key);
            RocketMQSerializable.writeStr(out, false, value.toString());
        }
    }

    void encode(ByteBuf out);

    void decode(HashMap<String, String> fields) throws RemotingCommandException;


}
