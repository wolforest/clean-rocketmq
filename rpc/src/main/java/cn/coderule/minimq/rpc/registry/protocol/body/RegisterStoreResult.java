
package cn.coderule.minimq.rpc.registry.protocol.body;

import java.io.Serializable;
import lombok.Data;

@Data
public class RegisterStoreResult implements Serializable {
    private String haServerAddr;
    private String masterAddr;
    private KVTable kvTable;

}
