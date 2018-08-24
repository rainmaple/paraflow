package cn.edu.ruc.iir.paraflow.loader.utils;

import cn.edu.ruc.iir.paraflow.commons.Stats;
import cn.edu.ruc.iir.paraflow.loader.ParaflowRecord;
import com.conversantmedia.util.concurrent.DisruptorBlockingQueue;
import com.conversantmedia.util.concurrent.PushPullBlockingQueue;
import com.conversantmedia.util.concurrent.SpinPolicy;
import org.junit.Test;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * paraflow
 *
 * @author guodong
 */
public class ConcurrentQueueTest
{
    @Test
    public void testPushPullQueuePerformance()
    {
        BlockingQueue<ParaflowRecord> blockingQueue = new PushPullBlockingQueue<>(500);
        Sender sender = new Sender(blockingQueue);
        Receiver receiver = new Receiver(blockingQueue);
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        executorService.submit(sender);
        executorService.submit(receiver);
        try {
            executorService.awaitTermination(100, TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testDisruptorQueuePerformance()
    {
        ExecutorService executorService = Executors.newFixedThreadPool(8);
        BlockingQueue<ParaflowRecord> blockingQueue
                = new DisruptorBlockingQueue<>(500, SpinPolicy.SPINNING);
        for (int i = 0; i < 4; i++) {
            Sender sender = new Sender(blockingQueue);
            executorService.submit(sender);
        }
        Receiver receiver = new Receiver(blockingQueue);
        executorService.submit(receiver);
        try {
            executorService.awaitTermination(100, TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private class Sender
            implements Runnable
    {
        private final BlockingQueue<ParaflowRecord> concurrentQueue;

        Sender(BlockingQueue<ParaflowRecord> concurrentQueue)
        {
            this.concurrentQueue = concurrentQueue;
        }

        private void sendMsg()
        {
            try {
                concurrentQueue.put(new ParaflowRecord(1, System.currentTimeMillis(), 1, (Object) new byte[200]));
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        @Override
        public void run()
        {
            while (true) {
                sendMsg();
            }
        }
    }

    private class Receiver
            implements Runnable
    {
        private final BlockingQueue<ParaflowRecord> concurrentQueue;
        private final Stats stats;

        Receiver(BlockingQueue<ParaflowRecord> concurrentQueue)
        {
            this.concurrentQueue = concurrentQueue;
            this.stats = new Stats(1000);
        }

        @Override
        public void run()
        {
            while (true) {
                ParaflowRecord record;
                try {
                    record = concurrentQueue.take();
                    stats.record(200, (int) record.getKey());
                }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}