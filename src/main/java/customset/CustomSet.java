package customset;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Predicate;

import static java.lang.reflect.Array.newInstance;
import static java.util.Arrays.fill;
import static java.util.Objects.requireNonNull;

/**
 * A specialized, high-performance hash-backed set implementation that mimics
 * the architectural design of modern JDK {@link java.util.HashSet} and {@link java.util.HashMap}.
 *
 * <p>This implementation utilizes singly-linked list chaining for standard collision
 * resolution and automatically transitions collision bins into self-balancing Red-Black trees
 * ({@link TreeNode}) when bin occupancy exceeds {@link #TREEIFY_THRESHOLD}, mitigating
 * worst-case $O(N)$ lookup degradation to $O(\log N)$.
 *
 * <p>Unlike the standard JDK {@link java.util.HashSet} (which wraps elements inside intermediate
 * {@code HashMap.Node} objects), {@code CustomSet} optimizes memory layout and cache locality
 * by storing entries directly, delivering enhanced wall-clock execution performance while
 * preserving identical asymptotic Big-O time and space complexity.
 *
 * @param <E> the type of elements maintained by this set
 * @author Benjamin Kane
 * LinkedIn - <a href="https://www.linkedin.com/in/benjamin-kane-81149482/"/>
 * GitHub account bk10aao - <a href="https://github.com/bk10aao"/>
 * Repository - <a href="https://github.com/bk10aao/CustomSetV3"/>
 * @see java.util.Set
 * @see java.util.HashSet
 */
