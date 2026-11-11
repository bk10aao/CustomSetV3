# Custom Set V3

Implementation of a Set using a custom Red Black tree. The standard JDK version  is a wrapper of a JDK HashMap and 
therefore stores both key and value, this version is backed similarly, yet only requiring the value, minimizing memory 
overhead.

Version 1 being an array back version can be found [here](https://github.com/bk10aao/CustomSet).

Version 2 being back by a Java HashMap can be found [here](https://github.com/bk10aao/CustomSetV2).

All methods implemented are identical to those found in the Java [Set](https://docs.oracle.com/javase/8/docs/api/java/util/Set.html) interface.

## Build and Test

1. To build and test the project run command `./gradlew clean build`
2. To test the project run command ` ./gradlew clean test`

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

### V3 vs JDK HashSet

| Method                         |   V3 (ns)   | JDK HashSet (ns) |            Winner            | Margin |
|:-------------------------------|:-----------:|:----------------:|:----------------------------:|:------:|
| **`add(E)`**                   |   866,325   |    1,103,464     |            **V3**            | 1.27×  |
| **`addAll(Collection)`**       |   581,141   |     936,890      |            **V3**            | 1.61×  |
| **`clear()`**                  |   314,558   |     497,317      |            **V3**            | 1.58×  |
| **`clone()`**                  |   628,457   |     833,011      |            **V3**            | 1.33×  |
| **`constructor()`**            |    1.60     |       3.01       |            **V3**            | 1.88×  |
| **`constructor(Collection)`**  |   624,257   |     814,089      |            **V3**            | 1.30×  |
| **`constructor(int)`**         |    1.84     |       3.15       |            **V3**            | 1.71×  |
| **`constructor(int, double)`** |    1.84     |       3.15       |            **V3**            | 1.71×  |
| **`contains(Object)`**         |    2.25     |       2.22       | **Statistically Equivalent** | 1.01×  |
| **`containsAll(Collection)`**  |    8,451    |      26,374      |            **V3**            | 3.12×  |
| **`equals(Object)`**           |  1,095,450  |    1,483,152     |            **V3**            | 1.35×  |
| **`hashCode()`**               |   97,727    |     208,277      |            **V3**            | 2.13×  |
| **`isEmpty()`**                |    0.54     |       0.64       |            **V3**            | 1.19×  |
| **`iterator()`**               |   117,276   |     208,537      |            **V3**            | 1.78×  |
| **`remove(Object)`**           |   638,211   |     778,885      |            **V3**            | 1.22×  |
| **`removeAll(Collection)`**    |   654,625   |     815,236      |            **V3**            | 1.25×  |
| **`retainAll(Collection)`**    | 341,450,016 |   343,015,587    | **Statistically Equivalent** | 1.00×  |
| **`size()`**                   |    0.54     |       0.64       |            **V3**            | 1.18×  |
| **`spliterator()`**            |   142,612   |     131,726      | **Statistically Equivalent** | 1.08×  |
| **`toArray()`**                |   120,236   |     123,778      | **Statistically Equivalent** | 1.03×  |
| **`toArray(T[])`**             |   140,018   |     136,907      | **Statistically Equivalent** | 1.02×  |
| **`toString()`**               |   860,517   |     903,591      | **Statistically Equivalent** | 1.05×  |

---

### V3 vs V2

| Method                         |   V3 (ns)   |  V2 (ns)   |            Winner            |   Margin    |
|:-------------------------------|:-----------:|:----------:|:----------------------------:|:-----------:|
| **`add(E)`**                   |   866,325   | 1,070,281  |            **V3**            |    1.24×    |
| **`addAll(Collection)`**       |   581,141   |  910,499   |            **V3**            |    1.57×    |
| **`clear()`**                  |   314,558   |    2.07    |            **V2**            | 151,935.26× |
| **`clone()`**                  |   628,457   |  380,860   |            **V2**            |    1.65×    |
| **`constructor()`**            |    1.60     |    3.02    |            **V3**            |    1.88×    |
| **`constructor(Collection)`**  |   624,257   |  822,653   |            **V3**            |    1.32×    |
| **`constructor(int)`**         |    1.84     |    3.16    |            **V3**            |    1.71×    |
| **`constructor(int, double)`** |    1.84     |    3.21    |            **V3**            |    1.74×    |
| **`contains(Object)`**         |    2.25     |    2.39    |            **V3**            |    1.07×    |
| **`containsAll(Collection)`**  |    8,451    |   27,014   |            **V3**            |    3.20×    |
| **`equals(Object)`**           |  1,095,450  | 1,517,248  |            **V3**            |    1.39×    |
| **`hashCode()`**               |   97,727    |  105,126   | **Statistically Equivalent** |    1.08×    |
| **`isEmpty()`**                |    0.54     |    0.74    |            **V3**            |    1.38×    |
| **`iterator()`**               |   117,276   |  209,272   |            **V3**            |    1.78×    |
| **`remove(Object)`**           |   638,211   |  379,697   |            **V2**            |    1.68×    |
| **`removeAll(Collection)`**    |   654,625   |  349,324   |            **V2**            |    1.87×    |
| **`retainAll(Collection)`**    | 341,450,016 | 38,383,333 |            **V2**            |    8.90×    |
| **`size()`**                   |    0.54     |    0.74    |            **V3**            |    1.37×    |
| **`spliterator()`**            |   142,612   |  131,245   | **Statistically Equivalent** |    1.09×    |
| **`toArray()`**                |   120,236   |  124,008   | **Statistically Equivalent** |    1.03×    |
| **`toArray(T[])`**             |   140,018   |  133,199   | **Statistically Equivalent** |    1.05×    |
| **`toString()`**               |   860,517   | 1,065,356  |            **V3**            |    1.24×    |

---

### V3 vs V1

| Method                         |   V3 (ns)   |   V1 (ns)   |            Winner            |  Margin   |
|:-------------------------------|:-----------:|:-----------:|:----------------------------:|:---------:|
| **`add(E)`**                   |   866,325   |  2,615,281  |            **V3**            |   3.02×   |
| **`addAll(Collection)`**       |   581,141   |  2,776,303  |            **V3**            |   4.78×   |
| **`clear()`**                  |   314,558   |   692,785   |            **V3**            |   2.20×   |
| **`clone()`**                  |   628,457   |   698,006   |            **V3**            |   1.11×   |
| **`constructor()`**            |    1.60     |    4.50     |            **V3**            |   2.81×   |
| **`constructor(Collection)`**  |   624,257   |   457,421   |            **V3**            |   1.36×   |
| **`constructor(int)`**         |    1.84     |    2,847    |            **V3**            | 1,543.38× |
| **`constructor(int, double)`** |    1.84     |    2,847    |            **V3**            | 1,545.00× |
| **`contains(Object)`**         |    2.25     |    2.12     | **Statistically Equivalent** |   1.06×   |
| **`containsAll(Collection)`**  |    8,451    |    9,937    |            **V3**            |   1.18×   |
| **`equals(Object)`**           |  1,095,450  |  3,197,317  |            **V3**            |   2.92×   |
| **`hashCode()`**               |   97,727    |   134,003   |            **V3**            |   1.37×   |
| **`isEmpty()`**                |    0.54     |    0.54     | **Statistically Equivalent** |   1.00×   |
| **`iterator()`**               |   117,276   |   288,993   |            **V3**            |   2.46×   |
| **`remove(Object)`**           |   638,211   |   713,294   |            **V3**            |   1.12×   |
| **`removeAll(Collection)`**    |   654,625   |   747,767   |            **V3**            |   1.14×   |
| **`retainAll(Collection)`**    | 341,450,016 | 347,300,706 | **Statistically Equivalent** |   1.02×   |
| **`size()`**                   |    0.54     |    0.54     | **Statistically Equivalent** |   1.00×   |
| **`spliterator()`**            |   142,612   |   151,917   | **Statistically Equivalent** |   1.07×   |
| **`toArray()`**                |   120,236   |   167,134   |            **V3**            |   1.39×   |
| **`toArray(T[])`**             |   140,018   |   176,093   |            **V3**            |   1.26×   |
| **`toString()`**               |   860,517   |   950,141   | **Statistically Equivalent** |   1.10×   |
## Performance Testing

<b>Note: The following performance charts are designed to be viewed in dark mode.</b>

### Heat Maps
![Heatmap](PerformanceTesting/V3_JDK/heatmap.png)
![Heatmap](PerformanceTesting/V2_V3/heatmap.png)
![Heatmap](PerformanceTesting/V1_V3/heatmap.png)

### Geometric
![Geometric](PerformanceTesting/V3_JDK/geometric.png)
![Geometric](PerformanceTesting/V2_V3/geometric.png)

## Grouped Graphs 
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
![Combined Performance Charts](PerformanceTesting/V1_V2_V3_JDK/spliterator.png)
![Combined Performance Charts](PerformanceTesting/V1_V2_V3_JDK/remove.png)
![Combined Performance Charts](PerformanceTesting/V1_V2_V3_JDK/removeAll.png)
![Combined Performance Charts](PerformanceTesting/V1_V2_V3_JDK/retainAll.png)
![Combined Performance Charts](PerformanceTesting/V1_V2_V3_JDK/size.png)
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
![Combined Performance Charts](PerformanceTesting/V3_JDK/spliterator.png)
![Combined Performance Charts](PerformanceTesting/V3_JDK/remove.png)
![Combined Performance Charts](PerformanceTesting/V3_JDK/removeAll.png)
![Combined Performance Charts](PerformanceTesting/V3_JDK/retainAll.png)
![Combined Performance Charts](PerformanceTesting/V3_JDK/size.png)
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
![Combined Performance Charts](PerformanceTesting/V2_V3/spliterator.png)
![Combined Performance Charts](PerformanceTesting/V2_V3/remove.png)
![Combined Performance Charts](PerformanceTesting/V2_V3/removeAll.png)
![Combined Performance Charts](PerformanceTesting/V2_V3/retainAll.png)
![Combined Performance Charts](PerformanceTesting/V2_V3/size.png)
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
![Combined Performance Charts](PerformanceTesting/V1_V3/spliterator.png)
![Combined Performance Charts](PerformanceTesting/V1_V3/remove.png)
![Combined Performance Charts](PerformanceTesting/V1_V3/removeAll.png)
![Combined Performance Charts](PerformanceTesting/V1_V3/retainAll.png)
![Combined Performance Charts](PerformanceTesting/V1_V3/size.png)
![Combined Performance Charts](PerformanceTesting/V1_V3/toArray.png)
![Combined Performance Charts](PerformanceTesting/V1_V3/toArrayT.png)
![Combined Performance Charts](PerformanceTesting/V1_V3/toString.png)