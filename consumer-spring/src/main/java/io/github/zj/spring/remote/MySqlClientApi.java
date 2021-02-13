package io.github.zj.spring.remote;

import io.github.zj.PullCallback;
import io.github.zj.PullResult;
import io.github.zj.common.protocol.header.PullMessageRequestHeader;
import io.github.zj.message.ConsumerGroupMetadata;
import io.github.zj.message.MessageExt;
import io.github.zj.message.MessageQueue;
import io.github.zj.remote.ClientApi;
import io.github.zj.remote.ClientApiManager;
import io.github.zj.spring.dao.TopicConfigDao;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    @Resource
    private TopicConfigDao topicConfigDao;


    @Override
    public ConsumerGroupMetadata readConsumerGroupMetadata(String consumerGroup) {
        return topicConfigDao.readConsumerGroupMetadata(consumerGroup);
    }

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

    @Override
    public void pullMessage(PullMessageRequestHeader requestHeader, PullCallback pullCallback) {
        CompletableFuture<PullResult> completableFuture =
                CompletableFuture.supplyAsync(()-> this.getMessage(requestHeader),executor);
        completableFuture.whenComplete((result,throwable) ->{
            if(throwable != null){
                pullCallback.onException(throwable);
            }else{
                pullCallback.onSuccess(result);
            }
        });
    }


    /**
     * 获取下一次拉取的消费进度:nextBeginOffset
     * 1. 拉取消息（100条<读取pullBatchSize参数>）
     * 1. 更新对应的消费进度 t_consumer_offset
     * @param pullMessageRequest
     * @return
     */
    private PullResult getMessage(PullMessageRequestHeader pullMessageRequest){
        PullResult pullResult = new PullResult();

        List<MessageExt>  mgsList  = topicConfigDao.pullMessage(pullMessageRequest);
        Optional<MessageExt> maxOptional = mgsList.stream().max(Comparator.comparing(MessageExt::getQueueOffset));
        if(maxOptional.isPresent()){
            pullMessageRequest.setQueueOffset(maxOptional.get().getQueueOffset());
            topicConfigDao.updateConsumeOffset(pullMessageRequest);

            pullResult.setNextBeginOffset(maxOptional.get().getQueueOffset());
            pullResult.setMsgFoundList(mgsList);
            return pullResult;
        }else{
            pullResult.setNextBeginOffset(pullMessageRequest.getQueueOffset());
            return pullResult;
        }

    }
}
