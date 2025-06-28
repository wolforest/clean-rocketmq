package cn.coderule.minimq.domain.config.store;

import java.io.Serializable;
import lombok.Data;

@Data
public class MetaConfig implements Serializable {
   private long consumeOffsetVersionUpdateStep = 500;
   private long delayOffsetVersionUpdateStep = 200;


}
