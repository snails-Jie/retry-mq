package io.github.zj.spring.remote;

import io.github.zj.message.MessageQueue;
import io.github.zj.remote.ClientApi;
import io.github.zj.remote.ClientApiManager;
import io.github.zj.spring.dao.TopicConfigDao;

import javax.annotation.Resource;
import java.util.List;

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
}
