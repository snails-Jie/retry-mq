package io.github.zj.cglib;

/**
 * Cglib 代理模式中 被代理的委托类
 */
public class Dog {
    public String call() {
        System.out.println("wang wang wang");
        return "Dog ..";
    }
}
