package com.wolf.minimq.domain.model.vo;

import com.wolf.minimq.domain.service.store.infra.MappedFile;
import java.io.Serializable;
import java.nio.ByteBuffer;
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