public final class CustomSet<E> implements Set<E>, Cloneable, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private transient int threshold;
    private final float loadFactor;
    private static final int DEFAULT_INITIAL_CAPACITY = 16;
    private static final int HASH_BITS_TO_SHIFT = 16;
    private static final int MAXIMUM_CAPACITY = 1 << 30;
    private static final int MIN_TREEIFY_CAPACITY = 64;
    private static final int TREEIFY_THRESHOLD = 8;
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;

    transient int modCount = 0;
    transient Node<E>[] table;

    transient int size;

    /**
     * Constructs a new, empty set with an initial capacity of 16 and a default load factor of 0.75.
     */
    public CustomSet() {
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Constructs a new, empty set with the specified initial capacity and a default load factor of 0.75.
     *
     * @param initialCapacity the initial capacity of the hash table
     * @throws IllegalArgumentException if the initial capacity is negative
     */
    public CustomSet(final int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Constructs a new, empty set with the specified initial capacity and load factor.
     *
     * @param initialCapacity the initial capacity of the hash table
     * @param loadFactor      the load factor threshold for resizing
     * @throws IllegalArgumentException if the initial capacity is negative or the load factor is non-positive,
     *                                  NaN, or infinite
     */
    public CustomSet(final int initialCapacity, final float loadFactor) {
        if (initialCapacity < 0 || loadFactor <= 0 || Float.isNaN(loadFactor) || Float.isInfinite(loadFactor))
            throw new IllegalArgumentException();
        this.loadFactor = loadFactor;
        this.threshold = tableSizeFor(initialCapacity);
    }

    /**
     * Constructs a new set containing the elements of the specified collection.
     * The set is created with an initial capacity sufficient to hold all elements
     * in the specified collection without requiring a resize operation.
     *
     * @param c the collection whose elements are to be placed into this set
     * @throws NullPointerException if the specified collection is null
     */
    public CustomSet(final Collection<? extends E> c) {
        requireNonNull(c, "Collection must not be null.");
        long required = (long) (c.size() / (double) DEFAULT_LOAD_FACTOR) + 1;
        int capacity = tableSizeFor((int) Math.min(required, MAXIMUM_CAPACITY));
        capacity = Math.max(capacity, DEFAULT_INITIAL_CAPACITY);
        this.loadFactor = DEFAULT_LOAD_FACTOR;
        this.threshold = capacity;
        addAll(c);
    }

    /**
     * Adds the specified element to this set if it is not already present.
     * If the set previously contained the element, the call leaves the set unchanged and returns {@code false}.
     *
     * @param e element to be added to this set
     * @return {@code true} if this set did not already contain the specified element
     */
    public boolean add(final E e) {
        Node<E>[] tab = table;
        if (tab == null || tab.length == 0)
            tab = expand();
        int hash = hash(e);
        int index = hash & (tab.length - 1);
        Node<E> first = tab[index];
        if (first == null)
            tab[index] = new Node<>(hash, e, null);
        else if (!insertValue(tab, first, index, hash, e))
            return false;
        incrementSizeAndCheckResize();
        return true;
    }

    /**
     * Adds all the elements in the specified collection to this set if they're not already present.
     *
     * @param c collection containing elements to be added to this set
     * @return {@code true} if this set changed as a result of the call
     * @throws NullPointerException if the specified collection is null
     */
    public boolean addAll(final Collection<? extends E> c) {
        requireNonNull(c, "Collection must not be null.");
        if (c.isEmpty())
            return false;
        int initialSize = size;
        ensureCapacity(c.size());
        for (E e : c)
            add(e);
        return size > initialSize;
    }

    /**
     * Returns a shallow copy of this {@code CustomSet} instance: the elements themselves are not cloned.
     *
     * @return a shallow copy of this set
     */
    @Override
    public CustomSet<E> clone() {
        try {
            CustomSet<E> copy = (CustomSet<E>) super.clone();
            copy.modCount = 0;
            if (table == null) {
                copy.table = null;
                return copy;
            }
            Node<E>[] src = table, dst = (Node<E>[]) new Node[src.length];
            copy.table = dst;
            for (int i = 0; i < src.length; i++) {
                Node<E> n = src[i];
                if (n == null)
                    continue;
                if (n instanceof TreeNode) {
                    TreeNode<E> head = new TreeNode<>(n.hash, n.key, null), tail = head;
                    for (n = n.next; n != null; n = n.next) {
                        TreeNode<E> c = new TreeNode<>(n.hash, n.key, null);
                        c.previous = tail;
                        tail.next = c;
                        tail = c;
                    }
                    dst[i] = head;
                    head.treeify(dst);
                } else {
                    Node<E> head = new Node<>(n.hash, n.key, null), tail = head;
                    for (n = n.next; n != null; n = n.next) {
                        Node<E> c = new Node<>(n.hash, n.key, null);
                        tail.next = c;
                        tail = c;
                    }
                    dst[i] = head;
                }
            }
            return copy;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

    /**
     * Returns {@code true} if this set contains the specified element.
     *
     * @param o element whose presence in this set is to be tested
     * @return {@code true} if this set contains the specified element
     */
    public boolean contains(final Object o) {
        Node<E>[] tab = table;
        if (tab == null || tab.length == 0)
            return false;
        int hash = hash(o);
        int index = hash & (tab.length - 1);
        Node<E> first = tab[index];
        if (first == null)
            return false;
        if (first.hash == hash && keysMatch(o, first))
            return true;
        if (first instanceof TreeNode)
            return ((TreeNode<E>) first).getTreeNode(hash, o) != null;
        for (Node<E> node = first.next; node != null; node = node.next)
            if (node.hash == hash && keysMatch(o, node))
                return true;
        return false;
    }

    /**
     * Removes all the elements from this set. The set will be empty after this call returns.
     */
    public void clear() {
        Node<E>[] tab = table;
        if (tab != null && size > 0) {
            size = 0;
            fill(tab, null);
            modCount++;
        }
    }

    /**
     * Returns {@code true} if this set contains all the elements of the specified collection.
     *
     * @param c collection to be checked for containment in this set
     * @return {@code true} if this set contains all elements of the specified collection
     * @throws NullPointerException if the specified collection is null
     */
    public boolean containsAll(final Collection<?> c) {
        requireNonNull(c, "Collection must not be null.");
        for (Object e : c)
            if (!contains(e))
                return false;
        return true;
    }

    /**
     * Compares the specified object with this set for equality. Returns {@code true} if the
     * specified object is also a set, the two sets have the same size, and every member of
     * the specified set is contained in this set.
     *
     * @param o object to be compared for equality with this set
     * @return {@code true} if the specified object is equal to this set
     */
    @Override
    public boolean equals(final Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Set<?> other) || other.size() != size())
            return false;
        return containsAll(other);
    }

    /**
     * Returns the hash code value for this set. The hash code of a set is defined to be the
     * sum of the hash codes of the elements in the set.
     *
     * @return the hash code value for this set
     */
    @Override
    public int hashCode() {
        int sum = 0;
        Node<E>[] tab = table;
        if (tab != null)
            for (Node<E> node : tab)
                while (node != null) {
                    if (node.key != null)
                        sum += node.key.hashCode();
                    node = node.next;
                }
        return sum;
    }

    /**
     * Returns {@code true} if this set contains no elements.
     *
     * @return {@code true} if this set contains no elements
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Returns an iterator over the elements in this set.
     *
     * @return an iterator over the elements in this set
     */
    public Iterator<E> iterator() {
        return new HashIterator<>(this);
    }

    /**
     * Removes the specified element from this set if it is present.
     *
     * @param o object to be removed from this set, if present
     * @return {@code true} if this set contained the specified element
     */
    public boolean remove(final Object o) {
        Node<E>[] tab = table;
        if (tab == null || tab.length == 0)
            return false;
        int hash = hash(o);
        int index = hash & (tab.length - 1);
        Node<E> first = tab[index];
        if (first == null)
            return false;
        if (first instanceof TreeNode)
            return removeTreeNode(o, (TreeNode<E>) first, hash, tab);
        return removeNodeFromLinkedList(first, index, hash, o);
    }

    /**
     * Removes from this set all of its elements that are contained in the specified collection.
     *
     * @param c collection containing elements to be removed from this set
     * @return {@code true} if this set changed as a result of the call
     * @throws NullPointerException if the specified collection is null
     */
    public boolean removeAll(final Collection<?> c) {
        requireNonNull(c, "Collection must not be null.");
        if (c.isEmpty() || size == 0)
            return false;
        if (size > c.size()) {
            boolean modified = false;
            for (Object e : c)
                if (remove(e))
                    modified = true;
            return modified;
        }
        return removeMatching(e -> containsQuietly(c, e));
    }

    /**
     * Retains only the elements in this set that are contained in the specified collection.
     *
     * @param c collection containing elements to be retained in this set
     * @return {@code true} if this set changed as a result of the call
     * @throws NullPointerException if the specified collection is null
     */
    public boolean retainAll(final Collection<?> c) {
        requireNonNull(c, "Collection must not be null.");
        if (size == 0)
            return false;
        if (c.isEmpty()) {
            clear();
            return true;
        }
        final Collection<?> retain = (c instanceof Set<?>) ? c : new CustomSet<>(c);
        return removeMatching(e -> !containsQuietly(retain, e));
    }

    /**
     * Returns the number of elements in this set (its cardinality).
     *
     * @return the number of elements in this set
     */
    public int size() {
        return size;
    }

    /**
     * Creates a {@link Spliterator} over the elements in this set.
     *
     * <p>The returned spliterator is <em>late-binding</em>, fail-fast (throwing a
     * {@link java.util.ConcurrentModificationException ConcurrentModificationException} if
     * structural modifications occur during iteration), and reports characteristics of
     * {@link Spliterator#DISTINCT} and {@link Spliterator#SIZED}.
     *
     * @return a {@code Spliterator} over the elements in this set
     * @see Spliterator
     */
    public Spliterator<E> spliterator() {
        return new CustomSetSpliterator<>(this, 0, -1, size, modCount, true);
    }

    /**
     * Returns an array containing all the elements in this set.
     *
     * @return an array containing all the elements in this set
     */
    public Object[] toArray() {
        Object[] arr = new Object[size];
        int index = 0;
        if (table != null && size > 0)
            for (Node<E> node : table)
                while (node != null) {
                    arr[index++] = node.key;
                    node = node.next;
                }
        return arr;
    }

    /**
     * Returns an array containing all the elements in this set; the runtime type of
     * the returned array is that of the specified array.
     *
     * @param a   the array into which the elements of this set are to be stored, if it is big enough;
     *            otherwise, a new array of the same runtime type is allocated for this purpose
     * @param <T> the runtime type of the array to contain the collection
     * @return an array containing all the elements in this set
     * @throws NullPointerException if the specified array is null
     */
    public <T> T[] toArray(final T[] a) {
        requireNonNull(a, "Array must not be null.");
        T[] arr = a.length < size ? (T[]) newInstance(a.getClass().getComponentType(), size) : a;
        int index = 0;
        if (table != null && size > 0)
            for (Node<E> node : table)
                while (node != null) {
                    arr[index++] = (T) node.key;
                    node = node.next;
                }
        if (a.length > size)
            arr[size] = null;
        return arr;
    }

    /**
     * Returns a string representation of this set. The string representation consists of a list
     * of the set's elements in the order they are returned by its iterator, enclosed in square
     * brackets ({@code "[]"}). Adjacent elements are separated by the characters {@code ", "}
     * (comma and space).
     *
     * @return a string representation of this set
     */
    @Override
    public String toString() {
        if (size == 0)
            return "[]";
        StringBuilder sb = new StringBuilder((int) Math.min(1 << 20, Math.max(16L, size * 8L))).append('[');
        boolean first = true;
        if (table != null)
            for (Node<E> node : table)
                while (node != null) {
                    if (!first)
                        sb.append(", ");
                    sb.append(node.key);
                    node = node.next;
                    first = false;
                }
        return sb.append("]").toString();
    }

    private static boolean containsQuietly(final Collection<?> c, final Object o) {
        try {
            return c.contains(o);
        } catch (NullPointerException | ClassCastException e) {
            return false;
        }
    }

    private void ensureCapacity(final int additional) {
        long required = (long) ((size + (long) additional) / (double) loadFactor) + 1;
        int target = tableSizeFor((int) Math.min(required, MAXIMUM_CAPACITY));
        int currentCapacity = (table == null) ? threshold : table.length;
        if (target > currentCapacity)
            resize(target);
    }

    static <E> boolean keysMatch(Object target, Node<E> node) {
        return Objects.equals(node.key, target);
    }

    @Serial
    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        if (loadFactor <= 0 || Float.isNaN(loadFactor) || Float.isInfinite(loadFactor))
            throw new InvalidObjectException("Illegal load factor: " + loadFactor);
        int cap = s.readInt();
        if (cap < 0)
            throw new InvalidObjectException("Illegal capacity: " + cap);
        int mappings = s.readInt();
        if (mappings < 0)
            throw new InvalidObjectException("Illegal mappings count: " + mappings);
        int capacity = Math.max(tableSizeFor(cap), DEFAULT_INITIAL_CAPACITY);
        this.table = (Node<E>[]) new Node[capacity];
        setThreshold(capacity);
        this.size = 0;
        for (int i = 0; i < mappings; i++)
            add((E) s.readObject());
    }

    private void resize(final int newCapacity) {
        Node<E>[] oldTable = table;
        int oldCapacity = (oldTable == null) ? 0 : oldTable.length;
        Node<E>[] newTable = (Node<E>[]) new Node[newCapacity];
        table = newTable;
        setThreshold(newCapacity);
        if (oldTable != null && size > 0) {
            if (newCapacity == oldCapacity << 1) {
                transferBins(oldTable, newTable, oldCapacity);
                return;
            }
            int mask = newCapacity - 1;
            boolean canTreeify = newCapacity >= MIN_TREEIFY_CAPACITY;
            for (int i = 0; i < oldCapacity; i++) {
                Node<E> node = oldTable[i];
                if (node == null)
                    continue;
                oldTable[i] = null;
                int count = 0;
                while (node != null) {
                    Node<E> next = node.next;
                    Node<E> entry = (node instanceof TreeNode)
                            ? new Node<>(node.hash, node.key, null)
                            : node;
                    int index = entry.hash & mask;
                    entry.next = newTable[index];
                    newTable[index] = entry;
                    node = next;
                    count++;
                }
                if (canTreeify && count > TREEIFY_THRESHOLD)
                    for (int j = i; j < newCapacity; j += oldCapacity) {
                        int binCount = 0;
                        for (Node<E> n = newTable[j]; n != null && binCount <= TREEIFY_THRESHOLD; n = n.next)
                            binCount++;
                        if (binCount > TREEIFY_THRESHOLD)
                            treeifyBin(newTable, j);
                    }
            }
        }
    }

    private Node<E>[] expand() {
        Node<E>[] oldTable = table;
        int oldCapacity = (oldTable == null) ? 0 : oldTable.length;
        int newCapacity;
        if (oldCapacity > 0) {
            if (oldCapacity >= MAXIMUM_CAPACITY) {
                threshold = Integer.MAX_VALUE;
                return oldTable;
            }
            newCapacity = oldCapacity << 1;
            if (newCapacity > MAXIMUM_CAPACITY)
                newCapacity = MAXIMUM_CAPACITY;
        } else
            newCapacity = threshold > 0 ? threshold : DEFAULT_INITIAL_CAPACITY;
        Node<E>[] newTable = (Node<E>[]) new Node[newCapacity];
        table = newTable;
        setThreshold(newCapacity);
        transferBins(oldTable, newTable, oldCapacity);
        return newTable;
    }

    private static int hash(final Object key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> HASH_BITS_TO_SHIFT);
    }

    private void incrementSizeAndCheckResize() {
        size++;
        modCount++;
        if (size > threshold)
            expand();
    }

    private boolean insertValue(final Node<E>[] tab, final Node<E> first, final int index, final int hash, final E e) {
        if (first instanceof TreeNode)
            return ((TreeNode<E>) first).putTreeVal(tab, hash, e) == null;
        else {
            Node<E> current = first;
            int binCount = 0;
            while (true) {
                if (current.hash == hash && keysMatch(e, current))
                    return false;
                Node<E> nextNode = current.next;
                if (nextNode == null) {
                    current.next = new Node<>(hash, e, null);
                    if (binCount >= TREEIFY_THRESHOLD - 1)
                        treeifyBin(tab, index);
                    break;
                }
                current = nextNode;
                binCount++;
            }
        }
        return true;
    }

    private boolean removeMatching(final Predicate<? super E> filter) {
        Node<E>[] tab = table;
        if (tab == null || size == 0)
            return false;
        int removed = 0;
        List<E> treeKeys = null;
        try {
            for (int i = 0; i < tab.length; i++) {
                Node<E> first = tab[i];
                if (first == null)
                    continue;
                if (first instanceof TreeNode) {
                    for (Node<E> n = first; n != null; n = n.next)
                        if (filter.test(n.key)) {
                            if (treeKeys == null)
                                treeKeys = new ArrayList<>();
                            treeKeys.add(n.key);
                        }
                    continue;
                }
                Node<E> previous = null;
                for (Node<E> n = first; n != null; n = n.next) {
                    if (filter.test(n.key)) {
                        if (previous == null)
                            tab[i] = n.next;
                        else
                            previous.next = n.next;
                        removed++;
                    } else
                        previous = n;
                }
            }
        } finally {
            if (removed > 0) {
                size -= removed;
                modCount++;
            }
        }
        if (treeKeys != null)
            for (E key : treeKeys)
                remove(key);
        return removed > 0 || treeKeys != null;
    }

    private void removeNode(final Node<E> previous, final int index, final Node<E> entry) {
        Node<E>[] tab = table;
        if (previous == null)
            tab[index] = entry.next;
        else
            previous.next = entry.next;
        size--;
        modCount++;
    }

    private boolean removeNodeFromLinkedList(final Node<E> first, final int index, final int hash, final Object o) {
        Node<E> current = first;
        Node<E> previous = null;
        while (current != null) {
            if (current.hash == hash && keysMatch(o, current)) {
                removeNode(previous, index, current);
                return true;
            }
            previous = current;
            current = current.next;
        }
        return false;
    }

    private boolean removeTreeNode(Object o, TreeNode<E> first, int hash, Node<E>[] tab) {
        TreeNode<E> node = (TreeNode<E>) first.getTreeNode(hash, o);
        if (node != null) {
            node.removeTreeNode(tab);
            size--;
            modCount++;
            return true;
        }
        return false;
    }

    private void setThreshold(int capacity) {
        threshold = capacity >= MAXIMUM_CAPACITY
                ? Integer.MAX_VALUE
                : (int) Math.min((long) capacity * loadFactor, Integer.MAX_VALUE);
    }

    private static int tableSizeFor(final int cap) {
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> HASH_BITS_TO_SHIFT;
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }

    private void transferBins(final Node<E>[] oldTable, final Node<E>[] newTable, final int oldCapacity) {
        int newCapacity = newTable.length;
        for (int i = 0; i < oldCapacity; ++i) {
            Node<E> entry = oldTable[i];
            if (entry != null) {
                oldTable[i] = null;
                if (entry.next == null)
                    newTable[entry.hash & (newCapacity - 1)] = entry;
                else if (entry instanceof TreeNode)
                    ((TreeNode<E>) entry).split(newTable, i, oldCapacity);
                else {
                    Node<E> lowHead = null, lowTail = null, highHead = null, highTail = null, nextEntry;
                    do {
                        nextEntry = entry.next;
                        if ((entry.hash & oldCapacity) == 0) {
                            if (lowTail == null)
                                lowHead = entry;
                            else
                                lowTail.next = entry;
                            lowTail = entry;
                        } else {
                            if (highTail == null)
                                highHead = entry;
                            else
                                highTail.next = entry;
                            highTail = entry;
                        }
                    } while ((entry = nextEntry) != null);

                    if (lowTail != null) {
                        lowTail.next = null;
                        newTable[i] = lowHead;
                    }
                    if (highTail != null) {
                        highTail.next = null;
                        newTable[i + oldCapacity] = highHead;
                    }
                }
            }
        }
    }

    private void treeifyBin(final Node<E>[] tab, final int index) {
        if (tab == null || tab.length < MIN_TREEIFY_CAPACITY) {
            expand();
            return;
        }
        Node<E> entry = tab[index];
        if (entry != null) {
            TreeNode<E> head = null, tail = null;
            do {
                TreeNode<E> treeNode = new TreeNode<>(entry.hash, entry.key, null);
                if (tail == null)
                    head = treeNode;
                else {
                    treeNode.previous = tail;
                    tail.next = treeNode;
                }
                tail = treeNode;
            } while ((entry = entry.next) != null);
            tab[index] = head;
            head.treeify(tab);
        }
    }

    @Serial
    private void writeObject(java.io.ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        s.writeInt(table == null ? 0 : table.length);
        s.writeInt(size);
        if (table != null && size > 0)
            for (Node<E> node : table)
                while (node != null) {
                    s.writeObject(node.key);
                    node = node.next;
                }
    }

}