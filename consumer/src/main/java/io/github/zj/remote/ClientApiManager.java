package io.github.zj.remote;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @ClassName ClientApiManager
 * @Description: 客户端调用管理类 -基于DriverManager的SPI注册Driver的思想
 * @author: zhangjie
 * @Date: 2021/2/5 15:28
 **/
public class ClientApiManager {
    private final static CopyOnWriteArrayList<ClientApi> clientApiList = new CopyOnWriteArrayList<>();

    static{
        loadInitialClientApi();
    }

    //通过SPI机制触发初始化
    private static void loadInitialClientApi(){
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            @Override
            public Void run() {
                ServiceLoader<ClientApi> loaderClientApis =ServiceLoader.load(ClientApi.class);
                Iterator<ClientApi> clientApiIterator = loaderClientApis.iterator();
                try{
                    while (clientApiIterator.hasNext()){
                        clientApiIterator.next();
                    }
                }catch (Throwable t){
                    // Do nothing
                }
                return null;
            }
        });
    }

    public static synchronized void registerClient(ClientApi clientApi){
        if(clientApi != null){
            clientApiList.addIfAbsent(clientApi);
        }else{
            throw new NullPointerException();
        }
    }

    public static List<ClientApi> getClientApis() {
        return clientApiList;
    }



}
