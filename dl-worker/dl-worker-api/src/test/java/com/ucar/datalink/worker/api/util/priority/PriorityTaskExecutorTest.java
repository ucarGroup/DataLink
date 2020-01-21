package com.ucar.datalink.worker.api.util.priority;

import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by lubiao on 2018/5/12.
 */
public class PriorityTaskExecutorTest {

    @Test
    public void testPriorityQueue() {
        PriorityBlockingQueue<Integer> queue = new PriorityBlockingQueue<>();
        List<Integer> list = Lists.newArrayList(3, 3, 2, 2, 1, 1);
        list.forEach(i -> queue.add(i));
        list.forEach(i -> {
            System.out.println(queue.peek());
            int t = queue.peek();
            queue.remove(t);
        });
    }

    @Test
    public void test() {
        ExecutorService service = new ThreadPoolExecutor(
                10,
                10,
                60L,
                TimeUnit.SECONDS,
                new SynchronousQueue<>(),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

        List<List<Integer>> list = new ArrayList<>();
        list.add(Lists.newArrayList(3, 2, 1, 10, 100));
        list.add(Lists.newArrayList(2, 3, 1, 10));
        list.add(Lists.newArrayList(1, 3, 2, 9, 8));

        PriorityTaskExecutor controller = new PriorityTaskExecutor(list.size());
        List<Future> futures = new ArrayList<>();
        for (final List<Integer> ll : list) {
            futures.add(service.submit(() -> {
                try {
                    PriorityTask<Integer> task = buildPriorityTask(ll);
                    controller.execute(task);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }));
        }

        futures.forEach(f -> {
            try {
                f.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private PriorityTask<Integer> buildPriorityTask(List<Integer> list) {
        PriorityTask<Integer> task = new PriorityTask<>(bucket -> {
            bucket.getItems().forEach(s -> System.out.println(s));
        });
        for (Integer i : list) {
            task.addItem(i, i);
        }

        return task;
    }
}
