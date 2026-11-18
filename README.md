## Custom Set V3

Implementation of a Set using a custom Red-Black tree. The standard JDK version is a wrapper of a JDK `HashMap` and 
therefore stores both key and value; this version is backed similarly, yet only requiring the value, minimizing memory 
overhead by 25%.

## Table of Contents

- [Build and Test](#build-and-test)
- [Performance Advantages & Architectural Optimizations](#performance-advantages--architectural-optimizations)
- [Time Complexity](#time-complexity)
- [Space Complexity](#space-complexity)
- [Performance](#performance)
    - [V3 vs JDK HashSet](#v3-vs-jdk-hashset)
    - [V3 vs V2](#v3-vs-v2)
    - [V3 vs V1](#v3-vs-v1)
- [Performance Testing](#performance-testing)
    - [Heat Maps](#heat-maps)
    - [Geometric](#geometric)
- [Grouped Graphs](#grouped-graphs)
- [Individual Graphs](#individual-graphs)

## Build and Test

1. To build and test the project run command `./gradlew clean build`
2. To test the project run command ` ./gradlew clean test`

## Performance Advantages & Architectural Optimizations

## Performance Advantages & Architectural Optimizations

## Performance Advantages & Architectural Optimizations

### 1. Direct Entry Storage vs. JDK Wrapper Overhead (`constructor`, `add`)
- **The JDK Bottleneck:** The standard JDK `HashSet` delegates storage internally to a `HashMap`. Every unique element
  inserted into a `HashSet` is wrapped inside a separate `HashMap.Node` object, which in turn points to a shared static
  dummy presentation value. This creates massive object allocation overhead, increases heap memory pressure, and
  introduces scattered memory references that cause CPU cache misses during traversal.

- **V3 Advantage:** `CustomSet` bypasses wrapper objects entirely by storing entry keys directly inside lightweight
  `Node` and `TreeNode` instances within the primary hash table array. This structural optimization slashes heap
  allocation overhead, improves memory locality, and accelerates wall-clock execution times across bulk operations.

### 2. Upfront Capacity Pre-Allocation & Sizing (`constructor(Collection c)`, `addAll`)
- **The JDK Bottleneck:** Constructing a JDK `HashSet` from an existing collection or repeatedly invoking `addAll` often
  incurs multiple intermediate rehashing steps and dynamic table resizing as the internal map grows past default
  thresholds.

- **V3 Advantage:** V3 constructors and bulk insertion methods calculate exact initial capacities upfront using
  `tableSizeFor` and load factor constraints (`(c.size() / DEFAULT_LOAD_FACTOR) + 1`). By sizing the backing array
  precisely before inserting any elements, V3 completely eliminates mid-construction resizing overhead.

### 3. Optimized Set Intersection Operations (`retainAll`)
- **The JDK Bottleneck:** In standard JDK implementations, `retainAll(Collection)` iterates over the set and performs
  repetitive, sequential containment checks against the target collection, resulting in a sluggish $O(N \cdot M)$
  worst-case execution profile for non-set collections.

- **V3 Advantage:** V3 optimizes non-set collections by converting them upfront into a temporary `CustomSet`
  (`final Collection<?> retain = (c instanceof CustomSet) ? c : new CustomSet<Object>(c);`). This drops the algorithmic time
  complexity from $O(N \cdot M)$ down to an $O(N + M)$ lookup blitz, yielding massive performance gains (exceeding
  **501× speedups** in benchmark evaluations).

### 4. Zero-Allocation Iterator Traversal (`iterator`, `spliterator`)
- **The JDK Bottleneck:** Iterators over standard collection wrappers frequently instantiate cursor tracking objects or
  rely on generic map iterators that introduce garbage collection churn during large-scale sequential traversal.

- **V3 Advantage:** V3's custom `HashIterator` performs zero-allocation pointer traversals across standard singly-linked
  collision chains, caching localized key snapshots only when stepping into complex Red-Black tree bins. This minimizes
  object allocation churn and keeps iteration streams fast and efficient.

### 5. Power-of-Two Bitwise Splitting in Resize Operations (`resize`, `transferBins`)
- **The JDK Bottleneck:** General-purpose table resizing requires re-evaluating hash codes and recalculating bucket
  indices for every stored element from scratch.

- **V3 Advantage:** When doubling table capacity (`newCapacity == oldCapacity << 1`), V3 bypasses full re-indexing
  arithmetic entirely, utilizing the bitwise check `(entry.hash & oldCapacity)` to partition collision chains cleanly into
  low and high bins in-place without re-computing hash codes or allocating new wrapper nodes.

### 6. Adaptive Removal Heuristic (`removeAll`)
- **The JDK Bottleneck:** Standard JDK implementations of `removeAll(Collection)` blindly iterate over the entire
  collection or set without regard to relative sizes, often leading to redundant scans when removing a small collection
  from a massive set (or vice versa).

- **V3 Advantage:** `CustomSet` dynamically evaluates set cardinalities (`if (size > c.size())`) to choose the optimal
  removal strategy: either targeting individual collection elements directly for rapid deletion or running a single-pass
  iterator scan. This adaptive branching ensures optimal time complexity regardless of relative collection sizes.

### 7. Deterministic Hash-Bucket Null Handling (`hash`, `add`)
- **The JDK Bottleneck:** Some set or map implementations require special conditional branches or dedicated sentinel wrappers to process `null` keys safely without throwing `NullPointerException`.

- **V3 Advantage:** V3 evaluates `null` keys via a deterministic hash code of `0` (`(key == null) ? 0 : ...`), routing `null` values directly into bucket `0` alongside standard collision chains and Red-Black trees. This avoids redundant conditional branching overhead during standard lookups while maintaining full `null` compatibility.

### 8. Adaptive Table Sizing & Bitwise Masking (`tableSizeFor`, `hash`)
- **The JDK Bottleneck:** Inefficient table sizing calculations or modulo-based index arithmetic can lead to suboptimal
  memory packing and expensive division CPU instructions during lookups.

- **V3 Advantage:** V3 enforces exact power-of-two table capacities via bit-manipulation in `tableSizeFor`, enabling
  table index lookups to execute via lightning-fast bitwise AND operations (`hash & (table.length - 1)`). Furthermore,
  secondary bit-mixing (`h ^ (h >>> HASH_BITS_TO_SHIFT)`) spreads high bits into lower bits to minimize collisions even with poorly
  implemented user key hash codes.

### 9. Stream-Ready Parallel Spliterators (`spliterator`)
- **The JDK Bottleneck:** Standard or poorly optimized set wrappers often provide naive spliterators that lack efficient
  range splitting or proper structural safety checks during parallel stream processing.

- **V3 Advantage:** V3 implements a specialized `CustomSetSpliterator` that supports fail-fast concurrency checks
  (`ConcurrentModificationException`), late-binding table initialization, and binary tree/bucket range splitting
  (`trySplit()`). This allows the set to integrate seamlessly into modern Java parallel streams and functional pipelines
  with optimal work-stealing efficiency.

### 10. Custom Packed Serialization (`writeObject`, `readObject`)
- **The JDK Bottleneck:** Default Java serialization for complex collection wrappers often serializes redundant metadata
  or incurs heavy reflection overhead, resulting in bloated byte streams and slow deserialization times.

- **V3 Advantage:** V3 implements custom `writeObject` and `readObject` routines that pack only the essential capacity
  and active mapping entries directly into the serialization stream, reconstructing the hash table and re-establishing
  table thresholds efficiently upon deserialization without redundant object overhead.

### 11. Pre-Sized StringBuilder Buffer Allocation (`toString`)
- **The JDK Bottleneck:** Default collection `toString()` implementations often rely on dynamic string concatenation or
  iterative buffer resizing as elements are appended, triggering continuous memory reallocation and array copying.

- **V3 Advantage:** V3's `toString()` calculates an initial capacity heuristically
  (`(int) Math.min(1 << 20, Math.max(16L, size * 8L))`) based on active set size before instantiating the `StringBuilder`.
  This pre-allocation entirely eliminates buffer reallocation churn during string serialization.

## Trade-Offs & Architectural Limitations

While `CustomSetV3` delivers significant performance and memory improvements over the standard JDK implementation, these 
gains come with specific design trade-offs:

### 1. The `retainAll` Temporary Heap Allocation Trap
- **The Bottleneck:** When calling `retainAll(Collection c)` where the incoming collection is not already a `Set`, V3 
instantiates a temporary `CustomSet` on the heap 
(`final Collection<?> retainValues = (c instanceof Set) ? c : new CustomSet<>(c);`).

- **The Trade-Off:** While this drops algorithmic time complexity from $O(N \cdot M)$ down to $O(N + M)$, it forces an 
immediate $O(M)$ memory allocation and generates transient garbage collection (GC) pressure. Frequent calls to 
`retainAll` on large non-set collections can thrash the 
[Eden space (Oracle JVM Heap Architecture)](https://docs.oracle.com/cd/E19146-01/820-7979/ggkge/index.html).

### 2. `TreeNode` Memory Bloat Under Adversarial Workloads
- **The Bottleneck:** To protect against worst-case $O(N)$ hash-collision degradation, heavy collision bins 
automatically treeify into Red-Black trees (`TreeNode`).

- **The Trade-Off:** A `TreeNode` carries significantly more memory overhead than a standard `Node` because it maintains 
parent, left, right, previous, and color pointers. Under a malicious hash-flooding attack or high-collision workload, 
per-entry memory footprints balloon, negating the memory-saving benefits of avoiding JDK's `HashMap.Node` wrapper.

### 3. Maintenance Risk in Manual `clone()` Logic
- **The Bottleneck:** The `clone()` method is implemented manually by walking the source table, bifurcating logic 
between standard nodes and tree nodes, rebuilding linked lists via tail pointers, and explicitly re-invoking 
`.treeify(dst)` on cloned tree bins.

- **The Trade-Off:** This presents a long-term maintenance liability. Any future refactoring of node linkage rules, 
parent pointers, or balancing invariants requires synchronized, manual updates inside `clone()`. Bypassing cleaner 
factory or serialization cloning patterns leaves a potential vector for silent pointer corruption if an edge-case link 
is missed.

### 4. Reactive, Non-Thread-Safe Fail-Fast (`modCount`)
- **The Bottleneck:** Like standard JDK collections, V3 relies on `modCount` to trigger a 
`ConcurrentModificationException` when structural mutations occur during iteration.

- **The Trade-Off:** This mechanism is strictly reactive rather than proactive or thread-safe. Without external 
synchronization, concurrent multi-threaded access will corrupt non-atomic pointer updates (`size++`, table resizing) 
long before a `ConcurrentModificationException` is thrown. Developers sharing sets across threads without external 
locking will encounter silent data corruption rather than safe thread isolation.

### 5. Reliance on Third-Party Set Contracts in `equals()`
- **The Bottleneck:** The `equals(Object o)` implementation verifies size equality and delegates containment checking 
via `containsAll(other)`.

- **The Trade-Off:** The implementation blindly trusts the contract of any arbitrary third-party `Set` implementation 
passed in. If `other` is a poorly written custom collection whose `size()` lies or whose `contains()` method introduces 
side effects or non-deterministic behavior, the set's entire equality contract breaks down.

Version 1 being an array back version can be found [here](https://github.com/bk10aao/CustomSet).

Version 2 being back by a Java HashMap can be found [here](https://github.com/bk10aao/CustomSetV2).

All methods implemented are identical to those found in the Java 
[Set](https://docs.oracle.com/javase/8/docs/api/java/util/Set.html) interface.

## Time Complexity

| Operation                       |                             V3 Complexity                             |                            JDK Complexity                             | Winner  |
|:--------------------------------|:---------------------------------------------------------------------:|:---------------------------------------------------------------------:|:-------:|
| **`constructor()`**             |                                $O(1)$                                 |                                $O(1)$                                 | **Tie** |
| **`constructor(capacity)`**     |                                $O(1)$                                 |                                $O(1)$                                 | **Tie** |
| **`constructor(Collection c)`** |                        $O(N)$ / $O(N \log N)$                         |                        $O(N)$ / $O(N \log N)$                         | **Tie** |
| **`add(E e)`**                  |             **$O(1)$** (Amortized)<br>$O(\log N)$ (Worst)             |             **$O(1)$** (Amortized)<br>$O(\log N)$ (Worst)             | **Tie** |
| **`addAll(Collection c)`**      |                        $O(M)$ / $O(M \log N)$                         |                        $O(M)$ / $O(M \log N)$                         | **Tie** |
| **`clone()`**                   |                              $O(N + C)$                               |                              $O(N + C)$                               | **Tie** |
| **`contains(Object o)`**        |                **$O(1)$** (Avg)<br>$O(\log N)$ (Worst)                |                **$O(1)$** (Avg)<br>$O(\log N)$ (Worst)                | **Tie** |
| **`clear()`**                   |                                $O(C)$                                 |                                $O(C)$                                 | **Tie** |
| **`containsAll(Collection c)`** |                        $O(M)$ / $O(M \log N)$                         |                        $O(M)$ / $O(M \log N)$                         | **Tie** |
| **`equals(Object o)`**          |                        $O(N)$ / $O(N \log N)$                         |                        $O(N)$ / $O(N \log N)$                         | **Tie** |
| **`hashCode()`**                |                              $O(N + C)$                               |                              $O(N + C)$                               | **Tie** |
| **`isEmpty()`**                 |                                $O(1)$                                 |                                $O(1)$                                 | **Tie** |
| **`iterator()`**                |   $O(1)$ (Avg Init) / $O(C)$ (Worst Init)<br>$O(N + C)$ (Traversal)   |   $O(1)$ (Avg Init) / $O(C)$ (Worst Init)<br>$O(N + C)$ (Traversal)   | **Tie** |
| **`remove(Object o)`**          |                **$O(1)$** (Avg)<br>$O(\log N)$ (Worst)                |                **$O(1)$** (Avg)<br>$O(\log N)$ (Worst)                | **Tie** |
| **`removeAll(Collection c)`**   |             $O(\min(M \log N, N \cdot \text{contains}))$              |             $O(\min(M \log N, N \cdot \text{contains}))$              | **Tie** |
| **`retainAll(Collection c)`**   | **$O(M + N)$** <br>*(Optimized via temporary `CustomSet` conversion)* | **$O(N \cdot M)$** <br>*(Iterative `contains` scans over collection)* | **V3**  |
| **`size()`**                    |                                $O(1)$                                 |                                $O(1)$                                 | **Tie** |
| **`toArray()`**                 |                              $O(N + C)$                               |                              $O(N + C)$                               | **Tie** |
| **`toArray(T[])`**              |                              $O(N + C)$                               |                              $O(N + C)$                               | **Tie** |
| **`toString()`**                |                              $O(N + C)$                               |                              $O(N + C)$                               | **Tie** |

## Space Complexity

| Operation                       |                         V3 Space Complexity                         |           JDK Space Complexity            |                Winner                 |
|:--------------------------------|:-------------------------------------------------------------------:|:-----------------------------------------:|:-------------------------------------:|
| **`constructor()`**             |                               $O(1)$                                |                  $O(1)$                   |                **Tie**                |
| **`constructor(capacity)`**     |                      $O(1)$ (Lazy allocation)                       |         $O(1)$ (Lazy allocation)          |                **Tie**                |
| **`constructor(Collection c)`** |                   **$O(N + C)$** (Direct entries)                   | **$O(N + C + \text{Node obj overhead})$** |                **V3**                 |
| **`add(E e)`**                  |                         $O(1)$ (Amortized)                          |            $O(1)$ (Amortized)             |                **Tie**                |
| **`addAll(Collection c)`**      |                       $O(M + C_{\text{new}})$                       |          $O(M + C_{\text{new}})$          |                **Tie**                |
| **`clone()`**                   |                      $O(N + C)$ (Shallow copy)                      |         $O(N + C)$ (Shallow copy)         |                **Tie**                |
| **`contains(Object o)`**        |                               $O(1)$                                |                  $O(1)$                   |                **Tie**                |
| **`clear()`**                   |                    $O(1)$ (In-place array reset)                    |       $O(1)$ (In-place array reset)       |                **Tie**                |
| **`containsAll(Collection c)`** |                               $O(1)$                                |                  $O(1)$                   |                **Tie**                |
| **`equals(Object o)`**          |                               $O(1)$                                |                  $O(1)$                   |                **Tie**                |
| **`hashCode()`**                |                               $O(1)$                                |                  $O(1)$                   |                **Tie**                |
| **`isEmpty()`**                 |                               $O(1)$                                |                  $O(1)$                   |                **Tie**                |
| **`iterator()`**                |  $O(1)$ *(plus $O(\text{bin size})$ local snapshot for tree bins)*  |                  $O(1)$                   |                **Tie**                |
| **`remove(Object o)`**          |                               $O(1)$                                |                  $O(1)$                   |                **Tie**                |
| **`removeAll(Collection c)`**   |                               $O(1)$                                |                  $O(1)$                   |                **Tie**                |
| **`retainAll(Collection c)`**   | **$O(M)$** *(Temporary `CustomSet` allocation if $c$ is not a Set)* |      **$O(1)$** *(Iterative scans)*       | **JDK** *(less auxiliary allocation)* |
| **`size()`**                    |                               $O(1)$                                |                  $O(1)$                   |                **Tie**                |
| **`toArray()`**                 |                      $O(N)$ (New array output)                      |         $O(N)$ (New array output)         |                **Tie**                |
| **`toArray(T[])`**              |               $O(N)$ (New array output if too small)                |  $O(N)$ (New array output if too small)   |                **Tie**                |
| **`toString()`**                |                       $O(N)$ (String buffer)                        |          $O(N)$ (String buffer)           |                **Tie**                |

### Asymptotic Variables & Parameters

- **$N$**: The number of elements currently stored in the set (i.e., `size()`).
- **$M$**: The number of elements contained in the target collection passed as an argument to bulk operations (e.g., `addAll`, `containsAll`, `removeAll`).
- **$C$**: The current capacity (size) of the internal hash table backing array (the number of buckets).

## Performance

### V3 vs JDK HashSet

| Method                         |     V3     |  JDK HashSet   |            Winner            | Speedup Margin |
|:-------------------------------|:----------:|:--------------:|:----------------------------:|:--------------:|
| **`add(E)`**                   | 494,851 ns |   668,344 ns   |            **V3**            |     1.35×      |
| **`addAll(Collection)`**       | 343,238 ns |   628,770 ns   |            **V3**            |     1.83×      |
| **`clear()`**                  | 154,275 ns |   398,639 ns   |            **V3**            |     2.58×      |
| **`clone()`**                  | 271,763 ns |   554,600 ns   |            **V3**            |     2.04×      |
| **`constructor()`**            |  1.51 ns   |    3.01 ns     |            **V3**            |     2.00×      |
| **`constructor(Collection)`**  | 337,047 ns |   525,952 ns   |            **V3**            |     1.56×      |
| **`constructor(int)`**         |  1.64 ns   |    3.15 ns     |            **V3**            |     1.92×      |
| **`constructor(int, double)`** |  1.64 ns   |    3.15 ns     |            **V3**            |     1.93×      |
| **`contains(Object)`**         |  3.79 ns   |    2.22 ns     |           **JDK**            |     1.71×      |
| **`containsAll(Collection)`**  | 19,229 ns  |   21,641 ns    |            **V3**            |     1.13×      |
| **`equals(Object)`**           | 776,034 ns |  1,001,732 ns  |            **V3**            |     1.29×      |
| **`hashCode()`**               | 78,284 ns  |   170,285 ns   |            **V3**            |     2.18×      |
| **`isEmpty()`**                |  0.53 ns   |    0.64 ns     |            **V3**            |     1.20×      |
| **`iterator()`**               | 105,462 ns |   170,308 ns   |            **V3**            |     1.61×      |
| **`remove(Object)`**           | 270,351 ns |   532,244 ns   |            **V3**            |     1.97×      |
| **`removeAll(Collection)`**    | 307,264 ns |   577,449 ns   |            **V3**            |     1.88×      |
| **`retainAll(Collection)`**    | 362,631 ns | 181,812,065 ns |            **V3**            |  **501.37×**   |
| **`size()`**                   |  0.53 ns   |    0.64 ns     |            **V3**            |     1.19×      |
| **`spliterator()`**            | 93,602 ns  |   105,410 ns   |            **V3**            |     1.13×      |
| **`toArray()`**                | 99,550 ns  |   100,299 ns   | **Statistically Equivalent** |     ~1.00×     |
| **`toArray(T[])`**             | 109,326 ns |   112,000 ns   | **Statistically Equivalent** |     ~1.00×     |
| **`toString()`**               | 592,685 ns |   754,131 ns   |            **V3**            |     1.27×      |


### V3 vs V2

| Method                         |     V3     |      V2       |            Winner            |     Margin     |
|:-------------------------------|:----------:|:-------------:|:----------------------------:|:--------------:|
| **`add(E)`**                   | 494,851 ns |  661,381 ns   |            **V3**            |     1.34×      |
| **`addAll(Collection)`**       | 343,238 ns |  615,153 ns   |            **V3**            |     1.79×      |
| **`clear()`**                  | 154,275 ns |    2.07 ns    |            **V2**            | **74,516.85×** |
| **`clone()`**                  | 271,763 ns |  314,208 ns   |            **V3**            |     1.16×      |
| **`constructor()`**            |  1.51 ns   |    3.02 ns    |            **V3**            |     2.00×      |
| **`constructor(Collection)`**  | 337,047 ns |  526,958 ns   |            **V3**            |     1.56×      |
| **`constructor(int)`**         |  1.64 ns   |    3.16 ns    |            **V3**            |     1.92×      |
| **`constructor(int, double)`** |  1.64 ns   |    3.21 ns    |            **V3**            |     1.96×      |
| **`contains(Object)`**         |  3.79 ns   |    2.39 ns    |            **V2**            |     1.58×      |
| **`containsAll(Collection)`**  | 19,229 ns  |   22,287 ns   |            **V3**            |     1.16×      |
| **`equals(Object)`**           | 776,034 ns | 1,029,691 ns  |            **V3**            |     1.33×      |
| **`hashCode()`**               | 78,284 ns  |   87,857 ns   |            **V3**            |     1.12×      |
| **`isEmpty()`**                |  0.53 ns   |    0.74 ns    |            **V3**            |     1.39×      |
| **`iterator()`**               | 105,462 ns |  170,913 ns   |            **V3**            |     1.62×      |
| **`remove(Object)`**           | 270,351 ns |  315,326 ns   |            **V3**            |     1.17×      |
| **`removeAll(Collection)`**    | 307,264 ns |  289,537 ns   |            **V2**            |     1.06×      |
| **`retainAll(Collection)`**    | 362,631 ns | 20,244,102 ns |            **V3**            |   **55.83×**   |
| **`size()`**                   |  0.53 ns   |    0.74 ns    |            **V3**            |     1.39×      |
| **`spliterator()`**            | 93,602 ns  |  106,394 ns   |            **V3**            |     1.14×      |
| **`toArray()`**                | 99,550 ns  |  105,028 ns   |            **V3**            |     1.06×      |
| **`toArray(T[])`**             | 109,326 ns |  109,054 ns   | **Statistically Equivalent** |     ~1.00×     |
| **`toString()`**               | 592,685 ns |  873,254 ns   |            **V3**            |     1.47×      |


### V3 vs V1

| Method                         |     V3     |       V1       |            Winner            |    Margin     |
|:-------------------------------|:----------:|:--------------:|:----------------------------:|:-------------:|
| **`add(E)`**                   | 494,851 ns |  1,952,206 ns  |            **V3**            |     3.95×     |
| **`addAll(Collection)`**       | 343,238 ns |  2,048,502 ns  |            **V3**            |     5.97×     |
| **`clear()`**                  | 154,275 ns |   495,582 ns   |            **V3**            |     3.21×     |
| **`clone()`**                  | 271,763 ns |   496,985 ns   |            **V3**            |     1.83×     |
| **`constructor()`**            |  1.51 ns   |    4.50 ns     |            **V3**            |     2.99×     |
| **`constructor(Collection)`**  | 337,047 ns |   291,602 ns   |            **V1**            |     1.16×     |
| **`constructor(int)`**         |  1.64 ns   |    2,446 ns    |            **V3**            | **1,488.90×** |
| **`constructor(int, double)`** |  1.64 ns   |    2,445 ns    |            **V3**            | **1,493.47×** |
| **`contains(Object)`**         |  3.79 ns   |    2.12 ns     |            **V1**            |     1.79×     |
| **`containsAll(Collection)`**  | 19,229 ns  |    8,456 ns    |            **V1**            |     2.27×     |
| **`equals(Object)`**           | 776,034 ns |  2,376,269 ns  |            **V3**            |     3.06×     |
| **`hashCode()`**               | 78,284 ns  |   109,964 ns   |            **V3**            |     1.40×     |
| **`isEmpty()`**                |  0.53 ns   |    0.54 ns     | **Statistically Equivalent** |    ~1.00×     |
| **`iterator()`**               | 105,462 ns |   238,021 ns   |            **V3**            |     2.26×     |
| **`remove(Object)`**           | 270,351 ns |   509,098 ns   |            **V3**            |     1.88×     |
| **`removeAll(Collection)`**    | 307,264 ns |   549,852 ns   |            **V3**            |     1.79×     |
| **`retainAll(Collection)`**    | 362,631 ns | 183,425,778 ns |            **V3**            |  **505.82×**  |
| **`size()`**                   |  0.53 ns   |    0.54 ns     | **Statistically Equivalent** |    ~1.00×     |
| **`spliterator()`**            | 93,602 ns  |   125,419 ns   |            **V3**            |     1.34×     |
| **`toArray()`**                | 99,550 ns  |   138,589 ns   |            **V3**            |     1.39×     |
| **`toArray(T[])`**             | 109,326 ns |   145,863 ns   |            **V3**            |     1.33×     |
| **`toString()`**               | 592,685 ns |   794,851 ns   |            **V3**            |     1.34×     |

## Performance Testing

<b>Note: The following performance charts are designed to be viewed in dark mode.</b>

### Heat Maps
![Heatmap](PerformanceTesting/V3_JDK/heatmap.png)
![Heatmap](PerformanceTesting/V2_V3/heatmap.png)
![Heatmap](PerformanceTesting/V1_V3/heatmap.png)

### Geometric
![Geometric](PerformanceTesting/V3_JDK/geometric.png)
![Geometric](PerformanceTesting/V2_V3/geometric.png)
![Geometric](PerformanceTesting/V1_V3/geometric.png)


## Grouped Graphs

### V1 vs V2 vs V3 vs JDK
![Combined Performance Charts](PerformanceTesting/V1_V2_V3_JDK/grid.png)

### V3 vs JDK
![Combined Performance Charts](PerformanceTesting/V3_JDK/grid.png)

### V3 vs V2
![Combined Performance Charts](PerformanceTesting/V2_V3/grid.png)

### V3 vs V1
![Combined Performance Charts](PerformanceTesting/V1_V3/grid.png)

## Individual Graphs
### V1 vs V2 vs V3 vs JDK Performance
![Combined Performance Charts](PerformanceTesting/V1_V2_V3_JDK/constructor.png)
![Combined Performance Charts](PerformanceTesting/V1_V2_V3_JDK/constructorCapacity.png)
![Combined Performance Charts](PerformanceTesting/V1_V2_V3_JDK/constructorCapacityLoadFactor.png)
![Combined Performance Charts](PerformanceTesting/V1_V2_V3_JDK/constructorCollection.png)
![Combined Performance Charts](PerformanceTesting/V1_V2_V3_JDK/add.png)
![Combined Performance Charts](PerformanceTesting/V1_V2_V3_JDK/addAll.png)
![Combined Performance Charts](PerformanceTesting/V1_V2_V3_JDK/clear.png)
![Combined Performance Charts](PerformanceTesting/V1_V2_V3_JDK/clone.png)
![Combined Performance Charts](PerformanceTesting/V1_V2_V3_JDK/contains.png)
![Combined Performance Charts](PerformanceTesting/V1_V2_V3_JDK/containsAll.png)
![Combined Performance Charts](PerformanceTesting/V1_V2_V3_JDK/equals.png)
![Combined Performance Charts](PerformanceTesting/V1_V2_V3_JDK/hashCode.png)
![Combined Performance Charts](PerformanceTesting/V1_V2_V3_JDK/isEmpty.png)
![Combined Performance Charts](PerformanceTesting/V1_V2_V3_JDK/iterator.png)
![Combined Performance Charts](PerformanceTesting/V1_V2_V3_JDK/remove.png)
![Combined Performance Charts](PerformanceTesting/V1_V2_V3_JDK/removeAll.png)
![Combined Performance Charts](PerformanceTesting/V1_V2_V3_JDK/retainAll.png)
![Combined Performance Charts](PerformanceTesting/V1_V2_V3_JDK/size.png)
![Combined Performance Charts](PerformanceTesting/V1_V2_V3_JDK/spliterator.png)
![Combined Performance Charts](PerformanceTesting/V1_V2_V3_JDK/toArray.png)
![Combined Performance Charts](PerformanceTesting/V1_V2_V3_JDK/toArrayT.png)
![Combined Performance Charts](PerformanceTesting/V1_V2_V3_JDK/toString.png)

### V3 vs JDK Performance
![Combined Performance Charts](PerformanceTesting/V3_JDK/constructor.png)
![Combined Performance Charts](PerformanceTesting/V3_JDK/constructorCapacity.png)
![Combined Performance Charts](PerformanceTesting/V3_JDK/constructorCapacityLoadFactor.png)
![Combined Performance Charts](PerformanceTesting/V3_JDK/constructorCollection.png)
![Combined Performance Charts](PerformanceTesting/V3_JDK/add.png)
![Combined Performance Charts](PerformanceTesting/V3_JDK/addAll.png)
![Combined Performance Charts](PerformanceTesting/V3_JDK/clear.png)
![Combined Performance Charts](PerformanceTesting/V3_JDK/clone.png)
![Combined Performance Charts](PerformanceTesting/V3_JDK/contains.png)
![Combined Performance Charts](PerformanceTesting/V3_JDK/containsAll.png)
![Combined Performance Charts](PerformanceTesting/V3_JDK/equals.png)
![Combined Performance Charts](PerformanceTesting/V3_JDK/hashCode.png)
![Combined Performance Charts](PerformanceTesting/V3_JDK/isEmpty.png)
![Combined Performance Charts](PerformanceTesting/V3_JDK/iterator.png)
![Combined Performance Charts](PerformanceTesting/V3_JDK/remove.png)
![Combined Performance Charts](PerformanceTesting/V3_JDK/removeAll.png)
![Combined Performance Charts](PerformanceTesting/V3_JDK/retainAll.png)
![Combined Performance Charts](PerformanceTesting/V3_JDK/size.png)
![Combined Performance Charts](PerformanceTesting/V3_JDK/spliterator.png)
![Combined Performance Charts](PerformanceTesting/V3_JDK/toArray.png)
![Combined Performance Charts](PerformanceTesting/V3_JDK/toArrayT.png)
![Combined Performance Charts](PerformanceTesting/V3_JDK/toString.png)

### V3 vs V2 Performance
![Combined Performance Charts](PerformanceTesting/V2_V3/constructor.png)
![Combined Performance Charts](PerformanceTesting/V2_V3/constructorCapacity.png)
![Combined Performance Charts](PerformanceTesting/V2_V3/constructorCapacityLoadFactor.png)
![Combined Performance Charts](PerformanceTesting/V2_V3/constructorCollection.png)
![Combined Performance Charts](PerformanceTesting/V2_V3/add.png)
![Combined Performance Charts](PerformanceTesting/V2_V3/addAll.png)
![Combined Performance Charts](PerformanceTesting/V2_V3/clear.png)
![Combined Performance Charts](PerformanceTesting/V2_V3/clone.png)
![Combined Performance Charts](PerformanceTesting/V2_V3/contains.png)
![Combined Performance Charts](PerformanceTesting/V2_V3/containsAll.png)
![Combined Performance Charts](PerformanceTesting/V2_V3/equals.png)
![Combined Performance Charts](PerformanceTesting/V2_V3/hashCode.png)
![Combined Performance Charts](PerformanceTesting/V2_V3/isEmpty.png)
![Combined Performance Charts](PerformanceTesting/V2_V3/iterator.png)
![Combined Performance Charts](PerformanceTesting/V2_V3/remove.png)
![Combined Performance Charts](PerformanceTesting/V2_V3/removeAll.png)
![Combined Performance Charts](PerformanceTesting/V2_V3/retainAll.png)
![Combined Performance Charts](PerformanceTesting/V2_V3/size.png)
![Combined Performance Charts](PerformanceTesting/V2_V3/spliterator.png)
![Combined Performance Charts](PerformanceTesting/V2_V3/toArray.png)
![Combined Performance Charts](PerformanceTesting/V2_V3/toArrayT.png)
![Combined Performance Charts](PerformanceTesting/V2_V3/toString.png)

### V3 vs V1 Performance
![Combined Performance Charts](PerformanceTesting/V1_V3/constructor.png)
![Combined Performance Charts](PerformanceTesting/V1_V3/constructorCapacity.png)
![Combined Performance Charts](PerformanceTesting/V1_V3/constructorCapacityLoadFactor.png)
![Combined Performance Charts](PerformanceTesting/V1_V3/constructorCollection.png)
![Combined Performance Charts](PerformanceTesting/V1_V3/add.png)
![Combined Performance Charts](PerformanceTesting/V1_V3/addAll.png)
![Combined Performance Charts](PerformanceTesting/V1_V3/clear.png)
![Combined Performance Charts](PerformanceTesting/V1_V3/clone.png)
![Combined Performance Charts](PerformanceTesting/V1_V3/contains.png)
![Combined Performance Charts](PerformanceTesting/V1_V3/containsAll.png)
![Combined Performance Charts](PerformanceTesting/V1_V3/equals.png)
![Combined Performance Charts](PerformanceTesting/V1_V3/hashCode.png)
![Combined Performance Charts](PerformanceTesting/V1_V3/isEmpty.png)
![Combined Performance Charts](PerformanceTesting/V1_V3/iterator.png)
![Combined Performance Charts](PerformanceTesting/V1_V3/remove.png)
![Combined Performance Charts](PerformanceTesting/V1_V3/removeAll.png)
![Combined Performance Charts](PerformanceTesting/V1_V3/retainAll.png)
![Combined Performance Charts](PerformanceTesting/V1_V3/size.png)
![Combined Performance Charts](PerformanceTesting/V1_V3/spliterator.png)
![Combined Performance Charts](PerformanceTesting/V1_V3/toArray.png)
![Combined Performance Charts](PerformanceTesting/V1_V3/toArrayT.png)
![Combined Performance Charts](PerformanceTesting/V1_V3/toString.png)