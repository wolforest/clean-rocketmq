package cn.coderule.minimq.domain.model.checkpoint;

public interface CheckPoint {
    boolean isShutdownSuccessful();
    void setShutdownSuccessful(boolean shutdownSuccessful);

    void load();
    void save();

    /**
     * get a copy of the min offset object
     *  - do not modify the copy min offset object
     *  - it's useless, it won't be saved
     *
     * @return the copy of min offset object
     */
    Offset getMinOffset();

    /**
     * try to change min offset
     *  - check if the file checkpoint.json.commit exists
     *  - commit the last attempt
     *  - delete .try file if exists
     *  - create file checkpoint.json.try
     *  - save the copy of min offset object
     *  - change minCopy
     *
     */
    Offset tryMinOffset();

    /**
     * commit the attempt to change min offset
     *  - rename file checkpoint.json.try to checkpoint.json.commit
     *  - replace min offset object by the copy
     *  - rename file checkpoint.json.commit to checkpoint.json
     *  - delete checkpoint.json
     *
     */
    void commitMinOffset();

    /**
     * cancel the attempt to change min offset
     *  - check if the file checkpoint.json.commit exists
     *  - if yes log the error and abort
     *  - check if the file checkpoint.json.try exists
     *  - if not log the warning and abort
     *  - delete file checkpoint.json.try
     *
     */
    void cancelMinOffset();

    /**
     * get the max offset object
     *  - modify it if needed
     *  - modification will be saved
     *
     * @return the max offset object
     */
    Offset getMaxOffset();
    void saveMaxOffset();

}
