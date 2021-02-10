//package io.github.zj.log;
//
//import java.util.concurrent.ConcurrentHashMap;
//
///**
// * @ClassName InternalLoggerFactory
// * @Description: TODO
// * @author: zhangjie
// * @Date: 2021/2/10 12:37
// **/
//public abstract class InternalLoggerFactory {
//
//    public static final String LOGGER_SLF4J = "slf4j";
//
//    public static final String LOGGER_INNER = "inner";
//
//    private static String loggerType = null;
//
//    private static ConcurrentHashMap<String, InternalLoggerFactory> loggerFactoryCache = new ConcurrentHashMap<String, InternalLoggerFactory>();
//
//    static{
//        try {
//            new Slf4jLoggerFactory();
//        } catch (Throwable e) {
//            //ignore
//        }
//        try {
//            new InnerLoggerFactory();
//        } catch (Throwable e) {
//            //ignore
//        }
//    }
//    /**
//     * 通过日志名创建对应的日志实例
//     * @param name
//     * @return
//     */
//    public static InternalLogger getLogger(String name) {
//        return getLoggerFactory().getLoggerInstance(name);
//    }
//
//    private static InternalLoggerFactory getLoggerFactory() {
//        InternalLoggerFactory internalLoggerFactory = null;
//        if (loggerType != null) {
//            internalLoggerFactory = loggerFactoryCache.get(loggerType);
//        }
//    }
//
//    public static void setCurrentLoggerType(String type) {
//        loggerType = type;
//    }
//
//    protected void doRegister() {
//        String loggerType = getLoggerType();
//        if (loggerFactoryCache.get(loggerType) != null) {
//            return;
//        }
//        loggerFactoryCache.put(loggerType, this);
//    }
//
//    protected abstract String getLoggerType();
//}
