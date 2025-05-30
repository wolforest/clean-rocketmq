package cn.coderule.minimq.domain.domain.model.consumer.pop.revive;

import cn.coderule.minimq.domain.domain.model.consumer.pop.checkpoint.PopCheckPoint;
import java.io.Serializable;
import java.util.HashMap;
import lombok.Data;

@Data
public class ReviveContext implements Serializable {
    private final ReviveMap reviveMap;
    private final HashMap<String, PopCheckPoint> mockPointMap;
    private final long startTime;

    private long endTime;
    private long firstRt;
    private int noMsgCount;

    public ReviveContext() {
        this.reviveMap = new ReviveMap();
        this.mockPointMap = new HashMap<>();

        this.startTime = System.currentTimeMillis();
        this.endTime = 0;
        this.firstRt = 0;
        this.noMsgCount = 0;
    }

    public void increaseNoMsgCount() {
        this.noMsgCount++;
    }

    public HashMap<String, PopCheckPoint> getMap() {
        return reviveMap.getMap();
    }

}
