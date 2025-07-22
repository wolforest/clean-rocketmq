package cn.coderule.minimq.broker.domain.timer.service;

import cn.coderule.common.util.lang.collection.CollectionUtil;
import cn.coderule.minimq.domain.domain.timer.TimerEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import lombok.Data;

@Data
public class SplitService implements Serializable {
    private int msgIndex = 0;
    private int fileIndex = -1;
    private List<TimerEvent> list = null;

    private final List<List<TimerEvent>> lists;
    private final int fileSize;

    public SplitService(int fileSize) {
        this.fileSize = fileSize;
        this.lists = new LinkedList<>();
    }

    public List<List<TimerEvent>> split(List<TimerEvent> origin){
        if (origin.size() < 100) {
            lists.add(origin);
            return lists;
        }

        split(origin);
        return lists;
    }

    private void startNewFile(TimerEvent event, int index) {
        msgIndex = 0;
        fileIndex = index;
        if (!CollectionUtil.isEmpty(list)) {
            lists.add(list);
        }
        list = new LinkedList<>();
        list.add(event);
    }

    private void handleOldFile(TimerEvent event) {
        assert list != null;
        list.add(event);
        if (++msgIndex % 2000 == 0) {
            lists.add(list);
            list = new ArrayList<>();
        }
    }

    private void shard(List<TimerEvent> origin) {
        for (TimerEvent event : origin) {
            int index = (int) (event.getCommitLogOffset() / fileSize);
            if (fileIndex != index) {
                startNewFile(event, index);
                continue;
            }

            handleOldFile(event);
        }

        addList();
    }

    private void addList() {
        if (CollectionUtil.isEmpty(list)) {
            return;
        }

        lists.add(list);
    }


}
