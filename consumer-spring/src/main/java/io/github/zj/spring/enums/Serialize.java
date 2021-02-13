package io.github.zj.spring.enums;

/**
 * 消费监听接收参数序列化类型
 *
 */
public enum Serialize {
    STRING("String"),
    JSON("JSON"),
    JSON_ARRAY("JSONArray");

    private String value;
    Serialize(String value) {
        this.value = value;
    }
    public String getValue() {
        return value;
    }
}
