package io.github.zj.remote;

import io.github.zj.PullCallback;
import io.github.zj.PullResult;
import io.github.zj.common.protocol.header.PullMessageRequestHeader;
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

    /**
     * 读取队列的消费进度
     * @param groupName
     * @param mq
     * @return
     */
    Long fetchConsumeOffset(String groupName,MessageQueue mq);

    /**
     * 拉取消息
     * @param requestHeader 拉取消息参数
     * @param pullCallback 回调函数
     * @return
     */
     void pullMessage(final PullMessageRequestHeader requestHeader, final PullCallback pullCallback);
}
