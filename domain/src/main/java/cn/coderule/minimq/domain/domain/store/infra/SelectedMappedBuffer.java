package cn.coderule.minimq.domain.domain.store.infra;

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
public class SelectedMappedBuffer implements Serializable {
    private long startOffset;
    private ByteBuffer byteBuffer;
    private int size;
    protected MappedFile mappedFile;
    @Builder.Default
    private boolean isInCache = true;


    public synchronized void release() {
        if (this.mappedFile == null) {
            return;
        }

        this.mappedFile.release();
        this.mappedFile = null;
    }

    public synchronized boolean hasReleased() {
        return null == this.mappedFile;
    }

    public boolean isInMemory() {
        if (mappedFile == null) {
            return true;
        }

        long pos = startOffset - mappedFile.getMinOffset();
        return true;
    }

}
