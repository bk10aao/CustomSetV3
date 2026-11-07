
Implementation of a Set using a custom Red Black tree. The standard JDK version  is a wrapper of a JDK HashMap and 
therefore stores both key and value, this version is backed similarly, yet only requiring the value, minimizing memory 
overhead.

Version 1 being an array back version can be found [here](https://github.com/bk10aao/CustomSet).

Version 2 being back by a Java HashMap can be found [here](https://github.com/bk10aao/CustomSetV2).

All methods implemented are identical to those found in the Java [Set](https://docs.oracle.com/javase/8/docs/api/java/util/Set.html) interface.

## Build and Test

1. To build and test the project run command `./gradlew clean build`
2. To test the project run command `gradle test --tests customset.CustomSetTest`

## Time Complexity

| Operation                       |                      V3                       |                 JDK `HashSet`                 | Winner  |
|:--------------------------------|:---------------------------------------------:|:---------------------------------------------:|:-------:|
| **`constructor()`**             |                    $O(1)$                     |                    $O(1)$                     | **Tie** |
| **`constructor(capacity)`**     |                    $O(1)$                     |                    $O(1)$                     | **Tie** |
| **`constructor(Collection c)`** |            $O(N)$ / $O(N \log N)$             |            $O(N)$ / $O(N \log N)$             | **Tie** |
| **`add(E e)`**                  | **$O(1)$** (Amortized)<br>$O(\log N)$ (Worst) | **$O(1)$** (Amortized)<br>$O(\log N)$ (Worst) | **Tie** |
| **`addAll(Collection c)`**      |            $O(M)$ / $O(M \log N)$             |            $O(M)$ / $O(M \log N)$             | **Tie** |
| **`clone()`**                   |                    $O(N)$                     |                    $O(N)$                     | **Tie** |
| **`contains(Object o)`**        |    **$O(1)$** (Avg)<br>$O(\log N)$ (Worst)    |    **$O(1)$** (Avg)<br>$O(\log N)$ (Worst)    | **Tie** |
| **`clear()`**                   |                    $O(C)$                     |                    $O(C)$                     | **Tie** |
| **`containsAll(Collection c)`** |                 $O(M \log N)$                 |                 $O(M \log N)$                 | **Tie** |
| **`equals(Object o)`**          |            $O(N)$ / $O(N \log N)$             |            $O(N)$ / $O(N \log N)$             | **Tie** |
| **`hashCode()`**                |                  $O(N + C)$                   |                  $O(N + C)$                   | **Tie** |
| **`isEmpty()`**                 |                    $O(1)$                     |                    $O(1)$                     | **Tie** |
| **`iterator()`**                |    $O(1)$ (Init)<br>$O(N + C)$ (Traversal)    |    $O(1)$ (Init)<br>$O(N + C)$ (Traversal)    | **Tie** |
| **`remove(Object o)`**          |    **$O(1)$** (Avg)<br>$O(\log N)$ (Worst)    |    **$O(1)$** (Avg)<br>$O(\log N)$ (Worst)    | **Tie** |
| **`removeAll(Collection c)`**   | $O(\min(M \log N, N \cdot \text{contains}))$  | $O(\min(M \log N, N \cdot \text{contains}))$  | **Tie** |
| **`retainAll(Collection c)`**   |         $O(N \cdot \text{contains})$          |         $O(N \cdot \text{contains})$          | **Tie** |
| **`size()`**                    |                    $O(1)$                     |                    $O(1)$                     | **Tie** |
| **`toArray()`**                 |                  $O(N + C)$                   |                  $O(N + C)$                   | **Tie** |
| **`toArray(T[])`**              |                  $O(N + C)$                   |                  $O(N + C)$                   | **Tie** |
| **`toString()`**                |                  $O(N + C)$                   |                  $O(N + C)$                   | **Tie** |

## Space Complexity

| Operation                       |                      V3                      |                JDK `HashSet`                 | Winner  |
|:--------------------------------|:--------------------------------------------:|:--------------------------------------------:|:-------:|
| **`constructor()`**             |                    $O(1)$                    |                    $O(1)$                    | **Tie** |
| **`constructor(capacity)`**     |                    $O(C)$                    |                    $O(C)$                    | **Tie** |
| **`constructor(Collection c)`** |         $O(N + C)$ (Direct storage)          |      $O(N + C)$ (Wrapped node overhead)      | **V3**  |
| **`add(E e)`**                  | $O(1)$ (Amortized)<br>$O(\log N)$ (Tree bin) | $O(1)$ (Amortized)<br>$O(\log N)$ (Tree bin) | **Tie** |
| **`addAll(Collection c)`**      |           $O(M + C_{\text{new}})$            |           $O(M + C_{\text{new}})$            | **Tie** |
| **`clone()`**                   |          $O(N + C)$ (Shallow copy)           |          $O(N + C)$ (Shallow copy)           | **Tie** |
| **`contains(Object o)`**        |                    $O(1)$                    |                    $O(1)$                    | **Tie** |
| **`clear()`**                   |        $O(1)$ (In-place array reset)         |        $O(1)$ (In-place array reset)         | **Tie** |
| **`containsAll(Collection c)`** |                    $O(1)$                    |                    $O(1)$                    | **Tie** |
| **`equals(Object o)`**          |                    $O(1)$                    |                    $O(1)$                    | **Tie** |
| **`hashCode()`**                |                    $O(1)$                    |                    $O(1)$                    | **Tie** |
| **`isEmpty()`**                 |                    $O(1)$                    |                    $O(1)$                    | **Tie** |
| **`iterator()`**                |          $O(1)$ (Cursor state only)          |          $O(1)$ (Cursor state only)          | **Tie** |
| **`remove(Object o)`**          |                    $O(1)$                    |                    $O(1)$                    | **Tie** |
| **`removeAll(Collection c)`**   |                    $O(1)$                    |                    $O(1)$                    | **Tie** |
| **`retainAll(Collection c)`**   |                    $O(1)$                    |                    $O(1)$                    | **Tie** |
| **`size()`**                    |                    $O(1)$                    |                    $O(1)$                    | **Tie** |
| **`toArray()`**                 |          $O(N)$ (New array output)           |          $O(N)$ (New array output)           | **Tie** |
| **`toArray(T[])`**              |          $O(N)$ (New array output)           |          $O(N)$ (New array output)           | **Tie** |
| **`toString()`**                |            $O(N)$ (String buffer)            |            $O(N)$ (String buffer)            | **Tie** |

### Asymptotic Variables & Parameters

- **$N$**: The number of elements currently stored in the set (i.e., `size()`).
- **$M$**: The number of elements contained in the target collection passed as an argument to bulk operations (e.g., `addAll`, `containsAll`, `removeAll`).
- **$C$**: The current capacity (size) of the internal hash table backing array (the number of buckets).

## Performance

### V3 vs JDK

| Method                        | V3 (ns)    | JDK (ns)   |            Winner            | Margin |
|:------------------------------|:-----------|:-----------|:----------------------------:|:------:|
| **`add(E)`**                  | 146,366    | 234,506    |            **V3**            | 1.60×  |
| **`addAll(Collection)`**      | 92,164     | 247,432    |            **V3**            | 2.68×  |
| **`clear()`**                 | 13,564     | 14,886     | **Statistically Equivalent** | 1.10×  |
| **`clone()`**                 | 124,779    | 237,006    |            **V3**            | 1.90×  |
| **`constructor()`**           | 41         | 49         |            **V3**            | 1.20×  |
| **`constructor(Collection)`** | 92,395     | 191,957    |            **V3**            | 2.08×  |
| **`contains(Object)`**        | 37         | 66         |            **V3**            | 1.78×  |
| **`containsAll(Collection)`** | 5,031      | 16,117     |            **V3**            | 3.20×  |
| **`equals(Object)`**          | 243,488    | 835,394    |            **V3**            | 3.43×  |
| **`hashCode()`**              | 49,486     | 146,478    |            **V3**            | 2.96×  |
| **`isEmpty()`**               | 33         | 63         |            **V3**            | 1.91×  |
| **`iterator()`**              | 61,863     | 176,145    |            **V3**            | 2.85×  |
| **`remove(Object)`**          | 38         | 113        |            **V3**            | 2.97×  |
| **`removeAll(Collection)`**   | 17,521     | 39,788     |            **V3**            | 2.27×  |
| **`retainAll(Collection)`**   | 95,070,383 | 95,849,271 | **Statistically Equivalent** | 1.01×  |
| **`size()`**                  | 33         | 41         |            **V3**            | 1.24×  |
| **`toArray()`**               | 53,764     | 60,653     | **Statistically Equivalent** | 1.13×  |
| **`toArray(T[])`**            | 64,664     | 86,634     |            **V3**            | 1.34×  |
| **`toString()`**              | 484,510    | 488,435    | **Statistically Equivalent** | 1.01×  |

### V3 vs V2

| Method                        | V3 (ns)    | V2 (ns)    |            Winner            | Margin |
|:------------------------------|:-----------|:-----------|:----------------------------:|:------:|
| **`add(E)`**                  | 146,366    | 233,182    |            **V3**            | 1.59×  |
| **`addAll(Collection)`**      | 92,164     | 255,128    |            **V3**            | 2.77×  |
| **`clear()`**                 | 13,564     | 13,735     | **Statistically Equivalent** | 1.01×  |
| **`clone()`**                 | 124,779    | 195,211    |            **V3**            | 1.56×  |
| **`constructor()`**           | 41         | 40         | **Statistically Equivalent** | 1.03×  |
| **`constructor(Collection)`** | 92,395     | 197,882    |            **V3**            | 2.14×  |
| **`contains(Object)`**        | 37         | 100        |            **V3**            | 2.70×  |
| **`containsAll(Collection)`** | 5,031      | 16,922     |            **V3**            | 3.36×  |
| **`equals(Object)`**          | 243,488    | 452,081    |            **V3**            | 1.86×  |
| **`hashCode()`**              | 49,486     | 64,098     |            **V3**            | 1.30×  |
| **`isEmpty()`**               | 33         | 42         |            **V3**            | 1.27×  |
| **`iterator()`**              | 61,863     | 100,736    |            **V3**            | 1.63×  |
| **`remove(Object)`**          | 38         | 66         |            **V3**            | 1.74×  |
| **`removeAll(Collection)`**   | 17,521     | 35,869     |            **V3**            | 2.05×  |
| **`retainAll(Collection)`**   | 95,070,383 | 93,187,198 | **Statistically Equivalent** | 1.02×  |
| **`size()`**                  | 33         | 38         |            **V3**            | 1.15×  |
| **`toArray()`**               | 53,764     | 56,113     | **Statistically Equivalent** | 1.04×  |
| **`toArray(T[])`**            | 64,664     | 65,604     | **Statistically Equivalent** | 1.01×  |
| **`toString()`**              | 484,510    | 467,994    | **Statistically Equivalent** | 1.04×  |

## Performance Testing

<b>Note: The following performance charts are designed to be viewed in dark mode.</b>

### Heat Maps
![Heatmap](PerformanceTesting/V3_JDK/heatmap.png)
![Heatmap](PerformanceTesting/V2_V3/heatmap.png)

### Geometric
![Geometric](PerformanceTesting/V3_JDK/geometric.png)
![Geometric](PerformanceTesting/V2_V3/geometric.png)

## Grouped Graphs 
### V3 vs JDK
![Combined Performance Charts](PerformanceTesting/V3_JDK/grid.png)

### V3 vs V2
![Combined Performance Charts](PerformanceTesting/V2_V3/grid.png)

## Individual Graphs
### V1 vs V2 vs V3 vs JDK Performance
![Combined Performance Charts](PerformanceTesting/V1_V2_V3_JDK/constructor.png)
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
![Combined Performance Charts](PerformanceTesting/V1_V2_V3_JDK/toArray.png)
![Combined Performance Charts](PerformanceTesting/V1_V2_V3_JDK/toArrayT.png)
![Combined Performance Charts](PerformanceTesting/V1_V2_V3_JDK/toString.png)

### V3 vs JDK Performance
![Combined Performance Charts](PerformanceTesting/V3_JDK/constructor.png)
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
![Combined Performance Charts](PerformanceTesting/V3_JDK/toArray.png)
![Combined Performance Charts](PerformanceTesting/V3_JDK/toArrayT.png)
![Combined Performance Charts](PerformanceTesting/V3_JDK/toString.png)

### V3 vs V2 Performance
![Combined Performance Charts](PerformanceTesting/V2_V3/constructor.png)
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
![Combined Performance Charts](PerformanceTesting/V2_V3/toArray.png)
![Combined Performance Charts](PerformanceTesting/V2_V3/toArrayT.png)
![Combined Performance Charts](PerformanceTesting/V2_V3/toString.png)
