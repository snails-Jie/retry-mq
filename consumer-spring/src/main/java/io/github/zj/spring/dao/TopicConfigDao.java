package io.github.zj.spring.dao;

import io.github.zj.message.MessageQueue;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @ClassName TopicConfigDao
 * @Description: TODO
 * @author: zhangjie
 * @Date: 2021/2/6 22:57
 **/
@Mapper
public interface TopicConfigDao {

     List<MessageQueue> queryInfo(@Param("topicName") String topicName);

     List<String> findConsumerIdList(@Param("group")String group);
}
