
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

| Operation                        |            CustomSet V3 Complexity            |           JDK `HashSet` Complexity            | Winner  |
|:---------------------------------|:---------------------------------------------:|:---------------------------------------------:|:-------:|
| **`constructor()`**              |                    $O(1)$                     |                    $O(1)$                     | **Tie** |
| **`constructor(capacity)`**      |                    $O(1)$                     |                    $O(1)$                     | **Tie** |
| **`constructor(Collection c)`**  |            $O(N)$ / $O(N \log N)$             |            $O(N)$ / $O(N \log N)$             | **Tie** |
| **`add(E e)`**                   | **$O(1)$** (Amortized)<br>$O(\log N)$ (Worst) | **$O(1)$** (Amortized)<br>$O(\log N)$ (Worst) | **Tie** |
| **`addAll(Collection c)`**       |            $O(M)$ / $O(M \log N)$             |            $O(M)$ / $O(M \log N)$             | **Tie** |
| **`clone()`**                    |                    $O(N)$                     |                    $O(N)$                     | **Tie** |
| **`contains(Object o)`**         |    **$O(1)$** (Avg)<br>$O(\log N)$ (Worst)    |    **$O(1)$** (Avg)<br>$O(\log N)$ (Worst)    | **Tie** |
| **`clear()`**                    |                    $O(C)$                     |                    $O(C)$                     | **Tie** |
| **`containsAll(Collection c)`**  |                 $O(M \log N)$                 |                 $O(M \log N)$                 | **Tie** |
| **`equals(Object o)`**           |            $O(N)$ / $O(N \log N)$             |            $O(N)$ / $O(N \log N)$             | **Tie** |
| **`hashCode()`**                 |                  $O(N + C)$                   |                  $O(N + C)$                   | **Tie** |
| **`isEmpty()`**                  |                    $O(1)$                     |                    $O(1)$                     | **Tie** |
| **`iterator()`**                 |    $O(1)$ (Init)<br>$O(N + C)$ (Traversal)    |    $O(1)$ (Init)<br>$O(N + C)$ (Traversal)    | **Tie** |
| **`remove(Object o)`**           |    **$O(1)$** (Avg)<br>$O(\log N)$ (Worst)    |    **$O(1)$** (Avg)<br>$O(\log N)$ (Worst)    | **Tie** |
| **`removeAll(Collection c)`**    | $O(\min(M \log N, N \cdot \text{contains}))$  | $O(\min(M \log N, N \cdot \text{contains}))$  | **Tie** |
| **`retainAll(Collection c)`**    |         $O(N \cdot \text{contains})$          |         $O(N \cdot \text{contains})$          | **Tie** |
| **`size()`**                     |                    $O(1)$                     |                    $O(1)$                     | **Tie** |
| **`toArray()` / `toArray(T[])`** |                  $O(N + C)$                   |                  $O(N + C)$                   | **Tie** |
| **`toString()`**                 |                  $O(N + C)$                   |                  $O(N + C)$                   | **Tie** |

## Space Complexity

