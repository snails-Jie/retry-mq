package io.github.zj.store;

/**
 * 读取消费进度的方式
 * @author zhangjie
 */

public enum ReadOffsetType {
    /**
     * From memory
     */
    READ_FROM_MEMORY,
    /**
     * From storage
     */
    READ_FROM_STORE,
    /**
     * From memory,then from storage
     */
    MEMORY_FIRST_THEN_STORE;
}
