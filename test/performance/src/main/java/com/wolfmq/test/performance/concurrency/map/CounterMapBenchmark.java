package com.wolfmq.test.performance.concurrency.map;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.CommandLineOptions;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.CommandLineOptionException;

/**
 * compare counter update performance:
 *  - ConcurrentHashMap&lt;String, Long&gt;.merge(key, 1L, Long::sum)
 *  - HashMap&lt;String, AtomicLong&gt;.get(key).incrementAndGet()
 *
 * maps are initialized once with a fixed key set,
 * no keys are added or removed while measuring.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(value = 1, jvmArgsAppend = {"-Xms1g", "-Xmx1g"})
@State(Scope.Benchmark)
public class CounterMapBenchmark {

    @Param({"100", "500", "1000", "2000"})
    private int size;

    private ConcurrentHashMap<String, Long> concurrentMap;
    private HashMap<String, AtomicLong> atomicMap;

    @Setup(Level.Trial)
    public void setup() {
        concurrentMap = new ConcurrentHashMap<>();
        atomicMap = new HashMap<>();

        for (int i = 0; i < size; i++) {
            String key = "key-" + i;
            concurrentMap.put(key, 0L);
            atomicMap.put(key, new AtomicLong());
        }
    }

    private String randomKey() {
        return "key-" + ThreadLocalRandom.current().nextInt(size);
    }

    @Benchmark
    public long concurrentMerge() {
        return concurrentMap.merge(randomKey(), 1L, Long::sum);
    }

    /**
     * note: keys are fixed during measurement, so no resize happens
     * and concurrent reads of the HashMap buckets are de-facto safe.
     */
    @Benchmark
    public long atomicIncrement() {
        return atomicMap.get(randomKey()).incrementAndGet();
    }

    /**
     * args passthrough: mvn exec or java -cp ... CounterMapBenchmark -wi 3 -i 3 -t 4
     * with no args: run all benchmarks at 1/2/4/8/16 threads
     */
    public static void main(String[] args) throws RunnerException {
        try {
            if (args.length > 0) {
                new Runner(new CommandLineOptions(args)).run();
                return;
            }
        } catch (CommandLineOptionException e) {
            throw new IllegalArgumentException("invalid jmh options: " + String.join(" ", args), e);
        }

        for (int threads : new int[] {1, 2, 4, 8, 16}) {
            Options options = new OptionsBuilder()
                .include(CounterMapBenchmark.class.getSimpleName())
                .threads(threads)
                .shouldFailOnError(true)
                .build();

            new Runner(options).run();
        }
    }
}
