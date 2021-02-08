package io.github.zj.factory;

import io.github.zj.MQPushConsumer;
import io.github.zj.PullMessageService;
import io.github.zj.RebalanceService;
import io.github.zj.common.ServiceState;
import io.github.zj.common.SubscriptionData;
import io.github.zj.message.MessageQueue;
import io.github.zj.remote.ClientApi;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @ClassName MQClientInstance
 * @Description: MQ客户端示例（启动多个工作线程）
 * @author: zhangjie
 * @Date: 2021/2/3 16:01
 **/
public class MQClientInstance {

    private int pollNameServerInterval = 1000 * 30;

    private final RebalanceService rebalanceService;

    private ServiceState serviceState = ServiceState.CREATE_JUST;

    private ClientApi clientApi;

    private final Lock lockNamesrv = new ReentrantLock();

    private final static long LOCK_TIMEOUT_MILLIS = 3000;

    private final String clientId;

    private final PullMessageService pullMessageService;



    /** 消费组 -> 消费实例  */
    private final ConcurrentMap<String, MQPushConsumer> consumerTable = new ConcurrentHashMap<String, MQPushConsumer>();

    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r,"MQClientFactoryScheduledThread");
        }
    });

    public MQClientInstance(String clientId){
        this.clientId = clientId;

        this.rebalanceService = new RebalanceService(this);
        this.pullMessageService = new PullMessageService(this);
    }

    /**
     * 启动客户端
     */
    public void start(){
        switch (this.serviceState) {
            case CREATE_JUST:
                this.serviceState = ServiceState.START_FAILED;

                /** 启动一系列线程服务  start*/

                // topicSubscribeInfoTable 数据服务
                this.startScheduledTask();

                // 队列负载均衡服务
                this.rebalanceService.start();

                /** 启动一系列线程服务  end*/

                this.serviceState = ServiceState.RUNNING;
                break;
            default:
                break;
        }
    }

    private void startScheduledTask() {
       this.scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
           @Override
           public void run() {
               MQClientInstance.this.updateTopicRouteInfoFromNameServer();
           }
       },10,pollNameServerInterval,TimeUnit.MILLISECONDS);
    }

    /**
     * 注册消费者
     * @param group 消费组名
     * @param consumer 消费者
     * @return 注册是否成功
     */
    public boolean registerConsumer(final String group, final MQPushConsumer consumer) {
        if (null == group || null == consumer) {
            return false;
        }
        MQPushConsumer prev = this.consumerTable.putIfAbsent(group, consumer);
        if (prev != null) {
            System.out.println("the consumer group[" + group + "] exist already.");
            return false;
        }

        return true;
    }

    public void doRebalance() {
        for (Map.Entry<String, MQPushConsumer> entry : this.consumerTable.entrySet()) {
            MQPushConsumer impl = entry.getValue();
            if (impl != null) {
                try {
                    impl.doRebalance();
                } catch (Throwable e) {
                    System.out.println("doRebalance exception"+ e);
                }
            }
        }
    }

    public void updateTopicRouteInfoFromNameServer() {
        // 获取所有订阅主题
        Set<String> topicList = new HashSet<String>();

        // Consumer
        {
            Iterator<Map.Entry<String, MQPushConsumer>> it = this.consumerTable.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, MQPushConsumer> entry = it.next();
                MQPushConsumer impl = entry.getValue();
                if (impl != null) {
                    Set<SubscriptionData> subList = impl.subscriptions();
                    if (subList != null) {
                        for (SubscriptionData subData : subList) {
                            topicList.add(subData.getTopic());
                        }
                    }
                }
            }
        }

        // 包括 消费者 + 生产者的topic
        for (String topic : topicList) {
            this.updateTopicRouteInfoFromNameServer(topic);
        }
    }

    /**
     * 1. 从namesrv中获取topic路由信息：TopicRouteData
     *    1.1 包含队列信息：QueueData
     * 2. 更新订阅信息
     *    2.1 将QueueData转换为MessageQueue  --> 队列集合：subscribeInfo
     * 3.
     * @param topic
     * @return
     */
    public boolean updateTopicRouteInfoFromNameServer(final String topic) {
        try {
            if (this.lockNamesrv.tryLock(LOCK_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)) {
                // 从注册中心（mysql）中获取topic下队列信息（对应RocketMQ的TopicRouteData->Set<MessageQueue>）
                List<MessageQueue> subscribeInfo = clientApi.getTopicRouteInfo(topic);

                Iterator<Map.Entry<String, MQPushConsumer>> it = this.consumerTable.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, MQPushConsumer> entry = it.next();
                    MQPushConsumer impl = entry.getValue();
                    if (impl != null) {
                        impl.updateTopicSubscribeInfo(topic, subscribeInfo);
                    }
                }
                return true;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 原rocketMq逻辑：
     *   1. topic -> brokerAddr(topicRouteTable)
     *      1.1 一个topic下有多个broker
     *   2. 向broker中获取消费组的消费者数
     * @param group
     * @return
     */
    public List<String> findConsumerIdList(final String group) {
        if(group == null){
            return null;
        }
        return clientApi.findConsumerIdList(group);
    }

    public ClientApi getClientApi() {
        return clientApi;
    }

    public void setClientApi(ClientApi clientApi) {
        this.clientApi = clientApi;
    }

    public String getClientId() {
        return clientId;
    }

    public PullMessageService getPullMessageService() {
        return pullMessageService;
    }


}
