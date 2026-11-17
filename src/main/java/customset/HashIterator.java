package customset;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A fail-fast, hybrid {@link Iterator} implementation for traversing elements of a {@link CustomSet}.
 *
 * <p>{@code HashIterator} performs zero-allocation pointer traversal across standard singly-linked
 * collision chains while capturing a localized key snapshot when stepping into a
 * {@link TreeNode} bin. This hybrid approach preserves $O(1)$ lazy setup across standard
 * buckets while guaranteeing safe, uninterrupted iteration if {@link #remove()} triggers Red-Black
 * tree root rotations or untreeification.
 *
 * @param <E> the type of elements returned by this iterator.
 */
final class HashIterator<E> implements Iterator<E> {

    private final CustomSet<E> set;
    private Node<E> next;
    private Object[] treeBucketKeys;
    private int treeBucketCursor;
    private E lastReturnedKey;
    private int expectedModCount;
    private int index;
    private boolean yieldNull;
    private boolean currentIsNull;
    private boolean canRemove;

    /**
     * Constructs a new {@code HashIterator} positioned at the first available element
     * in the specified set, yielding {@code null} first if present.
     *
     * @param set the backing {@link CustomSet} to iterate over
     */
    HashIterator(final CustomSet<E> set) {
        this.set = set;
        this.expectedModCount = set.modCount;
        this.yieldNull = set.containsNull;
        this.index = 0;
        if (!yieldNull) {
            advanceToNextBucket();
        }
    }

    /**
     * Returns {@code true} if the iteration has more elements.
     *
     * @return {@code true} if the iterator has more elements
     */
    @Override
    public boolean hasNext() {
        return yieldNull || treeBucketKeys != null || next != null;
    }

    /**
     * Returns the next element in the iteration.
     *
     * @return the next element in the iteration
     * @throws NoSuchElementException          if the iteration has no more elements
     * @throws ConcurrentModificationException if the backing set was structurally modified concurrently
     */
    @Override
    public E next() {
        checkForComodification();
        if (yieldNull) {
            yieldNull = false;
            lastReturnedKey = null;
            currentIsNull = true;
            canRemove = true;
            advanceToNextBucket();
            return null;
        }
        if (treeBucketKeys != null) {
            E key = (E) treeBucketKeys[treeBucketCursor++];
            lastReturnedKey = key;
            currentIsNull = false;
            canRemove = true;
            if (treeBucketCursor >= treeBucketKeys.length) {
                treeBucketKeys = null;
                treeBucketCursor = 0;
                advanceToNextBucket();
            }
            return key;
        }
        Node<E> e = next;
        if (e == null) {
            throw new NoSuchElementException();
        }
        lastReturnedKey = e.key;
        currentIsNull = false;
        canRemove = true;
        if ((next = e.next) == null) {
            advanceToNextBucket();
        }
        return e.key;
    }

    /**
     * Removes from the underlying set the last element returned by this iterator.
     *
     * @throws IllegalStateException           if {@link #next()} has not yet been called, or {@code remove()}
     *                                         has already been called after the last call to {@code next()}
     * @throws ConcurrentModificationException if the backing set was structurally modified concurrently
     */
    @Override
    public void remove() {
        checkForComodification();
        if (!canRemove) {
            throw new IllegalStateException();
        }
        canRemove = false;

        if (currentIsNull) {
            set.remove(null);
            currentIsNull = false;
        } else {
            set.remove(lastReturnedKey);
            lastReturnedKey = null;
        }
        expectedModCount = set.modCount;
    }

    /**
     * Advances the bucket cursor to the next non-empty slot in the hash table,
     * initializing either a direct node pointer or a localized key snapshot for tree bins.
     */
    private void advanceToNextBucket() {
        Node<E>[] tab = set.table;
        if (tab != null && set.size > 0) {
            while (index < tab.length) {
                Node<E> bucket = tab[index++];
                if (bucket != null) {
                    if (bucket instanceof TreeNode) {
                        int count = 0;
                        for (Node<E> n = bucket; n != null; n = n.next) {
                            count++;
                        }
                        Object[] keys = new Object[count];
                        int i = 0;
                        for (Node<E> n = bucket; n != null; n = n.next) {
                            keys[i++] = n.key;
                        }
                        this.treeBucketKeys = keys;
                        this.treeBucketCursor = 0;
                        this.next = null;
                    } else {
                        this.next = bucket;
                    }
                    return;
                }
            }
        }
        this.next = null;
    }

    /**
     * Verifies that the backing set has not been structurally modified outside of this iterator.
     *
     * @throws ConcurrentModificationException if {@code set.modCount} does not match {@code expectedModCount}.
     */
    private void checkForComodification() {
        if (set.modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
    }
}