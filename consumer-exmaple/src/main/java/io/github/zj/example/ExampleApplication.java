package io.github.zj.example;

import io.github.zj.spring.annotation.EnableConsumer;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @ClassName ExampleApplication
 * @Description: TODO
 * @author: zhangjie
 * @Date: 2021/2/6 23:09
 **/
@SpringBootApplication
@EnableConsumer
public class ExampleApplication {
    public static void main(String[] args) {
        SpringApplication.run(ExampleApplication.class,args);
    }
}
