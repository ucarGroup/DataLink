package com.ucar.datalink.worker.api.util;

import com.ucar.datalink.common.utils.NamedThreadFactory;
import org.junit.Test;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by lubiao on 2017/11/23.
 */
public class ThreadTest {

    @Test
    public void testInterrupt1() throws Exception {

        /*System.out.println(Thread.currentThread().isInterrupted());//false
        Thread.currentThread().interrupt();//设置中断
        System.out.println(Thread.currentThread().isInterrupted());//true
        System.out.println(Thread.interrupted());//返回原来的中断状态true，并清除中断
        System.out.println(Thread.currentThread().isInterrupted());//false
        System.out.println(Thread.interrupted());//false*/

        ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 5, 0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(7),
                new ThreadPoolExecutor.CallerRunsPolicy());

        List<Future> list = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            Future<Integer> f = executor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(100000000L);
                    } catch (Exception e) {
                        throw new RuntimeException("eeeee");
                    }
                }
            }, 1);
            list.add(f);
        }

        List<Runnable> rr = executor.shutdownNow();
        /*rr.forEach(r -> {
            RunnableFuture rf = (RunnableFuture) r;
            rf.cancel(true);
        });*/
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);

        for (Future<Integer> f : list) {
            try {
                f.get();
                System.out.println("good");
            } catch (Exception e) {
                //System.out.println("bad");
                e.printStackTrace();
            }
        }
    }
}
