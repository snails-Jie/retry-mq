package io.github.zj.spring.config;

import com.zaxxer.hikari.HikariConfig;

/**
 * @ClassName ConsumerProperties
 * @Description: 消费端配置参数
 * @author: zhangjie
 * @Date: 2021/2/6 12:57
 **/
public class ConsumerProperties {
    /** 消费类型 */
    private String type;
    /** SPI机制根据配置去发现 */
    private String className;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

}
