package io.github.zj.spring.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @ClassName TopicConfigDao
 * @Description: TODO
 * @author: zhangjie
 * @Date: 2021/2/6 22:57
 **/
@Mapper
public interface TopicConfigDao {
     String queryInfo(@Param("topicName") String topicName);
}
