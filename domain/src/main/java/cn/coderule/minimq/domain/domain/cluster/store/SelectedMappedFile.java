package cn.coderule.minimq.domain.domain.cluster.store;

import cn.coderule.minimq.domain.domain.cluster.store.infra.MappedFile;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SelectedMappedFile implements Serializable {
    private int size;
    protected MappedFile mappedFile;
}
