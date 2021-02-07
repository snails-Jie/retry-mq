package io.github.zj.config;

import io.github.zj.common.RemotingUtil;
import io.github.zj.common.UtilAll;

/**
 * @ClassName ClientConfig
 * @Description: 客户端配置
 * @author: zhangjie
 * @Date: 2021/2/3 16:39
 **/
public class ClientConfig {

    private String clientIP = RemotingUtil.getLocalAddress();

    private String instanceName = System.getProperty("rocketmq.client.name", "DEFAULT");


    /**
     * @return 客户端ID = IP + PID(进程ID)
     */
    public  String buildMQClientId() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClientIP());

        sb.append("@");
        sb.append(this.getInstanceName());
        return sb.toString();
    }

    public String getClientIP() {
        return clientIP;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void changeInstanceNameToPID() {
        if (this.instanceName.equals("DEFAULT")) {
            this.instanceName = String.valueOf(UtilAll.getPid());
        }
    }

}
