package customset;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Spliterator;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 3, jvmArgsAppend = {"-Xms2g", "-Xmx2g"})
public class CustomSetV3Benchmark {

    @Param({
            "10000", "20000", "30000", "40000", "50000",
            "60000", "70000", "80000", "90000", "100000"
    })
    public int size;

    private CustomSet<Integer> baseSet;
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

        int smallSize = Math.max(1, size / 10);
        smallInputCollection = new ArrayList<>(smallSize);
        for (int i = 0; i < smallSize; i++) {
            smallInputCollection.add(i);
        }

        int fifth = size / 5;
        toRemoveCollection = new ArrayList<>(fifth);
        toRetainCollection = new ArrayList<>(fifth);
        for (int i = 0; i < fifth; i++) {
            toRemoveCollection.add(i);
            toRetainCollection.add(i);
        }

        baseSet = new CustomSet<>();
        for (int i = 0; i < size; i++) {
            baseSet.add(i);
        }
    }

    // Construction
    @Benchmark
    public CustomSet<Integer> benchmarkConstructor() {
        return new CustomSet<>();
    }

    @Benchmark
    public CustomSet<Integer> benchmarkConstructorCapacity() {
        return new CustomSet<>(size);
    }

    @Benchmark
    public CustomSet<Integer> benchmarkConstructorCapacityLoadFactor() {
        return new CustomSet<>(size, 0.75f);
    }

    @Benchmark
    public CustomSet<Integer> benchmarkConstructorCollection() {
        return new CustomSet<>(inputCollection);
    }

    @Benchmark
    public CustomSet<Integer> benchmarkAdd() {
        CustomSet<Integer> s = new CustomSet<>();
        for (int j = 0; j < size; j++) s.add(j);
        return s;
    }

    @Benchmark
    public boolean benchmarkAddAll() {
        CustomSet<Integer> s = new CustomSet<>();
        return s.addAll(inputCollection);
    }

    // Read-only
    @Benchmark
    public CustomSet<Integer> benchmarkClone() {
        return baseSet.clone();
    }

    @Benchmark
    public boolean benchmarkContains() {
        return baseSet.contains(size - 1);
    }

    @Benchmark
    public boolean benchmarkContainsAll() {
        return baseSet.containsAll(smallInputCollection);
    }

    @Benchmark
    public boolean benchmarkEquals() {
        CustomSet<Integer> other = new CustomSet<>();
        for (int i = 0; i < size; i++) other.add(i);
        return baseSet.equals(other);
    }

    @Benchmark
    public int benchmarkHashCode() {
        return baseSet.hashCode();
    }

    @Benchmark
    public boolean benchmarkIsEmpty() {
        return baseSet.isEmpty();
    }

    @Benchmark
    public int benchmarkIterator() {
        int sum = 0;
        for (Integer item : baseSet) sum += item;
        return sum;
    }

    @Benchmark
    public int benchmarkSpliterator() {
        Spliterator<Integer> spliterator = baseSet.spliterator();
        final int[] sum = {0};
        spliterator.forEachRemaining(i -> sum[0] += i);
        return sum[0];
    }

    @Benchmark
    public int benchmarkSize() {
        return baseSet.size();
    }

    @Benchmark
    public Object[] benchmarkToArray() {
        return baseSet.toArray();
    }

    @Benchmark
    public Integer[] benchmarkToArrayT() {
        return baseSet.toArray(new Integer[0]);
    }

    @Benchmark
    public String benchmarkToString() {
        return baseSet.toString();
    }

    // Mutating (clone first)
    @Benchmark
    public boolean benchmarkRemove() {
        CustomSet<Integer> s = baseSet.clone();
        return s.remove(size - 1);
    }

    @Benchmark
    public boolean benchmarkRemoveAll() {
        CustomSet<Integer> s = baseSet.clone();
        return s.removeAll(toRemoveCollection);
    }

    @Benchmark
    public boolean benchmarkRetainAll() {
        CustomSet<Integer> s = baseSet.clone();
        return s.retainAll(toRetainCollection);
    }

    @Benchmark
    public void benchmarkClear(Blackhole bh) {
        CustomSet<Integer> s = baseSet.clone();
        s.clear();
        bh.consume(s);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(".*benchmarkConstructor.*") // Matches all constructor benchmarks
                .result("custom-set-v3-constructors.csv")
                .resultFormat(ResultFormatType.CSV)
                .build();
        new Runner(opt).run();
    }

    //started 17:58
    //ETA 2:45
    //finish ~20:43
}