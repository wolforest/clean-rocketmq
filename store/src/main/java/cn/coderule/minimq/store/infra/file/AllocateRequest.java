package cn.coderule.minimq.store.infra.file;

import cn.coderule.minimq.domain.domain.store.infra.MappedFile;
import java.io.File;
import java.util.concurrent.CountDownLatch;
import lombok.Data;

@Data
public class AllocateRequest implements Comparable<AllocateRequest> {
    // Full file path
    private String filePath;
    private int fileSize;
    private final CountDownLatch countDownLatch = new CountDownLatch(1);
    protected volatile MappedFile mappedFile = null;

    public AllocateRequest(String filePath, int fileSize) {
        this.filePath = filePath;
        this.fileSize = fileSize;
    }

    public int compareTo(AllocateRequest other) {
        if (this.fileSize < other.fileSize) {
            return 1;
        }

        if (this.fileSize > other.fileSize) {
            return -1;
        }

        // return this.fileSize < other.fileSize ? 1 : this.fileSize > other.fileSize ? -1 : 0;
        int mIndex = this.filePath.lastIndexOf(File.separator);
        long mName = Long.parseLong(this.filePath.substring(mIndex + 1));
        int oIndex = other.filePath.lastIndexOf(File.separator);
        long oName = Long.parseLong(other.filePath.substring(oIndex + 1));
        return Long.compare(mName, oName);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((filePath == null) ? 0 : filePath.hashCode());
        result = prime * result + fileSize;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AllocateRequest other = (AllocateRequest) obj;
        if (filePath == null) {
            if (other.filePath != null)
                return false;
        } else if (!filePath.equals(other.filePath))
            return false;

        return fileSize == other.fileSize;
    }
}
