package cn.coderule.wolfmq.domain.config.store;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;
import lombok.Data;

@Data
public class KVConfig implements Serializable {
    private String dbName;
    private Set<String> tables = new TreeSet<>();
}
