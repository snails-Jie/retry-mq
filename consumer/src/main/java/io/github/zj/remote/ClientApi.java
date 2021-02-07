package io.github.zj.remote;

import io.github.zj.common.protocol.route.TopicRouteData;

/**
 * @author zhangjie
 */
public interface ClientApi {
    /**
     * 获取主题路由信息
     * @param topic 主题
     * @return
     */
    TopicRouteData getTopicRouteInfo(final String topic);
}
