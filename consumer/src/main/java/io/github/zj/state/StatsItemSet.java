//package io.github.zj.state;
//
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.ConcurrentMap;
//import java.util.concurrent.ScheduledExecutorService;
//
///**
// * @ClassName StatsItemSet
// * @Description: TODO
// * @author: zhangjie
// * @Date: 2021/2/10 12:17
// **/
//public class StatsItemSet {
//
//    private final String statsName;
//    private final ScheduledExecutorService scheduledExecutorService;
//
//    /**
//     * 指标key -> 指标信息
//     */
//    private final ConcurrentMap<String, StatsItem> statsItemTable = new ConcurrentHashMap<String, StatsItem>(128);
//
//
//    public StatsItemSet(String statsName, ScheduledExecutorService scheduledExecutorService){
//        this.statsName = statsName;
//        this.scheduledExecutorService = scheduledExecutorService;
//    }
//
//    public void addRTValue(final String statsKey, final int incValue, final int incTimes) {
//        StatsItem statsItem = this.getAndCreateRTStatsItem(statsKey);
//    }
//
//    public StatsItem getAndCreateRTStatsItem(final String statsKey) {
//        return getAndCreateItem(statsKey, true);
//    }
//
//    public StatsItem getAndCreateItem(final String statsKey, boolean rtItem) {
//        StatsItem statsItem = this.statsItemTable.get(statsKey);
//        if (null == statsItem) {
//            if (rtItem) {
//                statsItem = new RTStatsItem(this.statsName, statsKey, this.scheduledExecutorService, this.log);
//            }
//        }
//    }
//}
