package cn.edu.ruc.iir.paraflow.benchmark;

import cn.edu.ruc.iir.paraflow.benchmark.model.Customer;
import cn.edu.ruc.iir.paraflow.benchmark.model.LineOrder;
import cn.edu.ruc.iir.paraflow.benchmark.model.Nation;
import cn.edu.ruc.iir.paraflow.benchmark.model.Region;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * paraflow
 *
 * @author guodong
 */
public class TpchGenerationTest
{
    @Test
    public void testCustomerGeneration()
    {
        Iterable<Customer> customerGenerator = TpchTable.CUSTOMER.createGenerator(10000, 1, 1500, 0, 0);
        Iterator<Customer> customerIterator = customerGenerator.iterator();
        long start = System.currentTimeMillis();
        long counter = 0;
        long msgLen = 0;
        while (customerIterator.hasNext()) {
            Customer customer = customerIterator.next();
            msgLen += customer.toLine().length();
            counter++;
        }
        long end = System.currentTimeMillis();
        long duration = end - start;
        System.out.println("Generate " + counter + " messages in " + duration +
                           "ms (" + (1.0 * msgLen / duration) + " KB/s)");
    }

    @Test
    public void testGenerateCustomerFile()
    {
        Iterable<Customer> customerGenerator = TpchTable.CUSTOMER.createGenerator(10000, 1, 1500, 0, 0);
        Iterator<Customer> customerIterator = customerGenerator.iterator();
        File file = new File("/Users/Jelly/Developer/paraflow/paraflow-benchmark/data/customer.tbl");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            while (customerIterator.hasNext()) {
                writer.write(customerIterator.next().toLine());
                writer.newLine();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    // sf: 10000, num of msg: 39_996_464, size: 7.856GB
    @Test
    public void testLineOrderGeneration()
    {
        ExecutorService executorService = Executors.newCachedThreadPool();
        for (int i = 0; i < 4; i++) {
            LineOrderGenerationThread generationThread = new LineOrderGenerationThread();
            executorService.submit(generationThread);
        }
        try {
            executorService.awaitTermination(3600, TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private class LineOrderGenerationThread
            implements Runnable
    {
        @Override
        public void run()
        {
            Iterable<LineOrder> lineOrderIterable = TpchTable.LINEORDER.createGenerator(1000, 1, 1500, 0, 10000000);
            Iterator<LineOrder> lineOrderIterator = lineOrderIterable.iterator();
            long start = System.currentTimeMillis();
            long counter = 0;
            long msgLen = 0;
            while (lineOrderIterator.hasNext()) {
                LineOrder lineOrder = lineOrderIterator.next();
                msgLen += lineOrder.toLine().length();
                counter++;
            }
            long end = System.currentTimeMillis();
            long duration = end - start;
            System.out.println("Generate " + counter + " messages in " + duration +
                               "ms (" + (1.0 * msgLen / duration) + " KB/s)");
        }
    }

    @Test
    public void testGenerateNationFile()
    {
        Iterable<Nation> nationIterable = TpchTable.NATION.createGenerator(1, 1, 1500, 0, 0);
        Iterator<Nation> nationIterator = nationIterable.iterator();
        File file = new File("/Users/Jelly/Developer/paraflow/paraflow-benchmark/data/nation.sql");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            while (nationIterator.hasNext()) {
                writer.write(nationIterator.next().toLine());
                writer.newLine();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGenerateRegionFile()
    {
        Iterable<Region> regionIterable = TpchTable.REGION.createGenerator(1, 1, 1500, 0, 0);
        Iterator<Region> regionIterator = regionIterable.iterator();
        File file = new File("/Users/Jelly/Developer/paraflow/paraflow-benchmark/data/region.tbl");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            while (regionIterator.hasNext()) {
                writer.write(regionIterator.next().toLine());
                writer.newLine();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
