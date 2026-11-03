package customset;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unused")
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 2, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 3, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(2)
public class CustomSetV3Benchmark {

    @Param({"5000", "10000", "15000", "20000", "25000", "30000", "35000", "40000", "45000", "50000"})
    public int size;

    private CustomSet<Integer> set;
    private Collection<Integer> inputCollection;
    private Collection<Integer> smallInputCollection;
    private Collection<Integer> toRemoveCollection;
    private Collection<Integer> toRetainCollection;

    @Setup(Level.Trial)
    public void setupTrial() {
        inputCollection = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            inputCollection.add(i);
        }

        smallInputCollection = new ArrayList<>(Math.max(1, size / 10));
        for (int i = 0; i < Math.max(1, size / 10); i++) {
            smallInputCollection.add(i);
        }

        toRemoveCollection = new ArrayList<>(size / 5);
        for (int i = 0; i < size / 5; i++) {
            toRemoveCollection.add(i);
        }

        toRetainCollection = new ArrayList<>(size / 5);
        for (int i = 0; i < size / 5; i++) {
            toRetainCollection.add(i);
        }
    }

    @Setup(Level.Invocation)
    public void setupInvocation() {
        set = new CustomSet<>();
        for (int i = 0; i < size; i++) {
            set.add(i);
        }
    }

    @Benchmark
    public CustomSet<Integer> benchmarkConstructor() {
        return new CustomSet<>();
    }

    @Benchmark
    public CustomSet<Integer> benchmarkConstructorCollection() {
        return new CustomSet<>(inputCollection);
    }

    @Benchmark
    public CustomSet<Integer> benchmarkAdd() {
        CustomSet<Integer> s = new CustomSet<>();
        for (int j = 0; j < size; j++) {
            s.add(j);
        }
        return s;
    }

    @Benchmark
    public boolean benchmarkAddAll() {
        CustomSet<Integer> s = new CustomSet<>();
        return s.addAll(inputCollection);
    }

    @Benchmark
    public CustomSet<Integer> benchmarkClone() {
        return set.clone();
    }

    @Benchmark
    public boolean benchmarkContains() {
        return set.contains(size - 1);
    }

    @Benchmark
    public boolean benchmarkContainsAll() {
        return set.containsAll(smallInputCollection);
    }

    @Benchmark
    public boolean benchmarkEquals() {
        CustomSet<Integer> other = new CustomSet<>();
        for (int i = 0; i < size; i++) other.add(i);
        return set.equals(other);
    }

    @Benchmark
    public int benchmarkHashCode() {
        return set.hashCode();
    }

    @Benchmark
    public boolean benchmarkIsEmpty() {
        return set.isEmpty();
    }

    @Benchmark
    public int benchmarkIterator() {
        int sum = 0;
        for (Integer item : set) {
            sum += item;
        }
        return sum;
    }

    @Benchmark
    public boolean benchmarkRemove() {
        return set.remove(size - 1);
    }

    @Benchmark
    public boolean benchmarkRemoveAll() {
        return set.removeAll(toRemoveCollection);
    }

    @Benchmark
    public boolean benchmarkRetainAll() {
        return set.retainAll(toRetainCollection);
    }

    @Benchmark
    public int benchmarkSize() {
        return set.size();
    }

    @Benchmark
    public Object[] benchmarkToArray() {
        return set.toArray();
    }

    @Benchmark
    public Integer[] benchmarkToArrayT() {
        return set.toArray(new Integer[0]);
    }

    @Benchmark
    public String benchmarkToString() {
        return set.toString();
    }

    @Benchmark
    public void benchmarkClear() {
        set.clear();
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(CustomSetV3Benchmark.class.getSimpleName())
                .forks(1)
                .result("custom-set-v3-results.csv")
                .resultFormat(ResultFormatType.CSV)
                .build();

        new Runner(opt).run();
    }

    private static void writeCustomCsv(Collection<RunResult> results) {
        try (FileWriter writer = new FileWriter("CustomSetV1_jmh_performance.csv")) {
            writer.write("Benchmark;Size;Score (ns/op)\n");
            for (RunResult result : results) {
                String benchmarkName = result.getParams().getBenchmark();
                String shortName = benchmarkName.substring(benchmarkName.lastIndexOf('.') + 1);

                double score = result.getPrimaryResult().getScore();
                String sizeVal = result.getParams().getParam("size");

                writer.write("\"" + shortName + "\";" + (sizeVal != null ? sizeVal : "N/A") + ";" + score + "\n");
            }
            System.out.println("JMH Performance report saved: CustomSetV1_jmh_performance.csv");
        } catch (IOException e) {
            System.err.println("Failed to write CSV: " + e.getMessage());
        }
    }
}