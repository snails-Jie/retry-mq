package io.github.zj;

import io.github.zj.message.MessageExt;

import java.util.List;

/**
 * @ClassName PullResult
 * @Description: 拉取消息结果实体
 * @author: zhangjie
 * @Date: 2021/2/9 10:47
 **/
public class PullResult {
    private List<MessageExt> msgFoundList;

    private long nextBeginOffset;

    public PullResult() {
    }

    public long getNextBeginOffset() {
        return nextBeginOffset;
    }

    public void setNextBeginOffset(long nextBeginOffset) {
        this.nextBeginOffset = nextBeginOffset;
    }

    public List<MessageExt> getMsgFoundList() {
        return msgFoundList;
    }
    public void setMsgFoundList(List<MessageExt> msgFoundList) {
        this.msgFoundList = msgFoundList;
    }
}
