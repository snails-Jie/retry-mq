package io.github.zj.example;

import io.github.zj.spring.remote.MySqlClientApi;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @ClassName UserController
 * @Description: TODO
 * @author: zhangjie
 * @Date: 2021/2/6 23:10
 **/
@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private MySqlClientApi mySqlClientApi;

    @RequestMapping("/hello")
    public String sayHello(String name){
        mySqlClientApi.getTopicRouteInfo("test");
        return name + " hello";
    }
}
