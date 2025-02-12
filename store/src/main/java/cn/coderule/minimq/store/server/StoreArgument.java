package cn.coderule.minimq.store.server;

import cn.coderule.common.convention.container.ApplicationContext;
import cn.coderule.minimq.domain.config.StoreConfig;
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
    private StoreConfig storeConfig;
    private ApplicationContext monitorContext;


    public void validate() {

    }
}
