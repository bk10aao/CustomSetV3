package customset;

import customset.CustomSet.CustomSetSpliterator;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Spliterator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CustomSetTest {

    @Test
    public void onCreatingSetWitNegativeSize_and_loadFactorOf_50_throws_IllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new CustomSet<>(-10, 0.5f));
    }

    @Test
    public void onCreatingSetWithSize_10_and_loadFactorOf_zero_throws_IllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new CustomSet<>(10, 0));
    }

    @Test
    public void onCreatingSetWithSize_10_and_loadFactorOf_zero_point_five_constructsCorrectly() {
        CustomSet<Integer> customSet = new CustomSet<>(10, 0.5f);
        assertTrue(customSet.isEmpty());
    }

    @Test
    public void onCreatingSetWithNegativeSize_and_negativeLoadFactor_throws_IllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new CustomSet<>(-10, -10));
    }

    @Test
    public void onCreatingSetWithSizeOf_50_and_negativeLoadFactor_throws_IllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new CustomSet<>(50, -10));
    }

    @Test
    public void onConstructingSet_withSizeLessThan_0_throws_IllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new CustomSet<>(-1));
    }

    @Test
    public void onConstructingSet_withCollectionOfNull_throws_NullPointerException() {
        assertThrows(NullPointerException.class, () -> new CustomSet<>(null));
    }

    @Test
    public void onConstructingSet_with_10_constructsCorrectly() {
        CustomSet<Integer> customSet =  new CustomSet<>(10);
        assertEquals(0, customSet.size());
    }

    @Test
    public void onConstructingSet_returnsEmptySet() {
        CustomSet<Integer> customSet = new CustomSet<>();
        assertTrue(customSet.isEmpty());
    }

    @Test
    public void onConstructingSet_withInitialCapacityOf_10_andLoadFactor_NaN_throws_IllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new CustomSet<Integer>(10, Float.NaN));
    }

    @Test
    public void onConstructingSetWithOneItem_on_isEmpty_returnsFalse() {
        CustomSet<Integer> customSet = new CustomSet<>();
        assertTrue(customSet.add(10));
        assertTrue(customSet.contains(10));
        assertFalse(customSet.isEmpty());
    }

    @Test
    public void onConstructingSet_withCollectionOfFiveItems_sizeOf_5() {
        Collection<Integer> collection = IntStream.iterate(10, i -> i <= 50, i -> i + 10).boxed().collect(Collectors.toList());
        CustomSet<Integer> customSet = new CustomSet<>(collection);
        assertEquals(5, customSet.size());
        assertTrue(customSet.contains(10));
        assertTrue(customSet.contains(20));
        assertTrue(customSet.contains(30));
        assertTrue(customSet.contains(40));
        assertTrue(customSet.contains(50));
        assertFalse(customSet.contains(100));
    }

    @Test
    public void onAddingToSet_null_returns_true_and_sizeOf_1() {
        CustomSet<Integer> customSet = new CustomSet<>();
        assertTrue(customSet.add(null));
        assertEquals(1, customSet.size());
    }

    @Test
    public void onAddingToSet_null_twice_returns_false_and_sizeOf_1() {
        CustomSet<Integer> customSet = new CustomSet<>();
        assertTrue(customSet.add(null));
        assertEquals(1, customSet.size());
        assertFalse(customSet.add(null));
        assertEquals(1, customSet.size());
    }

    @Test
    public void onAddingToSet_10_returns_true_and_sizeOf_1() {
        CustomSet<Integer> customSet = new CustomSet<>();
        assertTrue(customSet.add(10));
        assertEquals(1, customSet.size());
    }

    @Test
    public void givenSetContainingOf_1_2_3_returnsFalse_onContainsNull() {
        CustomSet<Integer> customSet = new CustomSet<>(List.of(1, 2, 3));
        assertFalse(customSet.contains(null));
    }

    @Test
    public void givenSetContaining_null_returnsTrue_onContainsNull() {
        CustomSet<Integer> customSet = new CustomSet<>();
        customSet.add(null);
        assertTrue(customSet.contains(null));
    }


    @Test
    public void onAddingToSet_10_20_returns_true_and_sizeOf_2() {
        CustomSet<Integer> customSet = createDynamicSet(2);
        assertEquals(2, customSet.size());
    }

    @Test
    public void onAddingToSet_10_items_returns_true_andSizeOf_10() {
        CustomSet<Integer> customSet = createDynamicSet(10);
        assertEquals(10, customSet.size());
    }

    @Test
    public void onAddingToSet_twoIdenticalNumbersToSet_onlyAddsOne() {
        CustomSet<Integer> customSet = new CustomSet<>();
        assertTrue(customSet.add(10));
        assertEquals(1, customSet.size());
        assertFalse(customSet.add(10));
        assertEquals(1, customSet.size());
    }

    @Test
    public void onAddingToSet_50_items_returns_true_andSizeOf_50() {
        CustomSet<Integer> customSet = createDynamicSet(50);
        assertEquals(50, customSet.size());
    }

    @Test
    public void onAddingToSet_twoSameValues_returns_sizeOf_1() {
        CustomSet<Integer> customSet = new CustomSet<>();
        assertTrue(customSet.add(1));
        assertFalse(customSet.add(1));
        assertEquals(1, customSet.size());
    }

    @Test
    public void onAddingToSet_twoSameValues_andOneUnique_returns_sizeOf_2() {
        CustomSet<Integer> customSet = new CustomSet<>(List.of(1, 1, 2));
        assertTrue(customSet.contains(1));
        assertTrue(customSet.contains(2));
        assertEquals(2, customSet.size());
    }

    @Test
    public void onAddingToSet_twoValuesOf_10_20_onRemove_30_returns_false() {
        CustomSet<Integer> customSet = new CustomSet<>(List.of(10, 20));
        assertEquals(2, customSet.size());
        assertFalse(customSet.remove(30));
    }

    @Test
    public void onAddingToSet_10_20_onRemove_null_returns_false() {
        CustomSet<Integer> customSet = new CustomSet<>(List.of(10, 20));
        assertEquals(2, customSet.size());
        assertFalse(customSet.remove(null));
    }

    @Test
    public void onAddingToSet_10_null_20_onRemove_null_returns_false() {
        CustomSet<Integer> customSet = new CustomSet<>();
        customSet.add(10);
        customSet.add(null);
        customSet.add(20);
        assertEquals(3, customSet.size());
        assertTrue(customSet.remove(null));
        assertEquals(2, customSet.size());
    }

    @Test
    public void onAddingToSet_twoValuesOf_10_20_onRemove_10_returns_true() {
        CustomSet<Integer> customSet = new CustomSet<>(List.of(10, 20));
        assertEquals(2, customSet.size());
        assertTrue(customSet.remove(10));
        assertEquals(1, customSet.size());
    }

    @Test
    public void givenNonEmptySet_onRetainAllWitNullCollection_throwsNullPointerException() {
        CustomSet<Integer> customSet = createDynamicSet(5);
        assertThrows(NullPointerException.class, () -> customSet.retainAll(null));
    }

    @Test
    public void givenNonEmptySet_onRetainAllWithEmptyCollection_clearsSet() {
        CustomSet<Integer> customSet = createDynamicSet(5);
        Collection<Integer> empty = new ArrayList<>();
        assertTrue(customSet.retainAll(empty));
        assertTrue(customSet.isEmpty());
    }

    @Test
    public void givenSetOfValue_10_20_30_40_50_onRetainAllForCollection_20_30_returnsSetOf_20_30() {
        CustomSet<Integer> customSet = new CustomSet<>(List.of(10, 20, 30, 40, 50));
        Collection<Integer> c = new ArrayList<>();
        c.add(20);
        c.add(30);
        assertTrue(customSet.retainAll(c));
        assertFalse(customSet.contains(10));
        assertFalse(customSet.contains(40));
        assertFalse(customSet.contains(50));
        assertTrue(customSet.contains(20));
        assertTrue(customSet.contains(30));
        assertEquals(2, customSet.size());
    }

    @Test
    public void givenSetOfValue_10_20_30_40_50_onRetailAllForCollectionContainingValueThatDoesNotExist_returnsFalse() {
        CustomSet<Integer> customSet = new CustomSet<>(List.of(10, 20, 30, 40, 50));
        Collection<Integer> c = new ArrayList<>();
        c.add(60);
        assertTrue(customSet.retainAll(c));
        assertEquals(0, customSet.size());
    }

    @Test
    public void givenSetOfValue_10_20_30_40_50_onRetailAllForCollection_20_30_60_returnsSetOf_20_30() {
        CustomSet<Integer> customSet = new CustomSet<>(List.of(10, 20, 30, 40, 50));
        Collection<Integer> c = new ArrayList<>();
        c.add(20);
        c.add(30);
        c.add(60);
        assertTrue(customSet.retainAll(c));
        assertFalse(customSet.contains(10));
        assertFalse(customSet.contains(40));
        assertFalse(customSet.contains(50));
        assertTrue(customSet.contains(20));
        assertTrue(customSet.contains(30));
        assertEquals(2, customSet.size());
    }

    @Test
    public void dummy() {
        CustomSet<Integer> customSet = new CustomSet<>(List.of(10, 20, 30));
        assertThrows(NullPointerException.class, () -> customSet.containsAll(null));
    }

    @Test
    public void givenSetOfValue_10_20_30_onContainsAllForCollection_20_30_40_returns_false() {
        CustomSet<Integer> customSet = new CustomSet<>(List.of(10, 20, 30));
        Collection<Integer> c = new ArrayList<>();
        c.add(20);
        c.add(30);
        c.add(40);
        assertFalse(customSet.containsAll(c));
    }

    @Test
    public void givenSetOfValue_10_20_30_onRemoveAll_withNullCollection_throwsNullPointerException() {
        CustomSet<Integer> customSet = new CustomSet<>(List.of(10, 20, 30));
        assertThrows(NullPointerException.class, () -> customSet.removeAll(null));
    }

    @Test
    public void givenSetOfValue_10_20_30_40_50_onRemoveAllForCollection_20_30_returnsCollectionOf_10_40_50() {
        CustomSet<Integer> customSet = new CustomSet<>(List.of(10, 20, 30, 40, 50));
        Collection<Integer> c = new ArrayList<>();
        c.add(20);
        c.add(30);
        assertTrue(customSet.removeAll(c));
        assertTrue(customSet.contains(10));
        assertFalse(customSet.contains(20));
        assertFalse(customSet.contains(30));
        assertTrue(customSet.contains(40));
        assertTrue(customSet.contains(50));
    }

    @Test
    public void givenSetOfValue_10_20_30_onContainsAllForCollection_20_30_returns_true() {
        CustomSet<Integer> customSet = new CustomSet<>(List.of(10, 20, 30));
        Collection<Integer> c = new ArrayList<>();
        c.add(20);
        c.add(30);
        assertTrue(customSet.containsAll(c));
    }

    @Test
    public void givenSet_onAddingAllEmptyCollection_returnsFalse() {
        CustomSet<Integer> customSet = new CustomSet<>(List.of(10, 20, 30));
        assertFalse(customSet.addAll(List.of()));
    }

    @Test
    public void givenSetOfValue_10_20_30_onAddAllForCollectionOf_10_20_returns_false() {
        CustomSet<Integer> customSet = new CustomSet<>(List.of(10, 20, 30));
        Collection<Integer> c = new ArrayList<>();
        c.add(10);
        c.add(20);
        assertFalse(customSet.addAll(c));
    }

    @Test
    public void givenSetOfValue_10_20_30_onAddAllForCollectionOf_40_50_returns_true() {
        CustomSet<Integer> customSet = new CustomSet<>(List.of(10, 20, 30));
        Collection<Integer> c = new ArrayList<>();
        c.add(40);
        c.add(50);
        assertTrue(customSet.addAll(c));
    }

    @Test
    public void onAddingToSet_50_items_andClearingSet_returns_newSet() {
        CustomSet<Integer> customSet = createDynamicSet(50);
        assertEquals(50, customSet.size());
        customSet.clear();
        assertTrue(customSet.isEmpty());
    }

    @Test
    public void onConstructingEmptySet_returnsEmptyCurlyBracket_on_toString() {
        CustomSet<Integer> customSet = new CustomSet<>();
        assertEquals("{}", customSet.toString());
    }

    @Test
    public void onConstructingSet_withCollectionOfFiveItems_returnsCorrect_toString() {
        Collection<Integer> collection = IntStream.iterate(0, i -> i <= 50, i -> i + 10).boxed().collect(Collectors.toList());
        CustomSet<Integer> customSet = new CustomSet<>(collection);
        String setAsString = customSet.toString();
        assertTrue(setAsString.contains("10"));
        assertTrue(setAsString.contains("20"));
        assertTrue(setAsString.contains("30"));
        assertTrue(setAsString.contains("40"));
        assertTrue(setAsString.contains("50"));
        String pattern = "\\{[0-9]+(, [0-9]+)+}";
        assertTrue(setAsString.matches(pattern));
    }

    @Test
    public void givenEmptySet_onToArray_returns_emptyArray() {
        CustomSet<String> customSet = new CustomSet<>();
        Object[] values = customSet.toArray();
        Object[] expected = new Object[] { };
        assertEquals(0, values.length);
        assertArrayEquals(expected, values);
    }

    @Test
    public void onConstructingSet_withCollectionOfFiveItems_returnsCorrectArray() {
        CustomSet<String> customSet = new CustomSet<>(List.of("10", "20", "30"));
        Object[] values = customSet.toArray();
        Arrays.sort(values);
        Object[] expected = new Object[] { "10", "20", "30" };
        assertEquals(3, values.length);
        assertArrayEquals(expected, values);
    }

    @Test
    public void onAdding_1_000_000_values_causesNoIndexOverwritingIssues_andAdds_1_000_000_values() {
        CustomSet<Long> customSet = new CustomSet<>();
        LongStream.range(0, 1_000_000).forEach(customSet::add);
        assertEquals(1_000_000, customSet.size());
    }

    @Test
    public void onAdding_2_000_000_values_causesNoIndexOverwritingIssues_andAdds_2_000_000_values() {
        CustomSet<Long> customSet = new CustomSet<>();
        LongStream.range(0, 2_000_000).forEach(customSet::add);
        assertEquals(2_000_000, customSet.size());
    }

    @Test
    public void onAdding_3_000_000_values_causesNoIndexOverwritingIssues_andAdds_3_000_000_values() {
        CustomSet<Long> customSet = new CustomSet<>();
        LongStream.range(0, 3_000_000).forEach(customSet::add);
        assertEquals(3_000_000, customSet.size());
    }

    @Test
    public void givenSetOfThreeItems_iterator_iteratesOverAllElements_andReturnsSizeOf_3() {
        CustomSet<Integer> customSet = new CustomSet<>();
        customSet.add(1);
        customSet.add(2);
        customSet.add(3);
        int count = 0;
        Iterator<Integer> iterator = customSet.iterator();
        while (iterator.hasNext()) {
            iterator.next();
            count++;
        }
        assertEquals(3, count);
    }

    @Test
    public void givenSetOfThreeItems_iterator_remove_removesCurrentElement() {
        CustomSet<Integer> customSet = new CustomSet<>();
        customSet.add(1);
        customSet.add(2);
        customSet.add(3);
        Iterator<Integer> iterator = customSet.iterator();
        while (iterator.hasNext())
            if (iterator.next().equals(2))
                iterator.remove();
        assertFalse(customSet.contains(2));
        assertEquals(2, customSet.size());
    }

    @Test
    public void givenSetOf_100_items_spliterator_estimateSize_matchesSetSize() {
        CustomSet<Integer> customSet = createDynamicSet(100);
        assertEquals(100, customSet.spliterator().estimateSize());
    }

    @Test
    public void givenSameSet_onEquals_returns_true() {
        CustomSet<Integer> a = new CustomSet<>(List.of(1, 2));
        assertEquals(a, a);
    }

    @Test
    public void givenSetsOfDifferentSizes_onEquals_returns_false() {
        CustomSet<Integer> a = new CustomSet<>(List.of(1, 2));
        CustomSet<Integer> b = new CustomSet<>(List.of(1, 2, 3));
        assertNotEquals(a, b);
    }

    @Test
    public void givenTwoIdenticalSet_onEquals_returnsTrue() {
        CustomSet<Integer> a = new CustomSet<>();
        a.add(1);
        a.add(2);
        CustomSet<Integer> b = new CustomSet<>();
        b.add(1);
        b.add(2);
        assertEquals(a, b);
    }

    @Test
    public void givenTwoDifferentSets_onEquals_returnsFalse() {
        CustomSet<Integer> a = new CustomSet<>();
        a.add(1);
        a.add(2);
        CustomSet<Integer> b = new CustomSet<>();
        b.add(1);
        b.add(3);
        assertNotEquals(a, b);
    }

    @Test
    public void givenCustomSetOf_1_2_3_onEqualsNonMatchingObject_returns_false() {
        CustomSet<Integer> customSet = new CustomSet<>(List.of(1, 2, 3));
        assertNotEquals(customSet, new ArrayList<>());
    }

    @Test
    public void givenTwoIdenticalSets_hashCode_IsTheSame() {
        CustomSet<Integer> a = new CustomSet<>();
        a.add(1);
        a.add(2);
        CustomSet<Integer> b = new CustomSet<>();
        b.add(1);
        b.add(2);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void givenTwoDifferentSets_hashCode_IsDifferent() {
        CustomSet<Integer> a = new CustomSet<>();
        a.add(1);
        a.add(2);
        CustomSet<Integer> b = new CustomSet<>();
        b.add(1);
        b.add(3);
        assertNotEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void givenSetOfType_Integer_withValues_1_2_3_onToArray_withIntArrayParameter_returnsCorrectArray() {
        CustomSet<Integer> customSet = new CustomSet<>();
        customSet.add(1);
        customSet.add(2);
        customSet.add(3);
        assertArrayEquals(new Integer[] {1, 2, 3}, customSet.toArray(new Integer[0]));
    }

    @Test
    public void givenSetOfType_Integer_withValues_1_2_3_onToArray_withIntArrayParameterP_withSize_2_returnsCorrectArray() {
        CustomSet<Integer> customSet = new CustomSet<>();
        customSet.add(1);
        customSet.add(2);
        customSet.add(3);
        assertArrayEquals(new Integer[] {1, 2, 3}, customSet.toArray(new Integer[2]));
    }

    @Test
    public void givenSetOfType_Integer_withValues_1_2_3_onToArray_withIntArrayParameterP_withSize_5_returnsCorrectArray() {
        CustomSet<Integer> customSet = new CustomSet<>();
        customSet.add(1);
        customSet.add(2);
        customSet.add(3);
        assertArrayEquals(new Integer[] {1, 2, 3, null, null}, customSet.toArray(new Integer[5]));
    }

    @Test
    public void givenSetOfType_Integer_withValues_1_2_3_onClone_returnsEqualSets() {
        CustomSet<Integer> customSet = new CustomSet<>();
        customSet.add(1);
        customSet.add(2);
        customSet.add(3);
        CustomSet<Integer> clone = customSet.clone();
        assertEquals(clone, customSet);
    }

    private static CustomSet<Integer> createDynamicSet(int x) {
        CustomSet<Integer> customSet = new CustomSet<>();
        Random random = new Random();
        for (int i = 0; i < x; i++)
            customSet.add(random.nextInt());
        return customSet;
    }

    @Test
    void testTreeifyBinAndTreeNodeInsertion() {
        CustomSet<CollidingKey> set = new CustomSet<>(64);
        for (int i = 0; i < 10; i++) {
            assertTrue(set.add(new CollidingKey(i)), "Should successfully add unique colliding key " + i);
        }

        assertEquals(10, set.size());
        assertFalse(set.add(new CollidingKey(5)), "Adding duplicate element to tree bin should return false");
        assertEquals(10, set.size());
        assertTrue(set.add(new CollidingKey(100)), "Adding new element to tree bin should return true");
        assertEquals(11, set.size());
    }

    @Test
    void testTreeNodeContainsAndRemoval() {
        CustomSet<CollidingKey> set = new CustomSet<>(64);

        for (int i = 0; i < 12; i++) {
            set.add(new CollidingKey(i));
        }
        assertTrue(set.contains(new CollidingKey(5)));
        assertFalse(set.contains(new CollidingKey(999)));
        assertTrue(set.remove(new CollidingKey(5)), "Should successfully remove element from tree bin");
        assertFalse(set.contains(new CollidingKey(5)));
        assertEquals(11, set.size());
    }

    @Test
    void testTreeNodeSplitAndResizing() {
        CustomSet<CollidingKey> set = new CustomSet<>(64);
        for (int i = 0; i < 50; i++) {
            set.add(new CollidingKey(i));
        }
        for (int i = 50; i < 80; i++) {
            set.add(new CollidingKey(i));
        }

        assertEquals(80, set.size());
        for (int i = 0; i < 80; i++) {
            assertTrue(set.contains(new CollidingKey(i)), "Set should contain key " + i);
        }
    }

    @Test
    void testRemoveAllWhenCollectionLargerThanSet() {
        CustomSet<String> set = new CustomSet<>();
        set.add("apple");
        set.add("banana");
        java.util.List<String> largeCollection = java.util.Arrays.asList("apple", "banana", "cherry", "date");

        boolean modified = set.removeAll(largeCollection);

        assertTrue(modified, "Set should be modified after removing elements");
        assertTrue(set.isEmpty(), "Set should be completely empty after removing all elements");
    }

    static class CollidingKey implements Comparable<CollidingKey> {
        private final int id;

        CollidingKey(int id) {
            this.id = id;
        }

        @Override
        public int hashCode() {
            return 42;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof CollidingKey)) return false;
            return this.id == ((CollidingKey) o).id;
        }

        @Override
        public int compareTo(CollidingKey o) {
            return Integer.compare(this.id, o.id);
        }
    }

    @Test
    void spliterator_estimateSize_matchesSetSize() {
        CustomSet<Integer> set = createDynamicSet(100);
        assertEquals(100, set.spliterator().estimateSize());
    }

    @Test
    void spliterator_characteristics_areCorrect() {
        CustomSet<Integer> set = new CustomSet<>();
        int characteristics = set.spliterator().characteristics();

        assertTrue((characteristics & Spliterator.DISTINCT) != 0);
        assertTrue((characteristics & Spliterator.SIZED) != 0);
        // SUBSIZED is deliberately not reported: trySplit halves the estimate, so child sizes are approximate
        assertEquals(0, characteristics & Spliterator.SUBSIZED);
    }

    @Test
    void spliterator_tryAdvance_visitsAllElements() {
        CustomSet<Integer> set = new CustomSet<>(List.of(10, 20, 30, 40, 50));
        Spliterator<Integer> spliterator = set.spliterator();

        Set<Integer> seen = new HashSet<>();
        while (spliterator.tryAdvance(seen::add)) { }

        assertEquals(5, seen.size());
        assertTrue(seen.containsAll(List.of(10, 20, 30, 40, 50)));
        assertEquals(0, spliterator.estimateSize());
    }

    @Test
    void spliterator_forEachRemaining_visitsAllElements() {
        CustomSet<Integer> set = new CustomSet<>(List.of(1, 2, 3, 4, 5));
        Spliterator<Integer> spliterator = set.spliterator();

        List<Integer> collected = new ArrayList<>();
        spliterator.forEachRemaining(collected::add);

        assertEquals(5, collected.size());
        assertTrue(collected.containsAll(List.of(1, 2, 3, 4, 5)));
    }

    @Test
    void spliterator_handlesNullElement() {
        CustomSet<Integer> set = new CustomSet<>();
        set.add(null);
        set.add(10);
        set.add(20);

        Spliterator<Integer> spliterator = set.spliterator();
        List<Integer> collected = new ArrayList<>();
        spliterator.forEachRemaining(collected::add);

        assertEquals(3, collected.size());
        assertTrue(collected.contains(null));
        assertTrue(collected.contains(10));
        assertTrue(collected.contains(20));
    }

    @Test
    void spliterator_trySplit_producesValidSplit() {
        CustomSet<Integer> set = createDynamicSet(100);
        Spliterator<Integer> original = set.spliterator();

        Spliterator<Integer> split = original.trySplit();
        assertNotNull(split);

        long originalEst = original.estimateSize();
        long splitEst = split.estimateSize();

        assertTrue(originalEst >= 0);
        assertTrue(splitEst >= 0);
        assertTrue(originalEst + splitEst <= 100 + 5);
    }

    @Test
    void spliterator_trySplit_onSmallSet_returnsNull() {
        CustomSet<Integer> set = new CustomSet<>(List.of(1));
        Spliterator<Integer> spliterator = set.spliterator();
        assertNull(spliterator.trySplit());
    }

    @Test
    void spliterator_trySplit_thenAdvance_coversAllElements() {
        CustomSet<Integer> set = createDynamicSet(50);
        Spliterator<Integer> left = set.spliterator();
        Spliterator<Integer> right = left.trySplit();

        HashSet<Integer> all = new HashSet<>();

        if (right != null) {
            right.forEachRemaining(all::add);
        }
        left.forEachRemaining(all::add);

        assertEquals(50, all.size());
    }

    @Test
    void spliterator_isFailFast_onStructuralModification() {
        CustomSet<Integer> set = new CustomSet<>(List.of(1, 2, 3, 4, 5));
        Spliterator<Integer> spliterator = set.spliterator();
        set.add(99);
        assertThrows(ConcurrentModificationException.class, () -> spliterator.tryAdvance(e -> {}));
    }

    @Test
    void spliterator_isFailFast_onClear() {
        CustomSet<Integer> set = new CustomSet<>(List.of(1, 2, 3));
        Spliterator<Integer> spliterator = set.spliterator();

        set.clear();

        assertThrows(ConcurrentModificationException.class,
                () -> spliterator.forEachRemaining(e -> {}));
    }

    @Test
    void spliterator_withCollidingKeys_stillWorks() {
        CustomSet<CollidingKey> set = new CustomSet<>(64);
        for (int i = 0; i < 20; i++) {
            set.add(new CollidingKey(i));
        }

        Spliterator<CollidingKey> spliterator = set.spliterator();
        HashSet<CollidingKey> seen = new HashSet<>();

        spliterator.forEachRemaining(seen::add);

        assertEquals(20, seen.size());
    }

    @Test
    void spliterator_trySplit_multipleTimes() {
        CustomSet<Integer> set = createDynamicSet(200);
        Spliterator<Integer> s1 = set.spliterator();

        Spliterator<Integer> s2 = s1.trySplit();
        Spliterator<Integer> s3 = s1.trySplit();

        Set<Integer> all = new HashSet<>();

        if (s2 != null)
            s2.forEachRemaining(all::add);
        if (s3 != null)
            s3.forEachRemaining(all::add);
        s1.forEachRemaining(all::add);
        assertEquals(200, all.size());
    }

    @Test
    void spliterator_trySplit_intraChain_splitsSuccessfully() {
        CustomSet<String> set = new CustomSet<>(4);
        for (int i = 0; i < 20; i++) {
            set.add("Key-" + i);
        }

        Spliterator<String> spliterator = set.spliterator();
        Spliterator<String> prefix = spliterator.trySplit();

        assertNotNull(prefix, "Spliterator should successfully split");

        Set<String> collected = new HashSet<>();
        prefix.forEachRemaining(collected::add);
        spliterator.forEachRemaining(collected::add);

        assertEquals(20, collected.size());
    }

    @Test
    void spliterator_trySplit_neverSplitsInsideASingleBucket() {
        class CollidingKey implements Comparable<CollidingKey> {
            private final String name;
            CollidingKey(String name) { this.name = name; }
            @Override public int hashCode() { return 0; }
            @Override public boolean equals(Object o) {
                return o instanceof CollidingKey && ((CollidingKey) o).name.equals(name);
            }
            @Override public int compareTo(CollidingKey o) { return name.compareTo(o.name); }
        }

        CustomSet<CollidingKey> set = new CustomSet<>(16);
        for (String n : List.of("A", "B", "C", "D", "E"))
            set.add(new CollidingKey(n));

        CustomSetSpliterator<CollidingKey> spliterator =
                new CustomSetSpliterator<>(set, 0, 1, 5, set.size(), false);

        assertNull(spliterator.trySplit(), "A single bucket is never split (same as the JDK's HashMap spliterators)");

        List<CollidingKey> seen = new ArrayList<>();          // a List, so duplicates would be caught
        spliterator.forEachRemaining(seen::add);
        assertEquals(5, seen.size());
    }

    @Test
    void spliterator_trySplit_visitsEveryElementExactlyOnce() {
        CustomSet<Integer> set = createDynamicSet(1000);
        set.add(null);

        List<Spliterator<Integer>> parts = new ArrayList<>();
        parts.add(set.spliterator());
        for (int round = 0; round < 4; round++)
            for (Spliterator<Integer> sp : new ArrayList<>(parts)) {
                Spliterator<Integer> half = sp.trySplit();
                if (half != null)
                    parts.add(half);
            }

        List<Integer> seen = new ArrayList<>();               // a List, so duplicates would be caught
        for (Spliterator<Integer> sp : parts)
            sp.forEachRemaining(seen::add);

        assertTrue(parts.size() > 1, "expected the set to actually be split");
        assertEquals(set.size(), seen.size());
        assertEquals(set.size(), new HashSet<>(seen).size());
    }

    @Test
    void treeBin_untreeifies_whenElementsRemovedBelowThreshold() {
        CustomSet<CollidingKey> set = new CustomSet<>(64);
        for (int i = 0; i < 12; i++)
            set.add(new CollidingKey(i));
        assertEquals(12, set.size());
        for (int i = 0; i < 7; i++) {
            CollidingKey k = new CollidingKey(i);
            System.out.println(k + " " + i);
            assertTrue(set.remove(k));
        }

        assertEquals(5, set.size());
        for (int i = 7; i < 12; i++) {
            CollidingKey k = new CollidingKey(i);
            System.out.println(k + " " + i);
            assertTrue(set.contains(k));
        }
    }

    @Test
    void treeBin_removeRootAndInternalNodes() {
        CustomSet<CollidingKey> set = new CustomSet<>(64);
        for (int i = 0; i < 15; i++)
            set.add(new CollidingKey(i));
        assertTrue(set.remove(new CollidingKey(0)));
        assertTrue(set.remove(new CollidingKey(7)));
        assertTrue(set.remove(new CollidingKey(14)));
        assertEquals(12, set.size());
    }

    @Test
    void treeBin_iterator_and_spliterator_work_on_tree() {
        CustomSet<CollidingKey> set = new CustomSet<>(64);
        for (int i = 0; i < 20; i++)
            set.add(new CollidingKey(i));
        int count = 0;
        for (CollidingKey ignored : set)
            count++;
        assertEquals(20, count);
        Set<CollidingKey> seen = new HashSet<>();
        set.spliterator().forEachRemaining(seen::add);
        assertEquals(20, seen.size());
    }

    @Test
    void concurrentModification_onIterator_throws() {
        CustomSet<Integer> set = new CustomSet<>(List.of(1, 2, 3, 4, 5));
        Iterator<Integer> it = set.iterator();

        it.next();
        set.add(99);

        assertThrows(ConcurrentModificationException.class, it::next);
    }

    @Test
    void concurrentModification_onIterator_remove_isAllowed() {
        CustomSet<Integer> set = new CustomSet<>(List.of(1, 2, 3, 4, 5));
        Iterator<Integer> it = set.iterator();

        while (it.hasNext())
            if (it.next().equals(3))
                it.remove();

        assertFalse(set.contains(3));
        assertEquals(4, set.size());
    }

    @Test
    void largeTree_survives_multiple_resizes() {
        CustomSet<CollidingKey> set = new CustomSet<>(16);

        for (int i = 0; i < 200; i++)
            set.add(new CollidingKey(i));

        assertEquals(200, set.size());

        for (int i = 0; i < 200; i++)
            assertTrue(set.contains(new CollidingKey(i)));

        for (int i = 0; i < 100; i++)
            assertTrue(set.remove(new CollidingKey(i)));

        assertEquals(100, set.size());
    }

    @Test
    void testSerializationRoundTrip() throws Exception {
        CustomSet<String> set = new CustomSet<>();
        set.add("A");
        set.add(null);
        set.add("B");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(set);
        }

        CustomSet<String> deserialized;
        try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(baos.toByteArray()))) {
            deserialized = (CustomSet<String>) ois.readObject();
        }

        assertEquals(set.size(), deserialized.size());
        assertTrue(deserialized.contains("A"));
        assertTrue(deserialized.contains("B"));
        assertTrue(deserialized.contains(null));
    }

    @Test
    void testCloneIndependence() {
        CustomSet<Integer> set = new CustomSet<>(List.of(1, 2, 3));
        CustomSet<Integer> clone = set.clone();

        assertEquals(set, clone);
        clone.add(4);
        assertFalse(set.contains(4), "Modifying clone should not affect original set");
    }

    @Test
    void spliterator_trySplit_onSmallOrNullSpliterator() {
        CustomSet<Integer> set = new CustomSet<>();
        Spliterator<Integer> s1 = set.spliterator();
        assertNull(s1.trySplit(), "Empty set spliterator should return null on trySplit");

        set.add(null);
        Spliterator<Integer> s2 = set.spliterator();
        assertNull(s2.trySplit(), "Spliterator containing only null should return null on trySplit");
    }

    @Test
    void testRotateRightAndRootDeepTraversal() {
        CustomSet<CollidingKey> set = new CustomSet<>(64);
        for (int i = 10; i >= 0; i--)
            set.add(new CollidingKey(i));

        for (int i = 0; i <= 10; i++)
            assertTrue(set.contains(new CollidingKey(i)), "Should find key " + i);

        assertEquals(11, set.size());
    }

    @Test
    void testTreeNodeRootDirectTraversal() {
        CustomSet<CollidingKey> set = new CustomSet<>(64);
        for (int i = 0; i < 15; i++)
            set.add(new CollidingKey(i));
        assertTrue(set.contains(new CollidingKey(10)));
        assertTrue(set.contains(new CollidingKey(2)));
    }
}