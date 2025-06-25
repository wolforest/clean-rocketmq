package cn.coderule.minimq.domain.domain.model.cluster.store;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreTask implements Serializable {
    private String groupName;

    @Builder.Default
    private Set<Integer> reviveQueueSet = new TreeSet<>();
    @Builder.Default
    private Set<Integer> timerQueueSet = new TreeSet<>();
    @Builder.Default
    private Set<Integer> transactionQueueSet = new TreeSet<>();
}
