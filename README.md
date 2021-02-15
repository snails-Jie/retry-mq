## 基于mysql的消费端
### 如何使用
1.  引入consumer-spring的jar包
2.  配置文件进行相关配置
    - retry.config.client.datasource.type=mysql
    - retry.config.client.datasource.className=io.github.zj.spring.remote.MySqlClientApi
3.  对监听方法加上@RetryListener，并对其参数添加@RetryListenerParameter注解
### spring的适配
![](https://github.com/snails-Jie/retry-mq/blob/master/img/%E8%B0%83%E7%94%A8%E5%BA%8F%E5%88%97%E5%9B%BE.png?raw=true)
1. RetryMqListenerInitialization
    - 实现BeanPostProcessor，找到有@RetryListener注解的方法，将相关信息存储到RetryMqContext中
    - 重试上下文RetryMqContext两个属性
        - consumerInfo：存储@RetryListener注解信息（消费组）
        - retryConfMap：RetryMqConf存储关于反射相关信息（retryMqConf.getMethod().invoke）
2.  RetrySubscribeEventListener：监听容器启动事件
    - 遍历RetryMqContext的consumerInfo,启动消费者DefaultMQPushConsumer并缓存
    - 设置消息监听器,对拉取到的消息进行反射调用业务方法进行业务处理（RetryMqMessageListenerImpl）
    - RetryMqMessageListenerImpl使用Cglib动态代理支持插件化（详细见IntercepterUtil#getProxyObj）
3.  注册ClientApi（仿照数据库驱动基于SPI机制实现）
    - 在ConsumerConfig中进行注册（触发ClientApiManager.getClientApis()）
    - ClientApiManager的静态块触发SPI的服务发现机制，相应服务进行初始化
    - 基于mysql的实现：MySqlClientApi，在静态块中进行注册ClientApiManager.registerClient(new MySqlClientApi());
### 消费端的分析
1. 消费端的消费模型
![](https://github.com/snails-Jie/retry-mq/blob/master/img/%E6%B6%88%E8%B4%B9%E6%A8%A1%E5%9E%8B.png?raw=true)
2. 队列负载线程：RebalanceService（基于CountDownLatch2超时机制的定时任务）
    - 负载均衡推模式服务：RebalancePushImpl#doRebalance
    - 负载均衡算法（1.平均散列：AllocateMessageQueueAveragely）
        - 基于当前的队列数、消费者数（把队列分给消费者）
        - 添加队列消息拉取任务：pullRequestQueue
3.  消息拉取线程：PullMessageService（基于阻塞队列拉取）
    - 控制消息拉取速度（ProcessQueue）：消息数量、消息大小、消息跨度 -> 延迟拉取：延迟添加拉取任务
    - 向mysql拉取消息：ClientApi#pullMessage
    - 拉取到消息后的回调：PullCallback#onSuccess
        - 设置下次拉取的进度：pullRequest.setNextOffset
        - 将消息放入队列快照中：ProcessQueue
        - 将消息提交消息消费线程池中
4.  消息消费线程：ConsumeMessageConcurrentlyService$ConsumeRequest
    - 触发@RetryListener，进行业务消息消费


    
     