package cn.coderule.minimq.store.server.bootstrap;

import cn.coderule.common.convention.container.ApplicationContext;
import cn.coderule.minimq.domain.config.server.StoreConfig;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreArgument implements Serializable {
    private String[] args;
    private StoreConfig storeConfig;
    private ApplicationContext monitorContext;

    public StoreArgument(String[] args) {
        this.args = args;
    }

    public void validate() {

    }
}
