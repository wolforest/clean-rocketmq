package cn.coderule.minimq.domain.domain.store.infra;

import java.util.List;

public interface MappedFileQueue {
    boolean load();
    void checkSelf();
    void shutdown(long interval);
    void destroy();

    boolean isEmpty();
    int size();

    void setFileMode(int mode);

    List<MappedFile> getMappedFiles();

    void removeMappedFile(MappedFile mappedFile);
    void removeMappedFiles(List<MappedFile> files);

    MappedFile getMappedFileByIndex(int index);

    /**
     * create mappedFile by startOffset
     * @renamed from createMappedFile to createMappedFileByStartOffset
     *
     * @param startOffset startOffset of the mappedFile
     * @return mappedFile
     */
    MappedFile createMappedFileByStartOffset(long startOffset);

    /**
     * get or create MappedFile which available space > messageSize
     * @renamed from createMappedFileForSize to getOrCreateMappedFileForSize
     *
     * @param messageSize messageSize
     * @return mappedFile
     */
    MappedFile getOrCreateMappedFileForSize(int messageSize);

    /**
     * get or create mappedFile contains offset
     * @param offset offset
     * @return MappedFile
     */
    MappedFile createMappedFileForOffset(long offset);

    /**
     * get the mappedFile contains the offset
     *
     * @param offset offset
     * @return mappedFile | null
     */
    MappedFile getMappedFileByOffset(long offset);

    /**
     *
     * @return mappedFile | null
     */
    MappedFile getFirstMappedFile();

    /**
     *
     * @return mappedFile | null
     */
    MappedFile getLastMappedFile();


    long getMinOffset();
    long getMaxOffset();

    long getCommitPosition();
    long getFlushPosition();
    long getStoreTimestamp();

    long getUnCommittedSize();
    long getUnFlushedSize();

    boolean flush(int minPages);
    boolean commit(int minPages);

}
