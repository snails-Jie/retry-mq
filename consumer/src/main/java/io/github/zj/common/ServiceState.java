package io.github.zj.common;

/**
 * @author zhangjie
 */

public enum ServiceState {
    /**
     * Service just created,not start
     */
    CREATE_JUST,
    /**
     * Service Running
     */
    RUNNING,
    /**
     * Service Start failure
     */
    START_FAILED;
}
