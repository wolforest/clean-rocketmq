package cn.coderule.minimq.rpc.common.rpc.config;

import lombok.Data;

@Data
public class ProxyConfig {
    private String addr;
    private String username;
    private String password;

    public ProxyConfig() {
    }

    public ProxyConfig(String addr) {
        this.addr = addr;
    }

    public ProxyConfig(String addr, String username, String password) {
        this.addr = addr;
        this.username = username;
        this.password = password;
    }
}