| Operation                        |                      V3                      |                     JDK                      | Winner  |
|:---------------------------------|:--------------------------------------------:|:--------------------------------------------:|:-------:|
| **`constructor()`**              |                    $O(1)$                    |                    $O(1)$                    | **Tie** |
| **`constructor(capacity)`**      |                    $O(C)$                    |                    $O(C)$                    | **Tie** |
| **`constructor(Collection c)`**  |                  $O(N + C)$                  |                  $O(N + C)$                  | **Tie** |
| **`add(E e)`**                   | $O(1)$ (Amortized)<br>$O(\log N)$ (Tree bin) | $O(1)$ (Amortized)<br>$O(\log N)$ (Tree bin) | **Tie** |
| **`addAll(Collection c)`**       |           $O(M + C_{\text{new}})$            |           $O(M + C_{\text{new}})$            | **Tie** |
| **`clone()`**                    |                  $O(N + C)$                  |                  $O(N + C)$                  | **Tie** |
| **`contains(Object o)`**         |                    $O(1)$                    |                    $O(1)$                    | **Tie** |
| **`clear()`**                    |        $O(1)$ (In-place array reset)         |        $O(1)$ (In-place array reset)         | **Tie** |
| **`containsAll(Collection c)`**  |                    $O(1)$                    |                    $O(1)$                    | **Tie** |
| **`equals(Object o)`**           |                    $O(1)$                    |                    $O(1)$                    | **Tie** |
| **`hashCode()`**                 |                    $O(1)$                    |                    $O(1)$                    | **Tie** |
| **`isEmpty()`**                  |                    $O(1)$                    |                    $O(1)$                    | **Tie** |
| **`iterator()`**                 |          $O(1)$ (Cursor state only)          |          $O(1)$ (Cursor state only)          | **Tie** |
| **`remove(Object o)`**           |                    $O(1)$                    |                    $O(1)$                    | **Tie** |
| **`removeAll(Collection c)`**    |                    $O(1)$                    |                    $O(1)$                    | **Tie** |
| **`retainAll(Collection c)`**    |                    $O(1)$                    |                    $O(1)$                    | **Tie** |
| **`size()`**                     |                    $O(1)$                    |                    $O(1)$                    | **Tie** |
| **`toArray()` / `toArray(T[])`** |          $O(N)$ (New array output)           |          $O(N)$ (New array output)           | **Tie** |
| **`toString()`**                 |            $O(N)$ (String buffer)            |            $O(N)$ (String buffer)            | **Tie** |

### Asymptotic Variables & Parameters

- **$N$**: The number of elements currently stored in the set (i.e., `size()`).
- **$M$**: The number of elements contained in the target collection passed as an argument to bulk operations (e.g., `addAll`, `containsAll`, `removeAll`).
- **$C$**: The current capacity (size) of the internal hash table backing array (the number of buckets).

## Performance

### V3 vs JDK

| Method                        | V3 (ns)  | JDK (ns) |            Winner            | Margin |
|:------------------------------|:---------|:---------|:----------------------------:|:------:|
| **`add(E)`**                  | 145394   | 229506   |            **V3**            | 1.58×  |
| **`addAll(Collection)`**      | 129863   | 264354   |            **V3**            | 2.04×  |
| **`clear()`**                 | 13619    | 14886    | **Statistically Equivalent** | 1.09×  |
| **`clone()`**                 | 125162   | 227002   |            **V3**            | 1.81×  |
| **`constructor()`**           | 44       | 49       | **Statistically Equivalent** | 1.11×  |
| **`constructor(Collection)`** | 97070    | 198857   |            **V3**            | 2.05×  |
| **`contains(Object)`**        | 41       | 66       |            **V3**            | 1.61×  |
| **`containsAll(Collection)`** | 5207     | 15828    |            **V3**            | 3.04×  |
| **`equals(Object)`**          | 251750   | 693257   |            **V3**            | 2.75×  |
| **`hashCode()`**              | 49862    | 140641   |            **V3**            | 2.82×  |
| **`isEmpty()`**               | 37       | 71       |            **V3**            | 1.92×  |
| **`iterator()`**              | 64573    | 181739   |            **V3**            | 2.81×  |
| **`remove(Object)`**          | 44       | 118      |            **V3**            | 2.68×  |
| **`removeAll(Collection)`**   | 17551    | 39977    |            **V3**            | 2.28×  |
| **`retainAll(Collection)`**   | 89072297 | 93672223 | **Statistically Equivalent** | 1.05×  |
| **`size()`**                  | 52       | 40       | **Statistically Equivalent** | 1.30×  |
| **`toArray()`**               | 55145    | 61206    | **Statistically Equivalent** | 1.11×  |
| **`toArray(T[])`**            | 67277    | 86787    |            **V3**            | 1.29×  |
| **`toString()`**              | 473965   | 482031   | **Statistically Equivalent** | 1.02×  |

### V3 vs V2

