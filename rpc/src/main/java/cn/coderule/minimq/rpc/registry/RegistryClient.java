package cn.coderule.minimq.rpc.registry;

import cn.coderule.minimq.domain.model.Topic;
import cn.coderule.minimq.rpc.common.RpcClient;
import cn.coderule.minimq.rpc.common.protocol.DataVersion;
import java.util.List;

public interface RegistryClient {
    void setRpcClient(RpcClient rpcClient);

    List<String> getServerList();
    void setServerList(List<String> serverList);
    void scanServer();

    void registerTopic(Topic topic, DataVersion version);
    void registerStore();
}
