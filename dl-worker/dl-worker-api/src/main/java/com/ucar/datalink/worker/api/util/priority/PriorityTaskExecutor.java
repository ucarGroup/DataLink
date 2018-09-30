package com.ucar.datalink.worker.api.util.priority;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 优先级任务执行器,保证所有任务按照优先级顺序，全局有序执行.
 * 优先级顺序：从小到大
 *
 * @author lubiao
 */
public class PriorityTaskExecutor {
    private static final Logger logger = LoggerFactory.getLogger(PriorityTaskExecutor.class);

    private final AtomicInteger latch;
    private final PriorityBarrier barrier;
    private final BlockingQueue<Long> priorities;


    public PriorityTaskExecutor(int taskCount) {
        latch = new AtomicInteger(taskCount);
        barrier = new PriorityBarrier(Integer.MIN_VALUE);
        priorities = new PriorityBlockingQueue<>();
    }

    /**
     * 启动任务
     */
    public <T> void execute(PriorityTask<T> task) throws InterruptedException {
        synchronized (this) {
            if (latch.get() == 0) {
                throw new IllegalStateException("All Tasks has submitted,can not accept new Task.");
            }

            task.priorities().stream()
                    .forEach(p -> priorities.add(p));

            int number = latch.decrementAndGet();
            if (number == 0) {
                Long initPriority = this.priorities.peek();
                if (initPriority != null) {
                    barrier.signal(initPriority);
                }
            }
        }

        for (Long priority : task.priorities()) {
            this.await(priority);

            logger.debug("Start write for priority:" + priority);
            task.getCallback().call(task.getBucket(priority));

            this.single(priority);
            logger.debug("End write for priority:" + priority);

        }
    }

    /**
     * 等待指定优先级获得执行权限
     */
    private void await(long priority) throws InterruptedException {
        barrier.await(priority);
    }

    /**
     * 通知下一个priority
     */
    private synchronized void single(long priority) throws InterruptedException {
        this.priorities.remove(priority);
        // 触发下一个可运行的weight
        Long nextPriority = this.priorities.peek();
        if (nextPriority != null) {
            barrier.signal(nextPriority);
        }
    }
}
