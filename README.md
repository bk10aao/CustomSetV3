## Custom Set V3

Implementation of a Set using a custom Red-Black tree. The standard JDK version is a wrapper of a JDK `HashMap` and
therefore stores both key and value; this version is backed similarly, yet only requiring the key, minimizing memory
overhead by 25%.

## Table of Contents

- [Build and Test](#build-and-test)
- [Performance Advantages & Architectural Optimizations](#performance-advantages--architectural-optimizations)
- [Trade-Offs & Architectural Limitations](#trade-offs--architectural-limitations)
- [Related Versions](#related-versions)
- [Time Complexity](#time-complexity)
- [Space Complexity](#space-complexity)
- [Performance](#performance)
  - [V3 vs JDK HashSet](#v3-vs-jdk-hashset)
  - [V3 vs V2](#v3-vs-v2)
  - [V3 vs V1](#v3-vs-v1)
- [Performance Testing](#performance-testing)
  - [Heat Maps](#heat-maps)
  - [Geometric Mean Comparisons](#geometric-mean-comparisons)
- [Grouped Graphs](#grouped-graphs)
- [Individual Graphs](#individual-graphs)

## Build and Test

1. To build and test the project run command `./gradlew clean build`
2. To test the project run command `./gradlew clean test`

## Performance Advantages & Architectural Optimizations

CustomSetV3 is a memory-conscious, HashMap-style set that keeps the same asymptotic behaviour as JDK `HashSet` while eliminating the dummy value field and adding proper $O(N + M)$ bulk operations against non-set collections for `retainAll`.

### 1. Leaner Entry Storage
JDK `HashSet` is a thin wrapper over `HashMap`. Every element is stored in a `HashMap.Node` that also holds a dummy value reference (`PRESENT`).  
`CustomSet` stores only the key (plus hash and next/tree pointers). Under compressed oops this is typically ~8 bytes less per entry in ordinary (non-tree) bins — roughly a 20-25 % reduction in per-element memory. The extra indirection and the static dummy object are also avoided.

### 2. Pre-sizing on `addAll` / collection constructors
`HashSet(Collection)` already pre-sizes, but `HashSet.addAll` does not.  
`CustomSet` estimates required capacity up-front via `tableSizeFor` and load-factor scaling (clamping to `MAXIMUM_CAPACITY` and `DEFAULT_INITIAL_CAPACITY` in the collection constructor), reducing intermediate resizes during bulk insertion.

### 3. Optimised `retainAll` on non-`CustomSet` arguments
`retainAll` converts any argument that is not already a `CustomSet` into a temporary `CustomSet`, turning an $O(N \cdot M)$ nested scan into an $O(N + M)$ lookup pass.  
Against large lists the measured speedup exceeds 500×. When the argument is already a `CustomSet`, the temporary allocation is skipped.  
(`removeAll` keeps the classic size-based heuristic and does **not** perform this conversion.)

### 4. Single-pass table walk for bulk removal
`removeMatching` walks the hash table once and unlinks nodes in place. When used by `retainAll` (after the conversion above) it avoids the classic “iterator + repeated contains” pattern.

### 5. Proven HashMap techniques (parity, not advantage)
The following are intentional mirrors of modern OpenJDK `HashMap` and therefore deliver the same asymptotic behaviour:

- Power-of-two table sizing and `(n-1) & hash` indexing
- Secondary hash mixing `h ^ (h >>> 16)`
- Treeification / untreeification of collision bins
- Bitwise low/high split on table doubling
- Fail-fast, late-binding, range-splitting spliterator

They are listed for completeness, not as improvements over the JDK.

### 6. Tree bins remain linked-list traversable
Every `TreeNode` keeps the classic `next`/`previous` links. Consequently, the iterator and spliterator never have to special-case tree structure except for a one-time key snapshot when a tree bin is first encountered. The common-case path stays simple and iteration order within a bin remains stable across treeification.

### 7. Clean serialisation
`size` and `threshold` (in addition to `table` and `modCount`) are marked `transient`.  
`writeObject`/`readObject` therefore emit only the essential capacity, element count and the elements themselves, avoiding the redundant metadata that would otherwise be written by `defaultWriteObject`.

### 8. Independent of HashMap
The implementation does not wrap or depend on `java.util.HashMap`. This keeps the artefact smaller and allows the node layout or tree policy to evolve independently if required.

## Trade-Offs & Architectural Limitations

### 1. Temporary allocation on `retainAll`
Converting a non-`CustomSet` argument allocates an $O(M)$ temporary set. The allocation is short-lived, but repeated calls on large collections increase young-gen GC pressure.  
(The conversion is skipped when the argument is already a `CustomSet`.)

### 2. TreeNode memory cost under high collision
A `TreeNode` carries parent/left/right/previous/colour pointers. Under adversarial or poorly-hashed workloads the per-entry footprint grows and the original memory advantage disappears. (The same trade-off exists in the JDK.)  
In addition, the $O(\log N)$ guarantee only fully holds for `Comparable` keys; non-Comparable keys still fall back to linear search inside a tree bin (exactly as in OpenJDK).

### 3. Iterator allocates on tree bins
Zero-allocation traversal applies to ordinary collision chains. Stepping into a `TreeNode` bin captures an `Object[]` key snapshot in `HashIterator` so `remove()`—which can trigger tree rotations or untreeification—does not disrupt iteration.

### 4. Manual `clone()` surface
`clone()` walks the table, rebuilds lists and re-treeifies bins by hand. Any future change to node linkage or balancing invariants must be mirrored here. Serialisation goes through the normal `add` path and therefore obtains treeification for free; `clone()` is the sole maintenance hotspot. Prefer a copy-constructor or the serialisation path if the manual implementation ever becomes painful.  
Note that `clone()` copies the current table length; a sparsely-filled set therefore retains its capacity, whereas a pure serialise/deserialise round-trip would re-size from the element count.

### 5. Fail-fast is best-effort only
`modCount` provides the same best-effort concurrent-modification detection as the JDK collections. It is not a substitute for external synchronisation; concurrent mutation can still corrupt the structure before a `ConcurrentModificationException` is thrown.

### 6. Fixed load factor and capacity ceiling
`loadFactor` is `final` after construction (matching `HashMap`), and table capacity is capped at `MAXIMUM_CAPACITY` ($2^{30}$ buckets), matching the JDK limit.

## Related Versions

- **Version 1** (array-backed implementation) can be found [here](https://github.com/bk10aao/CustomSet).
- **Version 2** (backed by a Java `HashMap`) can be found [here](https://github.com/bk10aao/CustomSetV2).

All methods implemented are identical to those found in the Java
[Set](https://docs.oracle.com/javase/8/docs/api/java/util/Set.html) interface.

## Performance Advantages & Architectural Optimizations

### 1. Direct Entry Storage vs. JDK Wrapper Overhead (`constructor`, `add`)
- **The JDK Bottleneck:** The standard JDK `HashSet` delegates storage internally to a `HashMap`. Every unique element
  inserted into a `HashSet` is wrapped inside a separate `HashMap.Node` object, which in turn points to a shared static
  dummy presentation value. This creates massive object allocation overhead, increases heap memory pressure, and
  introduces scattered memory references that cause CPU cache misses during traversal.

- **V3 Advantage:** `CustomSetV3` bypasses wrapper objects entirely by storing entry keys directly inside lightweight
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
  (`final Collection<?> retainValues = (c instanceof Set) ? c : new CustomSet<>(c);`). This drops the algorithmic time
  complexity from $O(N \cdot M)$ down to an $O(N + M)$ lookup blitz, yielding massive performance gains (exceeding
  **378× speedups** in benchmark evaluations).

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

### 7. Dedicated Null-Element Tracking (`containsNull`)
- **The JDK Bottleneck:** Standard hash sets typically treat `null` as a special hash-bucket entry or require dedicated
  null-slot handling inside collision chains, introducing conditional branching overhead on every lookup.

- **V3 Advantage:** V3 tracks `null` elements via a dedicated `containsNull` boolean flag rather than occupying a hash
  table bucket index. This eliminates hash-collision overhead, hash code computation, and null-check branching on standard
  lookups.

### 8. Adaptive Table Sizing & Bitwise Masking (`tableSizeFor`, `hash`)
- **The JDK Bottleneck:** Inefficient table sizing calculations or modulo-based index arithmetic can lead to suboptimal
  memory packing and expensive division CPU instructions during lookups.

- **V3 Advantage:** V3 enforces exact power-of-two table capacities via bit-manipulation in `tableSizeFor`, enabling
  table index lookups to execute via lightning-fast bitwise AND operations (`hash & (table.length - 1)`). Furthermore,
  secondary bit-mixing (`h ^ (h >>> 16)`) spreads high bits into lower bits to minimize collisions even with poorly
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
| **`clone()`**                  | 271,763 ns |   496,985 ns   |            **V3**            |     1.83×      |
| **`constructor()`**            |  1.51 ns   |    4.50 ns     |            **V3**            |     2.99×     |
| **`constructor(Collection)`**  | 337,047 ns |   291,602 ns   |            **V1**            |     1.16×     |
| **`constructor(int)`**         |  1.64 ns   |    2,446 ns    |            **V3**            | **1,488.90×** |
| **`constructor(int, double)`** |  1.64 ns   |    2,445 ns    |            **V3**            | **1,493.47×** |
| **`contains(Object)`**         |  3.79 ns   |    2.12 ns     |            **V1**            |     1.79×     |
| **`containsAll(Collection)`**  | 19,229 ns  |    8,456 ns    |            **V1**            |     2.27×      |
| **`equals(Object)`**           | 776,034 ns |  2,376,269 ns  |            **V3**            |     3.06×     |
| **`hashCode()`**               | 78,284 ns  |   109,964 ns   |            **V3**            |     1.40×      |
| **`isEmpty()`**                |  0.53 ns   |    0.54 ns     | **Statistically Equivalent** |    ~1.00×     |
| **`iterator()`**               | 105,462 ns |   238,021 ns   |            **V3**            |     2.26×      |
| **`remove(Object)`**           | 270,351 ns |   509,098 ns   |            **V3**            |     1.88×      |
| **`removeAll(Collection)`**    | 307,264 ns |   549,852 ns   |            **V3**            |     1.79×      |
| **`retainAll(Collection)`**    | 362,631 ns | 183,425,778 ns |            **V3**            |  **505.82×**  |
| **`size()`**                   |  0.53 ns   |    0.54 ns     | **Statistically Equivalent** |    ~1.00×     |
| **`spliterator()`**            | 93,602 ns  |   125,419 ns   |            **V3**            |     1.34×      |
| **`toArray()`**                | 99,550 ns  |   138,589 ns   |            **V3**            |     1.39×      |
| **`toArray(T[])`**             | 109,326 ns |   145,863 ns   |            **V3**            |     1.33×      |
| **`toString()`**               | 592,685 ns |   794,851 ns   |            **V3**            |     1.34×      |

## Performance Testing

<b>Note: The following performance charts are designed to be viewed in dark mode.</b>

### Heat Maps
![V3 vs JDK Heatmap](PerformanceTesting/V3_JDK/heatmap.png)
![V3 vs V2 Heatmap](PerformanceTesting/V2_V3/heatmap.png)
![V3 vs V1 Heatmap](PerformanceTesting/V1_V3/heatmap.png)

### Geometric Mean Comparisons
![V3 vs JDK Geometric Mean](PerformanceTesting/V3_JDK/geometric.png)
![V3 vs V2 Geometric Mean](PerformanceTesting/V2_V3/geometric.png)
![V3 vs V1 Geometric Mean](PerformanceTesting/V1_V3/geometric.png)

## Grouped Graphs

### V1 vs V2 vs V3 vs JDK Grid
![V1 vs V2 vs V3 vs JDK Grid](PerformanceTesting/V1_V2_V3_JDK/grid.png)

### V3 vs JDK Grid
![V3 vs JDK Grid](PerformanceTesting/V3_JDK/grid.png)

### V3 vs V2 Grid
![V3 vs V2 Grid](PerformanceTesting/V2_V3/grid.png)

### V3 vs V1 Grid
![V3 vs V1 Grid](PerformanceTesting/V1_V3/grid.png)

## Individual Graphs

### V1 vs V2 vs V3 vs JDK Performance
<details>
<summary>Click to expand V1 vs V2 vs V3 vs JDK Performance charts (22 methods)</summary>

![constructor() — V1 vs V2 vs V3 vs JDK Performance](PerformanceTesting/V1_V2_V3_JDK/constructor.png)
![constructor(int) — V1 vs V2 vs V3 vs JDK Performance](PerformanceTesting/V1_V2_V3_JDK/constructorCapacity.png)
![constructor(int, double) — V1 vs V2 vs V3 vs JDK Performance](PerformanceTesting/V1_V2_V3_JDK/constructorCapacityLoadFactor.png)
![constructor(Collection) — V1 vs V2 vs V3 vs JDK Performance](PerformanceTesting/V1_V2_V3_JDK/constructorCollection.png)
![add(E) — V1 vs V2 vs V3 vs JDK Performance](PerformanceTesting/V1_V2_V3_JDK/add.png)
![addAll(Collection) — V1 vs V2 vs V3 vs JDK Performance](PerformanceTesting/V1_V2_V3_JDK/addAll.png)
![clear() — V1 vs V2 vs V3 vs JDK Performance](PerformanceTesting/V1_V2_V3_JDK/clear.png)
![clone() — V1 vs V2 vs V3 vs JDK Performance](PerformanceTesting/V1_V2_V3_JDK/clone.png)
![contains(Object) — V1 vs V2 vs V3 vs JDK Performance](PerformanceTesting/V1_V2_V3_JDK/contains.png)
![containsAll(Collection) — V1 vs V2 vs V3 vs JDK Performance](PerformanceTesting/V1_V2_V3_JDK/containsAll.png)
![equals(Object) — V1 vs V2 vs V3 vs JDK Performance](PerformanceTesting/V1_V2_V3_JDK/equals.png)
![hashCode() — V1 vs V2 vs V3 vs JDK Performance](PerformanceTesting/V1_V2_V3_JDK/hashCode.png)
![isEmpty() — V1 vs V2 vs V3 vs JDK Performance](PerformanceTesting/V1_V2_V3_JDK/isEmpty.png)
![iterator() — V1 vs V2 vs V3 vs JDK Performance](PerformanceTesting/V1_V2_V3_JDK/iterator.png)
![remove(Object) — V1 vs V2 vs V3 vs JDK Performance](PerformanceTesting/V1_V2_V3_JDK/remove.png)
![removeAll(Collection) — V1 vs V2 vs V3 vs JDK Performance](PerformanceTesting/V1_V2_V3_JDK/removeAll.png)
![retainAll(Collection) — V1 vs V2 vs V3 vs JDK Performance](PerformanceTesting/V1_V2_V3_JDK/retainAll.png)
![size() — V1 vs V2 vs V3 vs JDK Performance](PerformanceTesting/V1_V2_V3_JDK/size.png)
![spliterator() — V1 vs V2 vs V3 vs JDK Performance](PerformanceTesting/V1_V2_V3_JDK/spliterator.png)
![toArray() — V1 vs V2 vs V3 vs JDK Performance](PerformanceTesting/V1_V2_V3_JDK/toArray.png)
![toArray(T[]) — V1 vs V2 vs V3 vs JDK Performance](PerformanceTesting/V1_V2_V3_JDK/toArrayT.png)
![toString() — V1 vs V2 vs V3 vs JDK Performance](PerformanceTesting/V1_V2_V3_JDK/toString.png)

</details>

### V3 vs JDK Performance
<details>
<summary>Click to expand V3 vs JDK Performance charts (22 methods)</summary>

![constructor() — V3 vs JDK Performance](PerformanceTesting/V3_JDK/constructor.png)
![constructor(int) — V3 vs JDK Performance](PerformanceTesting/V3_JDK/constructorCapacity.png)
![constructor(int, double) — V3 vs JDK Performance](PerformanceTesting/V3_JDK/constructorCapacityLoadFactor.png)
![constructor(Collection) — V3 vs JDK Performance](PerformanceTesting/V3_JDK/constructorCollection.png)
![add(E) — V3 vs JDK Performance](PerformanceTesting/V3_JDK/add.png)
![addAll(Collection) — V3 vs JDK Performance](PerformanceTesting/V3_JDK/addAll.png)
![clear() — V3 vs JDK Performance](PerformanceTesting/V3_JDK/clear.png)
![clone() — V3 vs JDK Performance](PerformanceTesting/V3_JDK/clone.png)
![contains(Object) — V3 vs JDK Performance](PerformanceTesting/V3_JDK/contains.png)
![containsAll(Collection) — V3 vs JDK Performance](PerformanceTesting/V3_JDK/containsAll.png)
![equals(Object) — V3 vs JDK Performance](PerformanceTesting/V3_JDK/equals.png)
![hashCode() — V3 vs JDK Performance](PerformanceTesting/V3_JDK/hashCode.png)
![isEmpty() — V3 vs JDK Performance](PerformanceTesting/V3_JDK/isEmpty.png)
![iterator() — V3 vs JDK Performance](PerformanceTesting/V3_JDK/iterator.png)
![remove(Object) — V3 vs JDK Performance](PerformanceTesting/V3_JDK/remove.png)
![removeAll(Collection) — V3 vs JDK Performance](PerformanceTesting/V3_JDK/removeAll.png)
![retainAll(Collection) — V3 vs JDK Performance](PerformanceTesting/V3_JDK/retainAll.png)
![size() — V3 vs JDK Performance](PerformanceTesting/V3_JDK/size.png)
![spliterator() — V3 vs JDK Performance](PerformanceTesting/V3_JDK/spliterator.png)
![toArray() — V3 vs JDK Performance](PerformanceTesting/V3_JDK/toArray.png)
![toArray(T[]) — V3 vs JDK Performance](PerformanceTesting/V3_JDK/toArrayT.png)
![toString() — V3 vs JDK Performance](PerformanceTesting/V3_JDK/toString.png)

</details>

### V3 vs V2 Performance
<details>
<summary>Click to expand V3 vs V2 Performance charts (22 methods)</summary>

![constructor() — V3 vs V2 Performance](PerformanceTesting/V2_V3/constructor.png)
![constructor(int) — V3 vs V2 Performance](PerformanceTesting/V2_V3/constructorCapacity.png)
![constructor(int, double) — V3 vs V2 Performance](PerformanceTesting/V2_V3/constructorCapacityLoadFactor.png)
![constructor(Collection) — V3 vs V2 Performance](PerformanceTesting/V2_V3/constructorCollection.png)
![add(E) — V3 vs V2 Performance](PerformanceTesting/V2_V3/add.png)
![addAll(Collection) — V3 vs V2 Performance](PerformanceTesting/V2_V3/addAll.png)
![clear() — V3 vs V2 Performance](PerformanceTesting/V2_V3/clear.png)
![clone() — V3 vs V2 Performance](PerformanceTesting/V2_V3/clone.png)
![contains(Object) — V3 vs V2 Performance](PerformanceTesting/V2_V3/contains.png)
![containsAll(Collection) — V3 vs V2 Performance](PerformanceTesting/V2_V3/containsAll.png)
![equals(Object) — V3 vs V2 Performance](PerformanceTesting/V2_V3/equals.png)
![hashCode() — V3 vs V2 Performance](PerformanceTesting/V2_V3/hashCode.png)
![isEmpty() — V3 vs V2 Performance](PerformanceTesting/V2_V3/isEmpty.png)
![iterator() — V3 vs V2 Performance](PerformanceTesting/V2_V3/iterator.png)
![remove(Object) — V3 vs V2 Performance](PerformanceTesting/V2_V3/remove.png)
![removeAll(Collection) — V3 vs V2 Performance](PerformanceTesting/V2_V3/removeAll.png)
![retainAll(Collection) — V3 vs V2 Performance](PerformanceTesting/V2_V3/retainAll.png)
![size() — V3 vs V2 Performance](PerformanceTesting/V2_V3/size.png)
![spliterator() — V3 vs V2 Performance](PerformanceTesting/V2_V3/spliterator.png)
![toArray() — V3 vs V2 Performance](PerformanceTesting/V2_V3/toArray.png)
![toArray(T[]) — V3 vs V2 Performance](PerformanceTesting/V2_V3/toArrayT.png)
![toString() — V3 vs V2 Performance](PerformanceTesting/V2_V3/toString.png)

</details>

### V3 vs V1 Performance
<details>
<summary>Click to expand V3 vs V1 Performance charts (22 methods)</summary>

![constructor() — V3 vs V1 Performance](PerformanceTesting/V1_V3/constructor.png)
![constructor(int) — V3 vs V1 Performance](PerformanceTesting/V1_V3/constructorCapacity.png)
![constructor(int, double) — V3 vs V1 Performance](PerformanceTesting/V1_V3/constructorCapacityLoadFactor.png)
![constructor(Collection) — V3 vs V1 Performance](PerformanceTesting/V1_V3/constructorCollection.png)
![add(E) — V3 vs V1 Performance](PerformanceTesting/V1_V3/add.png)
![addAll(Collection) — V3 vs V1 Performance](PerformanceTesting/V1_V3/addAll.png)
![clear() — V3 vs V1 Performance](PerformanceTesting/V1_V3/clear.png)
![clone() — V3 vs V1 Performance](PerformanceTesting/V1_V3/clone.png)
![contains(Object) — V3 vs V1 Performance](PerformanceTesting/V1_V3/contains.png)
![containsAll(Collection) — V3 vs V1 Performance](PerformanceTesting/V1_V3/containsAll.png)
![equals(Object) — V3 vs V1 Performance](PerformanceTesting/V1_V3/equals.png)
![hashCode() — V3 vs V1 Performance](PerformanceTesting/V1_V3/hashCode.png)
![isEmpty() — V3 vs V1 Performance](PerformanceTesting/V1_V3/isEmpty.png)
![iterator() — V3 vs V1 Performance](PerformanceTesting/V1_V3/iterator.png)
![remove(Object) — V3 vs V1 Performance](PerformanceTesting/V1_V3/remove.png)
![removeAll(Collection) — V3 vs V1 Performance](PerformanceTesting/V1_V3/removeAll.png)
![retainAll(Collection) — V3 vs V1 Performance](PerformanceTesting/V1_V3/retainAll.png)
![size() — V3 vs V1 Performance](PerformanceTesting/V1_V3/size.png)
![spliterator() — V3 vs V1 Performance](PerformanceTesting/V1_V3/spliterator.png)
![toArray() — V3 vs V1 Performance](PerformanceTesting/V1_V3/toArray.png)
![toArray(T[]) — V3 vs V1 Performance](PerformanceTesting/V1_V3/toArrayT.png)
![toString() — V3 vs V1 Performance](PerformanceTesting/V1_V3/toString.png)

</details>