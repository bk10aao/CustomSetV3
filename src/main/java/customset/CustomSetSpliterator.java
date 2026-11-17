package customset;

import java.util.ConcurrentModificationException;
import java.util.Spliterator;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

/**
 * A customized {@link Spliterator} implementation for traversing elements of a {@link CustomSet}.
 *
 * <p>{@code CustomSetSpliterator} supports parallel execution, fail-fast concurrency checks,
 * late-binding table initialization, and binary tree/bucket range splitting.
 *
 * @param <E> the type of elements traversed by this spliterator
 */
class CustomSetSpliterator<E> implements Spliterator<E> {

    private final CustomSet<E> set;
    private final int expectedModCount;

    private Node<E> current;
    private Node<E>[] table;
    private long estimatedSize;
    private int fence;
    private int index;
    private boolean includeNull;
    private boolean sized;

    /**
     * Constructs a new {@code CustomSetSpliterator} with specified traversal bounds and configuration.
     *
     * @param set              the backing set
     * @param origin           the lower bound index (inclusive) in the hash table array
     * @param fence            the upper bound index (exclusive) in the hash table array, or {@code -1} to initialize lazily
     * @param estimatedSize    the estimated number of elements in this split range
     * @param expectedModCount the expected modification count
     * @param isRoot           {@code true} if this spliterator is the root instance responsible for null element handling
     */
    public CustomSetSpliterator(final CustomSet<E> set, final int origin, final int fence, final long estimatedSize,
                                final int expectedModCount, final boolean isRoot) {
        this.set = set;
        this.index = origin;
        this.fence = fence;
        this.estimatedSize = estimatedSize;
        this.expectedModCount = expectedModCount;
        this.includeNull = isRoot && set.containsNull;
        this.table = set.table;
        this.sized = isRoot;
    }

    /**
     * Returns a set of characteristics reporting that this spliterator is {@link Spliterator#DISTINCT}
     * and conditionally {@link Spliterator#SIZED}.
     *
     * @return the characteristic flags
     */
    public int characteristics() {
        return (sized ? Spliterator.SIZED : 0) | Spliterator.DISTINCT;
    }

    /**
     * Returns an estimate of the number of elements encountered by a {@link #forEachRemaining} traversal,
     * or returns {@link Long#MAX_VALUE} if infinite, unknown, or too expensive to compute.
     *
     * @return the estimated element count
     */
    public long estimateSize() {
        getFence();
        return Math.max(estimatedSize, 0);
    }

    /**
     * Performs the given action for each remaining element, sequentially in the current thread,
     * until all elements have been processed or the action throws an exception.
     *
     * @param action the action to perform on each element
     * @throws NullPointerException            if the specified action is null
     * @throws ConcurrentModificationException if the backing set was structurally modified concurrently
     */
    public void forEachRemaining(final Consumer<? super E> action) {
        requireNonNull(action, "Action must not be null.");
        checkForComodification();
        if (includeNull) {
            includeNull = false;
            action.accept(null);
            if (estimatedSize > 0)
                estimatedSize--;
        }
        int high = getFence();
        Node<E>[] tab = table;
        if (tab != null) {
            for (Node<E> node = current; node != null; node = node.next)
                action.accept(node.key);
            current = null;
            for (int i = index; i < high; i++)
                for (Node<E> n = tab[i]; n != null; n = n.next)
                    action.accept(n.key);
        }
        index = high;
        estimatedSize = 0;
        checkForComodification();
    }

    /**
     * If a remaining element exists, performs the given action on it, returning {@code true};
     * otherwise returns {@code false}.
     *
     * @param action the action to perform on the next element
     * @return {@code true} if an element was processed; {@code false} if no elements remained
     * @throws NullPointerException            if the specified action is null
     * @throws ConcurrentModificationException if the backing set was structurally modified concurrently
     */
    public boolean tryAdvance(final Consumer<? super E> action) {
        requireNonNull(action, "Action must not be null.");
        checkForComodification();
        int higherIndex = getFence();
        if (includeNull) {
            includeNull = false;
            action.accept(null);
            if (estimatedSize > 0)
                estimatedSize--;
            return true;
        }
        Node<E>[] tab = table;
        if (tab == null)
            return false;
        while (true) {
            if (current != null) {
                E e = current.key;
                current = current.next;
                action.accept(e);
                if (estimatedSize > 0)
                    estimatedSize--;
                return true;
            }
            if (index >= higherIndex)
                return false;
            current = tab[index++];
        }
    }

    /**
     * If this spliterator can be partitioned, returns a {@code Spliterator} covering elements,
     * that will, upon return from this method, not be covered by this spliterator.
     *
     * @return a {@code Spliterator} covering some portion of the elements, or {@code null} if unbreakable
     */
    public Spliterator<E> trySplit() {
        int high = getFence(), low = index, mid = (low + high) >>> 1;
        if (estimatedSize <= 1 || low >= mid || current != null)
            return null;
        long half = estimatedSize >>> 1;
        estimatedSize -= half;
        index = mid;
        sized = false;
        return new CustomSetSpliterator<>(set, low, mid, half, expectedModCount, false);
    }

    private void checkForComodification() {
        if (set.modCount != expectedModCount)
            throw new ConcurrentModificationException();
    }

    private int getFence() {
        int higherIndex;
        if ((higherIndex = fence) < 0) {
            table = set.table;
            estimatedSize = set.size;
            higherIndex = fence = (table == null) ? 0 : table.length;
        }
        return higherIndex;
    }
}