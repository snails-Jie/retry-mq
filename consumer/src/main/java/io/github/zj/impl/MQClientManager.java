package io.github.zj.impl;

import io.github.zj.config.ClientConfig;
import io.github.zj.factory.MQClientInstance;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @ClassName MQClientManager
 * @Description: 客户端管理类
 * @author: zhangjie
 * @Date: 2021/2/3 16:27
 **/
public class MQClientManager {

    private static MQClientManager instance = new MQClientManager();

    private ConcurrentMap<String, MQClientInstance> factoryTable = new ConcurrentHashMap<String, MQClientInstance>();

    public ConcurrentMap<String, MQClientInstance> getFactoryTable() {
        return factoryTable;
    }

    /** 单例 */
    public static MQClientManager getInstance() {
        return instance;
    }

    public MQClientInstance getOrCreateMQClientInstance(ClientConfig clientConfig) {
        String clientId = clientConfig.buildMQClientId();
        MQClientInstance instance = this.factoryTable.get(clientId);
        if (null == instance) {
            instance = new MQClientInstance();
            MQClientInstance prev = this.factoryTable.putIfAbsent(clientId, instance);
            if (prev != null) {
                instance = prev;
                System.out.println("Returned Previous MQClientInstance for clientId:"+ clientId);
            }else{
                System.out.println("Created new MQClientInstance for clientId:" + clientId);
            }
        }
        return instance;
    }
}
