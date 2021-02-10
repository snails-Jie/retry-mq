//package io.github.zj.state;
//
//import java.util.concurrent.ScheduledExecutorService;
//
///**
// * @ClassName ConsumerStatsManager
// * @Description: 指标监控管理
// * @author: zhangjie
// * @Date: 2021/2/10 12:12
// **/
//public class ConsumerStatsManager {
//
////    private static final InternalLogger log = ClientLogger.getLog();
//
//    private static final String TOPIC_AND_GROUP_PULL_RT = "PULL_RT";
//
//
//    private final StatsItemSet topicAndGroupPullRT;
//
//    public ConsumerStatsManager(final ScheduledExecutorService scheduledExecutorService){
//        this.topicAndGroupPullRT = new StatsItemSet(TOPIC_AND_GROUP_PULL_RT, scheduledExecutorService);
//    }
//
//    public void incPullRT(final String group, final String topic, final long rt) {
//        this.topicAndGroupPullRT.addRTValue(topic + "@" + group, (int) rt, 1);
//    }
//}
