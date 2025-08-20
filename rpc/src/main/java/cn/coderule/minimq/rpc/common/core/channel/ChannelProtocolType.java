
package cn.coderule.minimq.rpc.common.core.channel;

public enum ChannelProtocolType {
    UNKNOWN("unknown"),
    GRPC_V2("grpc_v2"),
    GRPC_V1("grpc_v1"),
    REMOTING("remoting");

    private final String name;

    ChannelProtocolType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
