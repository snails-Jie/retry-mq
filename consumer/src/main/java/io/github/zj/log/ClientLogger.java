//package io.github.zj.log;
//
///**
// * @ClassName ClientLogger
// * @Description: 客户端日志
// * @author: zhangjie
// * @Date: 2021/2/10 12:33
// **/
//public class ClientLogger {
//
//    private static final InternalLogger CLIENT_LOGGER;
//
//    public static final String CLIENT_LOG_USESLF4J = "rocketmq.client.logUseSlf4j";
//    public static final String CLIENT_LOG_LEVEL = "rocketmq.client.logLevel";
//    public static final String CLIENT_LOG_ADDITIVE = "rocketmq.client.log.additive";
//
//    private static final boolean CLIENT_USE_SLF4J;
//
//    static{
//        CLIENT_USE_SLF4J = Boolean.parseBoolean(System.getProperty(CLIENT_LOG_USESLF4J, "false"));
//        if (!CLIENT_USE_SLF4J) {
//            InternalLoggerFactory.setCurrentLoggerType(InnerLoggerFactory.LOGGER_INNER);
//            CLIENT_LOGGER = createLogger(LoggerName.CLIENT_LOGGER_NAME);
//        }
//    }
//
//    private static InternalLogger createLogger(final String loggerName) {
//        String clientLogLevel = System.getProperty(CLIENT_LOG_LEVEL, "INFO");
//        boolean additive = "true".equalsIgnoreCase(System.getProperty(CLIENT_LOG_ADDITIVE));
//        InternalLogger logger = InternalLoggerFactory.getLogger(loggerName);
//        InnerLoggerFactory.InnerLogger innerLogger = (InnerLoggerFactory.InnerLogger) logger;
//        Logger realLogger = innerLogger.getLogger();
//    }
//
//
//
//    public static InternalLogger getLog() {
//        return CLIENT_LOGGER;
//    }
//}
