

/**
 * $Id: GetAllTopicConfigResponseHeader.java 1835 2013-05-16 02:00:50Z vintagewang@apache.org $
 */package cn.coderule.minimq.rpc.store.protocol.header;

import cn.coderule.minimq.rpc.common.rpc.core.annotation.RocketMQAction;
import cn.coderule.minimq.rpc.common.rpc.core.enums.Action;
import cn.coderule.minimq.rpc.common.rpc.core.enums.ResourceType;
import cn.coderule.minimq.rpc.common.rpc.core.exception.RemotingCommandException;
import cn.coderule.minimq.rpc.common.rpc.protocol.code.RequestCode;
import cn.coderule.minimq.rpc.common.rpc.protocol.header.CommandHeader;


@RocketMQAction(value = RequestCode.GET_ALL_TOPIC_CONFIG, resource = ResourceType.TOPIC, action = Action.LIST)
public class GetAllTopicConfigResponseHeader implements CommandHeader {

    private Boolean lo;

    @Override
    public void checkFields() throws RemotingCommandException {
    }
}
