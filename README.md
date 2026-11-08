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

### V3 vs JDK

| Method                        | V3 (ns)    | JDK (ns)   |            Winner            | Margin |
|:------------------------------|:-----------|:-----------|:----------------------------:|:------:|
| **`add(E)`**                  | 152,784    | 229,506    |            **V3**            | 1.50×  |
| **`addAll(Collection)`**      | 94,672     | 264,354    |            **V3**            | 2.79×  |
| **`clear()`**                 | 13,816     | 14,886     | **Statistically Equivalent** | 1.08×  |
| **`clone()`**                 | 124,652    | 227,002    |            **V3**            | 1.82×  |
| **`constructor()`**           | 41         | 49         |            **V3**            | 1.20×  |
| **`constructor(Collection)`** | 94,270     | 198,857    |            **V3**            | 2.11×  |
| **`contains(Object)`**        | 47         | 66         |            **V3**            | 1.40×  |
| **`containsAll(Collection)`** | 5,168      | 15,828     |            **V3**            | 3.06×  |
| **`equals(Object)`**          | 252,115    | 693,257    |            **V3**            | 2.75×  |
| **`hashCode()`**              | 50,519     | 140,641    |            **V3**            | 2.78×  |
| **`isEmpty()`**               | 34         | 71         |            **V3**            | 2.09×  |
| **`iterator()`**              | 62,114     | 181,739    |            **V3**            | 2.93×  |
| **`remove(Object)`**          | 42         | 118        |            **V3**            | 2.81×  |
| **`removeAll(Collection)`**   | 18,037     | 39,977     |            **V3**            | 2.22×  |
| **`retainAll(Collection)`**   | 91,893,930 | 93,672,223 | **Statistically Equivalent** | 1.02×  |
| **`size()`**                  | 42         | 40         | **Statistically Equivalent** | 1.05×  |
| **`toArray()`**               | 55,719     | 61,206     |            **V3**            | 1.10×  |
| **`toArray(T[])`**            | 66,061     | 86,787     |            **V3**            | 1.31×  |
| **`toString()`**              | 485,781    | 482,031    | **Statistically Equivalent** | 1.01×  |

### V3 vs V2

| Method                        | V3 (ns)    | V2 (ns)    |            Winner            | Margin |
|:------------------------------|:-----------|:-----------|:----------------------------:|:------:|
| **`add(E)`**                  | 152,784    | 233,182    |            **V3**            | 1.53×  |
| **`addAll(Collection)`**      | 94,672     | 255,128    |            **V3**            | 2.69×  |
| **`clear()`**                 | 13,816     | 13,735     | **Statistically Equivalent** | 1.01×  |
| **`clone()`**                 | 124,652    | 195,211    |            **V3**            | 1.57×  |
| **`constructor()`**           | 41         | 40         | **Statistically Equivalent** | 1.02×  |
| **`constructor(Collection)`** | 94,270     | 197,882    |            **V3**            | 2.10×  |
| **`contains(Object)`**        | 47         | 100        |            **V3**            | 2.13×  |
| **`containsAll(Collection)`** | 5,168      | 16,922     |            **V3**            | 3.27×  |
| **`equals(Object)`**          | 252,115    | 452,081    |            **V3**            | 1.79×  |
| **`hashCode()`**              | 50,519     | 64,098     |            **V3**            | 1.27×  |
| **`isEmpty()`**               | 34         | 42         |            **V3**            | 1.24×  |
| **`iterator()`**              | 62,114     | 100,736    |            **V3**            | 1.62×  |
| **`remove(Object)`**          | 42         | 66         |            **V3**            | 1.57×  |
| **`removeAll(Collection)`**   | 18,037     | 35,869     |            **V3**            | 1.99×  |
| **`retainAll(Collection)`**   | 91,893,930 | 93,187,198 | **Statistically Equivalent** | 1.01×  |
| **`size()`**                  | 42         | 38         | **Statistically Equivalent** | 1.11×  |
| **`toArray()`**               | 55,719     | 56,113     | **Statistically Equivalent** | 1.01×  |
| **`toArray(T[])`**            | 66,061     | 65,604     | **Statistically Equivalent** | 1.01×  |
| **`toString()`**              | 485,781    | 467,994    | **Statistically Equivalent** | 1.04×  |

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
