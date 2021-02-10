package io.github.zj;

/**
 * 异步拉取消息接口
 * @author zhangjie
 */
public interface PullCallback {
    void onSuccess(final PullResult pullResult);

    void onException(final Throwable e);
}
