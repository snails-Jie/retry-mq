package io.github.zj.spring.enums;

/**
 *
 */
public enum MQMsgEnum {
    /**
     * 消息体
     */
    BODY("BODY");

    private final String value;

    MQMsgEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
