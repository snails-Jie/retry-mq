package io.github.zj.remote;

import io.github.zj.message.MessageQueue;

import java.util.List;

/**
 * @author zhangjie
 */
public interface ClientApi {
    /**
     * 获取主题路由信息
     * @param topic 主题
     * @return
     */
    List<MessageQueue> getTopicRouteInfo(final String topic);

    /**
     * 根据消费组名查询消费者ID列表
     * @param group 消费组
     * @return 消费者ID列表
     */
    List<String> findConsumerIdList(final String group);
}
