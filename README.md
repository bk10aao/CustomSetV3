
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
| **`add(E)`**                  | 138442   | 229505   |            **V3**            | 1.66×  |
| **`addAll(Collection)`**      | 120156   | 264353   |            **V3**            | 2.20×  |
| **`clear()`**                 | 13478    | 14886    | **Statistically Equivalent** | 1.10×  |
| **`clone()`**                 | 126077   | 227001   |            **V3**            | 1.80×  |
| **`constructor()`**           | 40       | 49       |            **V3**            | 1.23×  |
| **`constructor(Collection)`** | 97922    | 198857   |            **V3**            | 2.03×  |
| **`contains(Object)`**        | 38       | 65       |            **V3**            | 1.71×  |
| **`containsAll(Collection)`** | 4665     | 15827    |            **V3**            | 3.39×  |
| **`equals(Object)`**          | 232301   | 693256   |            **V3**            | 2.98×  |
| **`hashCode()`**              | 49512    | 140641   |            **V3**            | 2.84×  |
| **`isEmpty()`**               | 34       | 71       |            **V3**            | 2.09×  |
| **`iterator()`**              | 59463    | 181739   |            **V3**            | 3.06×  |
| **`remove(Object)`**          | 39       | 117      |            **V3**            | 3.00×  |
| **`removeAll(Collection)`**   | 16968    | 39977    |            **V3**            | 2.36×  |
| **`retainAll(Collection)`**   | 89488828 | 93672223 | **Statistically Equivalent** | 1.05×  |
| **`size()`**                  | 34       | 40       |            **V3**            | 1.18×  |
| **`toArray()`**               | 54858    | 61205    | **Statistically Equivalent** | 1.12×  |
| **`toArray(T[])`**            | 61537    | 86786    |            **V3**            | 1.41×  |
| **`toString()`**              | 475179   | 482031   | **Statistically Equivalent** | 1.01×  |

### V3 vs V2

| Method                        | V3 (ns)  | V2 (ns)  |            Winner            | Margin |
|:------------------------------|:---------|:---------|:----------------------------:|:------:|
| **`add(E)`**                  | 138442   | 233181   |            **V3**            | 1.68×  |
| **`addAll(Collection)`**      | 120156   | 255127   |            **V3**            | 2.12×  |
| **`clear()`**                 | 13478    | 13735    | **Statistically Equivalent** | 1.02×  |
| **`clone()`**                 | 126077   | 195211   |            **V3**            | 1.55×  |
| **`constructor()`**           | 40       | 39       | **Statistically Equivalent** | 1.03×  |
| **`constructor(Collection)`** | 97922    | 197881   |            **V3**            | 2.02×  |
| **`contains(Object)`**        | 38       | 100      |            **V3**            | 2.63×  |
| **`containsAll(Collection)`** | 4665     | 16921    |            **V3**            | 3.63×  |
| **`equals(Object)`**          | 232301   | 452081   |            **V3**            | 1.95×  |
| **`hashCode()`**              | 49512    | 64098    |            **V3**            | 1.29×  |
| **`isEmpty()`**               | 34       | 41       |            **V3**            | 1.21×  |
| **`iterator()`**              | 59463    | 100736   |            **V3**            | 1.69×  |
| **`remove(Object)`**          | 39       | 65       |            **V3**            | 1.67×  |
| **`removeAll(Collection)`**   | 16968    | 35868    |            **V3**            | 2.11×  |
| **`retainAll(Collection)`**   | 89488828 | 93187198 | **Statistically Equivalent** | 1.04×  |
| **`size()`**                  | 34       | 37       | **Statistically Equivalent** | 1.09×  |
| **`toArray()`**               | 54858    | 56113    | **Statistically Equivalent** | 1.02×  |
| **`toArray(T[])`**            | 61537    | 65604    | **Statistically Equivalent** | 1.07×  |
| **`toString()`**              | 475179   | 467993   | **Statistically Equivalent** | 1.02×  |

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
