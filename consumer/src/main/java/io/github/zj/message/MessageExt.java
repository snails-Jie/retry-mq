package io.github.zj.message;

/**
 * @ClassName MessageExt
 * @Description: 单条消息实体类
 * @author: zhangjie
 * @Date: 2021/2/9 10:26
 **/
public class MessageExt {
    /** 对应于表的主键 */
    private long queueOffset;

    /** 消息内容 */
    private String content;

    public long getQueueOffset() {
        return queueOffset;
    }

    public void setQueueOffset(long queueOffset) {
        this.queueOffset = queueOffset;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
