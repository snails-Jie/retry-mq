package io.github.zj;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @ClassName CompletableFutureTest
 * @Description: TODO
 * @author: zhangjie
 * @Date: 2021/2/9 17:47
 **/
public class CompletableFutureTest {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        long startTime = System.currentTimeMillis();

        CompletableFuture<Integer> completableFuture = CompletableFuture.supplyAsync(()->{
            System.out.println(Thread.currentThread().getName()+"\t completableFuture2");
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            int i = 10/0;
            return i;
        });
        completableFuture.whenComplete((result,throwable) -> {
            if(throwable != null){
                System.out.println("-------throwable="+throwable);
            }else{
                System.out.println("-------结果="+result);
            }
            System.out.println("whenComplete耗时：" + (System.currentTimeMillis() - startTime));
        });

        System.out.println("执行完耗时：" + (System.currentTimeMillis() - startTime));
        Thread.sleep(30000L);


    }
}