| Method                        | V3 (ns)  | V2 (ns)  |            Winner            | Margin |
|:------------------------------|:---------|:---------|:----------------------------:|:------:|
| **`add(E)`**                  | 145394   | 233182   |            **V3**            | 1.60×  |
| **`addAll(Collection)`**      | 129863   | 255128   |            **V3**            | 1.96×  |
| **`clear()`**                 | 13619    | 13735    | **Statistically Equivalent** | 1.01×  |
| **`clone()`**                 | 125162   | 195211   |            **V3**            | 1.56×  |
| **`constructor()`**           | 44       | 40       | **Statistically Equivalent** | 1.10×  |
| **`constructor(Collection)`** | 97070    | 197882   |            **V3**            | 2.04×  |
| **`contains(Object)`**        | 41       | 100      |            **V3**            | 2.44×  |
| **`containsAll(Collection)`** | 5207     | 16922    |            **V3**            | 3.25×  |
| **`equals(Object)`**          | 251750   | 452081   |            **V3**            | 1.80×  |
| **`hashCode()`**              | 49862    | 64098    |            **V3**            | 1.29×  |
| **`isEmpty()`**               | 37       | 42       | **Statistically Equivalent** | 1.14×  |
| **`iterator()`**              | 64573    | 100736   |            **V3**            | 1.56×  |
| **`remove(Object)`**          | 44       | 66       |            **V3**            | 1.50×  |
| **`removeAll(Collection)`**   | 17551    | 35869    |            **V3**            | 2.04×  |
| **`retainAll(Collection)`**   | 89072297 | 93187198 | **Statistically Equivalent** | 1.05×  |
| **`size()`**                  | 52       | 38       | **Statistically Equivalent** | 1.37×  |
| **`toArray()`**               | 55145    | 56113    | **Statistically Equivalent** | 1.02×  |
| **`toArray(T[])`**            | 67277    | 65604    | **Statistically Equivalent** | 1.03×  |
| **`toString()`**              | 473965   | 467994   | **Statistically Equivalent** | 1.01×  |

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
![Combined Performance Charts](PerformanceTesting/V1_V2_V3_JDK/contains.png)
![Combined Performance Charts](PerformanceTesting/V1_V2_V3_JDK/containsAll.png)
![Combined Performance Charts](PerformanceTesting/V1_V2_V3_JDK/equals.png)
![Combined Performance Charts](PerformanceTesting/V1_V2_V3_JDK/hashCode.png)
![Combined Performance Charts](PerformanceTesting/V1_V2_V3_JDK/isEmpty.png)
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
![Combined Performance Charts](PerformanceTesting/V3_JDK/contains.png)
![Combined Performance Charts](PerformanceTesting/V3_JDK/containsAll.png)
![Combined Performance Charts](PerformanceTesting/V3_JDK/equals.png)
![Combined Performance Charts](PerformanceTesting/V3_JDK/hashCode.png)
![Combined Performance Charts](PerformanceTesting/V3_JDK/isEmpty.png)
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
![Combined Performance Charts](PerformanceTesting/V2_V3/contains.png)
![Combined Performance Charts](PerformanceTesting/V2_V3/containsAll.png)
![Combined Performance Charts](PerformanceTesting/V2_V3/equals.png)
![Combined Performance Charts](PerformanceTesting/V2_V3/hashCode.png)
![Combined Performance Charts](PerformanceTesting/V2_V3/isEmpty.png)
![Combined Performance Charts](PerformanceTesting/V2_V3/remove.png)
![Combined Performance Charts](PerformanceTesting/V2_V3/removeAll.png)
![Combined Performance Charts](PerformanceTesting/V2_V3/retainAll.png)
![Combined Performance Charts](PerformanceTesting/V2_V3/size.png)
![Combined Performance Charts](PerformanceTesting/V2_V3/toArray.png)
![Combined Performance Charts](PerformanceTesting/V2_V3/toArrayT.png)
![Combined Performance Charts](PerformanceTesting/V2_V3/toString.png)
