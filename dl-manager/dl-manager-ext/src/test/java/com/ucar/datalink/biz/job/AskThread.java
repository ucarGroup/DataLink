package com.ucar.datalink.biz.job;

/**
 * Created by yang.wang09 on 2018-10-25 19:14.
 */
import java.util.concurrent.CompletableFuture;

public class AskThread implements Runnable {
    CompletableFuture<Integer> re = null;

    public AskThread(CompletableFuture<Integer> re) {
        this.re = re;
    }

    @Override
    public void run() {
        int myRe = 0;
        try {
            myRe = re.get() * re.get();
        } catch (Exception e) {
        }
        System.out.println(myRe);
    }

    public static void main(String[] args) throws InterruptedException {
        final CompletableFuture<Integer> future = new CompletableFuture<Integer>();
        new Thread(new AskThread(future)).start();
        // 模拟长时间的计算过程
        Thread.sleep(1000);
        // 告知完成结果
        future.complete(60);
    }
}
