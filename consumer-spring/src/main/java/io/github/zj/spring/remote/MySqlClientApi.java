package io.github.zj.spring.remote;

import io.github.zj.message.MessageQueue;
import io.github.zj.remote.ClientApi;
import io.github.zj.remote.ClientApiManager;
import io.github.zj.spring.dao.TopicConfigDao;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName MySqlClientApi
 * @Description: 基于MYSQL的客户端实现
 * @author: zhangjie
 * @Date: 2021/2/5 15:48
 **/
public class MySqlClientApi implements ClientApi {

    static{
        ClientApiManager.registerClient(new MySqlClientApi());
    }

    @Resource
    private TopicConfigDao topicConfigDao;


    @Override
    public List<MessageQueue> getTopicRouteInfo(String topic) {
        return topicConfigDao.queryInfo(topic);
    }

    @Override
    public List<String> findConsumerIdList(String group) {
        return topicConfigDao.findConsumerIdList(group);
    }

    @Override
    public Long fetchConsumeOffset(String groupName,MessageQueue mq) {
        Map<String,Object> param = new HashMap<>();
        param.put("groupName",groupName);
        param.put("topic",mq.getTopic());
        param.put("queueId",mq.getQueueId());
        return topicConfigDao.fetchConsumeOffset(param);
    }
}
