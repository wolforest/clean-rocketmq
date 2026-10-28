
package com.wolfmq.rpc.registry.protocol.header;

import com.wolfmq.rpc.common.rpc.core.annotation.CFNotNull;
import com.wolfmq.rpc.common.rpc.core.exception.RemotingCommandException;
import com.wolfmq.rpc.common.rpc.protocol.header.CommandHeader;
import lombok.Data;

@Data
public class QueryDataVersionResponseHeader implements CommandHeader {
    @CFNotNull
    private Boolean changed;

    @Override
    public void checkFields() throws RemotingCommandException {

    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("QueryDataVersionResponseHeader{");
        sb.append("changed=").append(changed);
        sb.append('}');
        return sb.toString();
    }
}
