package customset;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;

import static java.lang.System.identityHashCode;
import static java.lang.reflect.Array.newInstance;
import static java.util.Arrays.fill;
import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;
import static java.util.Objects.requireNonNullElseGet;

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
 * @see java.util.Set
 * @see java.util.HashSet
 * @author Benjamin Kane
 * LinkedIn - <a href="https://www.linkedin.com/in/benjamin-kane-81149482/"/>
 * GitHub account bk10aao - <a href="https://github.com/bk10aao"/>
 * Repository - <a href="https://github.com/bk10aao/CustomSetV3"/>
 */
public class CustomSet<E> implements Set<E>, Cloneable, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private boolean containsNull = false;

    private final float loadFactor;

    private int size;
    private int threshold;

    private transient Node<E>[] table;

    private static final int DEFAULT_INITIAL_CAPACITY = 16;
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;
    private static final int HASH_BITS_TO_SHIFT = 16;
    private static final int MAXIMUM_CAPACITY = 1 << 30;
    private static final int MIN_TREEIFY_CAPACITY = 64;
    private static final int TREEIFY_THRESHOLD   = 8;
    private static final int UNTREEIFY_THRESHOLD = 6;

    private transient int modCount = 0;

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
        int capacity = tableSizeFor(initialCapacity);
        this.table = (Node<E>[]) new Node[capacity];
        this.size = 0;
        this.loadFactor = loadFactor;
        setThreshold(capacity);
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
        this(Math.max((int) (requireNonNull(c).size() / DEFAULT_LOAD_FACTOR) + 1, DEFAULT_INITIAL_CAPACITY),
                DEFAULT_LOAD_FACTOR);
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
        if (e == null)
            return addNullElement();
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
            copy.table = (Node<E>[]) new Node[table.length];
            copy.size = 0;
            copy.containsNull = false;
            copy.modCount = 0;
            copy.setThreshold(copy.table.length);
            copy.addAll(this);
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
        if (o == null)
            return containsNull;
        Node<E>[] tab = table;
        if (tab == null || tab.length == 0)
            return false;
        int hash = hash(o);
        int index = hash & (tab.length - 1);
        Node<E> first = tab[index];
        if (first == null)
            return false;
        if (first instanceof TreeNode)
            return ((TreeNode<E>) first).getTreeNode(hash, o) != null;
        for (Node<E> node = first; node != null; node = node.next)
            if (node.hash == hash && keysMatch(o, node))
                return true;
        return false;
    }

    private static <E> boolean keysMatch(Object target, Node<E> node) {
        return node.key == target || node.key.equals(target);
    }

    /**
     * Removes all the elements from this set. The set will be empty after this call returns.
     */
    public void clear() {
        Node<E>[] tab = table;
        containsNull = false;
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
        return new HashIterator();
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
        this.containsNull = false;
        for (int i = 0; i < mappings; i++)
            add((E) s.readObject());
    }

    /**
     * Removes the specified element from this set if it is present.
     *
     * @param o object to be removed from this set, if present
     * @return {@code true} if this set contained the specified element
     */
    public boolean remove(final Object o) {
        if (o == null)
            return removeNullElement();
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
        boolean modified = false;
        if (size > c.size()) {
            for (Object e : c)
                if (remove(e))
                    modified = true;
        } else {
            Iterator<E> it = iterator();
            while (it.hasNext()) {
                if (c.contains(it.next())) {
                    it.remove();
                    modified = true;
                }
            }
        }
        return modified;
    }

    /**
     * Retains only the elements in this set that are contained in the specified collection.
     *
     * @param c collection containing elements to be retained in this set
     * @return {@code true} if this set changed as a result of the call
     * @throws NullPointerException if the specified collection is null
     */
    public boolean retainAll(final Collection<?> c) {
        requireNonNull(c, "Collection must not be null");
        if (size == 0)
            return false;
        boolean modified = false;
        Iterator<E> it = iterator();
        while (it.hasNext()) {
            E element = it.next();
            if (!c.contains(element)) {
                it.remove();
                modified = true;
            }
        }
        return modified;
    }

    /**
     * Returns the number of elements in this set (its cardinality).
     *
     * @return the number of elements in this set
     */
    public int size() {
        return size;
    }

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
        if (containsNull)
            arr[index++] = null;
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
     * @param a the array into which the elements of this set are to be stored, if it is big enough;
     *          otherwise, a new array of the same runtime type is allocated for this purpose
     * @param <T> the runtime type of the array to contain the collection
     * @return an array containing all the elements in this set
     * @throws NullPointerException if the specified array is null
     */
    public <T> T[] toArray(final T[] a) {
        requireNonNull(a);
        T[] arr = a.length < size ? (T[]) newInstance(a.getClass().getComponentType(), size) : a;
        int index = 0;
        if (containsNull)
            arr[index++] = null;
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
     * of the set's elements enclosed in square brackets or braces depending on formatting.
     *
     * @return a string representation of this set
     */
    @Override
    public String toString() {
        if (size == 0)
            return "{}";
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        if (containsNull) {
            sb.append("null");
            first = false;
        }
        for (Node<E> node : table)
            while (node != null) {
                if (!first)
                    sb.append(", ");
                sb.append(node.key);
                node = node.next;
                first = false;
            }
        return sb.append("}").toString();
    }

    private boolean addNullElement() {
        if (containsNull)
            return false;
        containsNull = true;
        size++;
        modCount++;
        return true;
    }

    private void ensureCapacity(final int additional) {
        long required = (long)((size + (long) additional ) / (double) loadFactor) + 1;
        int target = tableSizeFor((int)Math.min(required, MAXIMUM_CAPACITY));
        if (target > table.length)
            resize(target);
    }

    private void resize(final int newCapacity) {
        Node<E>[] oldTable = table;
        int oldCapacity = (oldTable == null) ? 0 : oldTable.length;
        Node<E>[] newTable = (Node<E>[]) new Node[newCapacity];

        table = newTable;
        setThreshold(newCapacity);

        if (oldTable != null && size > 0) {
            for (int i = 0; i < oldCapacity; ++i) {
                Node<E> entry = oldTable[i];
                if (entry != null) {
                    oldTable[i] = null;
                    if (entry.next == null)
                        newTable[entry.hash & (newCapacity - 1)] = entry;
                    else if (entry instanceof TreeNode)
                        ((TreeNode<E>) entry).split(newTable, i, oldCapacity);
                    else {
                        Node<E> lowHead = null, lowTail = null;
                        Node<E> highHead = null, highTail = null;
                        Node<E> nextEntry;
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
            newCapacity = DEFAULT_INITIAL_CAPACITY;
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
        if (first instanceof TreeNode) {
            TreeNode<E> existing = ((TreeNode<E>) first).putTreeVal(tab, hash, e);
            return existing == null;
        } else {
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

    private boolean removeNullElement() {
        boolean modified = false;
        if (containsNull) {
            containsNull = false;
            modified = true;
            modCount++;
            size--;
        }
        return modified;
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
                    Node<E> lowHead = null, lowTail = null;
                    Node<E> highHead = null, highTail = null;
                    Node<E> nextEntry;
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
        for (E e : this)
            s.writeObject(e);
    }

    private class HashIterator implements Iterator<E> {
        private Node<E> current;
        private Node<E> next;
        private int index;
        private boolean includeNull;
        private boolean removeNull;
        private int expectedModCount;

        private HashIterator() {
            expectedModCount = modCount;
            includeNull = containsNull;
            Node<E>[] tab = table;
            if (tab != null && size > 0)
                while (index < tab.length && (next = tab[index++]) == null) { }
        }

        public boolean hasNext() {
            return includeNull || next != null;
        }

        public E next() {
            checkForComodification();
            if (includeNull) {
                includeNull = false;
                removeNull = true;
                current = null;
                return null;
            }
            Node<E> e = next;
            if (e == null)
                throw new NoSuchElementException();
            removeNull = false;
            current = e;
            Node<E>[] tab = table;
            if ((next = e.next) == null && tab != null)
                while (index < tab.length && (next = tab[index++]) == null) { }
            return e.key;
        }

        public void remove() {
            checkForComodification();
            if (removeNull) {
                removeNull = false;
                CustomSet.this.remove(null);
            } else if (current != null) {
                Object key = current.key;
                current = null;
                CustomSet.this.remove(key);
            } else
                throw new IllegalStateException();
            expectedModCount = modCount;
        }

        private void checkForComodification() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
        }
    }

    private static class Node<E> {
        final int hash;
        final E key;
        Node<E> next;

        private Node(final int hash, final E key, final Node<E> next) {
            this.hash = hash;
            this.key = key;
            this.next = next;
        }

        @Override
        public final String toString() {
            return String.valueOf(key);
        }
    }

    private static final class TreeNode<E> extends Node<E> {
        private TreeNode<E> left;
        private TreeNode<E> right;
        private TreeNode<E> parent;
        private TreeNode<E> previous;

        private boolean isRed;

        private TreeNode(final int hash, final E key, final Node<E> next) {
            super(hash, key, next);
        }

        /**
         * Re-balances the red-black tree after a node deletion.
         *
         * @param root the current root of the tree
         * @param fixupNode    the node that replaced the deleted node (or the deleted node itself if it was a leaf)
         * @return the new root of the tree
         */
        private static <E> TreeNode<E> balanceDeletion(TreeNode<E> root, TreeNode<E> fixupNode) {
            for (TreeNode<E> parentNode, parentLeftChild, parentRightChild; ; ) {
                if (fixupNode == null || fixupNode == root)
                    return root;

                if ((parentNode = fixupNode.parent) == null) {
                    fixupNode.isRed = false;
                    return fixupNode;
                }

                if (fixupNode.isRed) {
                    fixupNode.isRed = false;
                    return root;
                }

                if ((parentLeftChild = parentNode.left) == fixupNode) {
                    if ((parentRightChild = parentNode.right) != null && parentRightChild.isRed) {
                        parentRightChild.isRed = false;
                        parentNode.isRed  = true;
                        root = rotateLeft(root, parentNode);
                        parentRightChild = (parentNode = fixupNode.parent) == null ? null : parentNode.right;
                    }

                    if (parentRightChild == null)
                        fixupNode = parentNode;
                    else {
                        TreeNode<E> siblingLeft = parentRightChild.left, siblingRight = parentRightChild.right;
                        if ((siblingRight == null || !siblingRight.isRed) && (siblingLeft == null || !siblingLeft.isRed)) {
                            parentRightChild.isRed = true;
                            fixupNode = parentNode;
                        } else {
                            if (siblingRight == null || !siblingRight.isRed) {
                                if (siblingLeft != null)
                                    siblingLeft.isRed = false;
                                parentRightChild.isRed = true;
                                root = rotateRight(root, parentRightChild);
                                parentRightChild = (parentNode = fixupNode.parent) == null ? null : parentNode.right;
                            }
                            if (parentRightChild != null) {
                                parentRightChild.isRed = parentNode.isRed;
                                if ((siblingRight = parentRightChild.right) != null)
                                    siblingRight.isRed = false;
                            }
                            if (parentNode != null) {
                                parentNode.isRed = false;
                                root = rotateLeft(root, parentNode);
                            }
                            fixupNode = root;
                        }
                    }
                } else {
                    if (parentLeftChild != null && parentLeftChild.isRed) {
                        parentLeftChild.isRed = false;
                        parentNode.isRed  = true;
                        root = rotateRight(root, parentNode);
                        parentLeftChild = (parentNode = fixupNode.parent) == null ? null : parentNode.left;
                    }

                    if (parentLeftChild == null)
                        fixupNode = parentNode;
                    else {
                        TreeNode<E> siblingLeft = parentLeftChild.left, siblingRight = parentLeftChild.right;
                        if ((siblingLeft == null || !siblingLeft.isRed) && (siblingRight == null || !siblingRight.isRed)) {
                            parentLeftChild.isRed = true;
                            fixupNode = parentNode;
                        } else {
                            if (siblingLeft == null || !siblingLeft.isRed) {
                                if (siblingRight != null)
                                    siblingRight.isRed = false;
                                parentLeftChild.isRed = true;
                                root = rotateLeft(root, parentLeftChild);
                                parentLeftChild = (parentNode = fixupNode.parent) == null ? null : parentNode.left;
                            }
                            if (parentLeftChild != null) {
                                parentLeftChild.isRed = parentNode.isRed;
                                if ((siblingLeft = parentLeftChild.left) != null)
                                    siblingLeft.isRed = false;
                            }
                            if (parentNode != null) {
                                parentNode.isRed = false;
                                root = rotateRight(root, parentNode);
                            }
                            fixupNode = root;
                        }
                    }
                }
            }
        }

        /**
         * Re-balances the red-black tree after a new node insertion.
         *
         * @param root        the root of the tree
         * @param currentNode the newly inserted node
         * @return the updated root of the tree
         */
        private static <E> TreeNode<E> balanceInsertion(TreeNode<E> root, TreeNode<E> currentNode) {
            currentNode.isRed = true;
            for (TreeNode<E> parentNode, grandparentNode, grandparentLeft, grandparentRight; ; ) {
                if ((parentNode = currentNode.parent) == null) {
                    currentNode.isRed = false;
                    return currentNode;
                } else if (!parentNode.isRed || (grandparentNode = parentNode.parent) == null)
                    return root;
                if (parentNode == grandparentNode.left)
                    if ((grandparentRight = grandparentNode.right) != null && grandparentRight.isRed) {
                        grandparentRight.isRed = false;
                        parentNode.isRed = false;
                        grandparentNode.isRed = true;
                        currentNode = grandparentNode;
                    } else {
                        if (currentNode == parentNode.right) {
                            root = rotateLeft(root, currentNode = parentNode);
                            grandparentNode = (parentNode = currentNode.parent) == null ? null : parentNode.parent;
                        }
                        if (parentNode != null) {
                            parentNode.isRed = false;
                            if (grandparentNode != null) {
                                grandparentNode.isRed = true;
                                root = rotateRight(root, grandparentNode);
                            }
                        }
                        return root;
                    }
                else if ((grandparentLeft = grandparentNode.left) != null && grandparentLeft.isRed) {
                    grandparentLeft.isRed = false;
                    parentNode.isRed = false;
                    grandparentNode.isRed = true;
                    currentNode = grandparentNode;
                } else {
                    if (currentNode == parentNode.left) {
                        root = rotateRight(root, currentNode = parentNode);
                        grandparentNode = (parentNode = currentNode.parent) == null ? null : parentNode.parent;
                    }
                    if (parentNode != null) {
                        parentNode.isRed = false;
                        if (grandparentNode != null) {
                            grandparentNode.isRed = true;
                            root = rotateLeft(root, grandparentNode);
                        }
                    }
                    return root;
                }
            }
        }

        private static int compare(final int hash1, final int hash2, final Object v1, final Object v2) {
            if (hash1 != hash2)
                return hash1 < hash2 ? -1 : 1;
            Class<?> kc = comparableClassFor(v1);
            int cmp;
            if (kc != null && (cmp = compareComparables(kc, v1, v2)) != 0)
                return cmp;
            return tieBreakOrder(v1, v2);
        }

        private static Class<?> comparableClassFor(final Object x) {
            if (x instanceof Comparable) {
                Class<?> c = x.getClass();
                if (c == String.class)
                    return c;
                for (Type t : c.getGenericInterfaces())
                    if (t instanceof ParameterizedType p && p.getRawType() == Comparable.class) {
                        Type[] args = p.getActualTypeArguments();
                        if (args.length == 1 && args[0] == c)
                            return c;
                    }
            }
            return null;
        }

        private static int compareComparables(final Class<?> kc, final Object k, final Object x) {
            return(x == null || x.getClass() != kc) ? 0 : ((Comparable)k).compareTo(x);
        }

        private TreeNode<E> find(final int hash, final Object target, Class<?> kc) {
            TreeNode<E> p = this;
            do {
                TreeNode<E> pl = p.left, pr = p.right, q;
                int dir;
                if (p.hash > hash)
                    p = pl;
                else if (p.hash < hash)
                    p = pr;
                else if (p.key == target || p.key.equals(target))
                    return p;
                else if (pl == null)
                    p = pr;
                else if (pr == null)
                    p = pl;
                else if ((kc != null || (kc = comparableClassFor(target)) != null)
                        && (dir = compareComparables(kc, target, p.key)) != 0)
                    p = (dir < 0) ? pl : pr;
                else if ((q = pr.find(hash, target, kc)) != null)
                    return q;
                else
                    p = pl;
            } while (p != null);
            return null;
        }

        private TreeNode<?> getTreeNode(final int hash, final Object o) {
            return((parent != null) ? root() : this).find(hash, o, null);
        }

        /**
         * Ensures that the given root node of a red-black tree bin is placed at the front
         * of the bucket's collision chain in the table.
         *
         * @param tab  the hash table array
         * @param root the root node of the tree
         */
        private static <E> void moveRootToFront(final Node<E>[] tab, final TreeNode<E> root) {
            int n;
            if (root != null && tab != null && (n = tab.length) > 0) {
                int index = (n - 1) & root.hash;
                TreeNode<E> first = (TreeNode<E>) tab[index];
                if (root != first) {
                    Node<E> rootNext;
                    tab[index] = root;
                    TreeNode<E> rootPrevious = root.previous;
                    if ((rootNext = root.next) != null)
                        ((TreeNode<E>) rootNext).previous = rootPrevious;
                    if (rootPrevious != null)
                        rootPrevious.next = rootNext;
                    if (first != null)
                        first.previous = root;
                    root.next = first;
                    root.previous = null;
                }
            }
        }

        private TreeNode<E> putTreeVal(final Node<E>[] tab, final int hash, final E value) {
            Class<?> kc = null;
            boolean searched = false;
            TreeNode<E> root = (parent != null) ? root() : this;
            for (TreeNode<E> p = root; ; ) {
                int dir;
                if (p.hash > hash)
                    dir = -1;
                else if (p.hash < hash)
                    dir = 1;
                else if (p.key == value || value.equals(p.key))
                    return p;
                else if ((kc == null && (kc = comparableClassFor(value)) == null)
                        || (dir = compareComparables(kc, value, p.key)) == 0) {
                    if (!searched) {
                        TreeNode<E> q, ch;
                        searched = true;
                        if (((ch = p.left) != null && (q = ch.find(hash, value, kc)) != null)
                                || ((ch = p.right) != null && (q = ch.find(hash, value, kc)) != null))
                            return q;
                    }
                    dir = tieBreakOrder(value, p.key);
                }
                TreeNode<E> parentOfNewNode = p;
                if ((p = (dir <= 0) ? p.left : p.right) == null) {
                    Node<E> nextAfterInsertion = parentOfNewNode.next;
                    TreeNode<E> newNode = new TreeNode<>(hash, value, nextAfterInsertion);
                    if (dir <= 0)
                        parentOfNewNode.left = newNode;
                    else
                        parentOfNewNode.right = newNode;
                    parentOfNewNode.next = newNode;
                    newNode.parent = newNode.previous = parentOfNewNode;
                    if (nextAfterInsertion != null)
                        ((TreeNode<E>) nextAfterInsertion).previous = newNode;
                    moveRootToFront(tab, balanceInsertion(root, newNode));
                    return null;
                }
            }
        }

        /**
         * Removes this node from the red-black tree, maintains the secondary
         * linked-list view, rebalances, and only converts back to a plain list
         * when the remaining tree is small.
         */
        private void removeTreeNode(final Node<E>[] tab) {
            int tableLength;
            if (tab == null || (tableLength = tab.length) == 0)
                return;
            final int index = (tableLength - 1) & hash;
            TreeNode<E> first = (TreeNode<E>) tab[index];
            TreeNode<E> root  = first;
            TreeNode<E> nextLinkedNode  = (TreeNode<E>) next;
            TreeNode<E> previousLinkedNode  = previous;
            if (previousLinkedNode == null)
                tab[index] = first = nextLinkedNode;
            else
                previousLinkedNode.next = nextLinkedNode;
            if (nextLinkedNode != null)
                nextLinkedNode.previous = previousLinkedNode;

            if (first == null)
                return;
            if (root.parent != null)
                root = root.root();

            TreeNode<E> rl;
            if (root.right == null || (rl = root.left) == null || rl.left == null) {
                tab[index] = first.untreeify();
                return;
            }
            TreeNode<E> nodeToDelete = this;
            TreeNode<E> leftChild = left;
            TreeNode<E> rightChild = right;
            TreeNode<E> replacement;

            if (leftChild != null && rightChild != null) {
                TreeNode<E> inOrderSuccessor = rightChild;
                for (TreeNode<E> successorLeft; (successorLeft = inOrderSuccessor.left) != null; )
                    inOrderSuccessor = successorLeft;
                boolean swappedColor = inOrderSuccessor.isRed;
                inOrderSuccessor.isRed = nodeToDelete.isRed;
                nodeToDelete.isRed = swappedColor;

                TreeNode<E> successorRight = inOrderSuccessor.right;
                TreeNode<E> parentNode = nodeToDelete.parent;

                if (inOrderSuccessor == rightChild) {
                    nodeToDelete.parent = inOrderSuccessor;
                    inOrderSuccessor.right  = nodeToDelete;
                } else {
                    TreeNode<E> successorParent = inOrderSuccessor.parent;
                    if ((nodeToDelete.parent = successorParent) != null)
                        if (inOrderSuccessor == successorParent.left)
                            successorParent.left  = nodeToDelete;
                        else
                            successorParent.right = nodeToDelete;
                    if ((inOrderSuccessor.right = rightChild) != null)
                        rightChild.parent = inOrderSuccessor;
                }

                nodeToDelete.left = null;
                if ((nodeToDelete.right = successorRight) != null)
                    successorRight.parent = nodeToDelete;

                if ((inOrderSuccessor.left = leftChild) != null)
                    leftChild.parent = inOrderSuccessor;

                if ((inOrderSuccessor.parent = parentNode) == null)
                    root = inOrderSuccessor;
                else if (nodeToDelete == parentNode.left)
                    parentNode.left  = inOrderSuccessor;
                else
                    parentNode.right = inOrderSuccessor;
                replacement = (successorRight != null) ? successorRight : nodeToDelete;
            }
            else
                replacement = requireNonNullElseGet(leftChild, () -> requireNonNullElse(rightChild, nodeToDelete));
            if (replacement != nodeToDelete) {
                TreeNode<E> pp = replacement.parent = nodeToDelete.parent;
                if (pp == null)
                    root = replacement;
                else if (nodeToDelete == pp.left)
                    pp.left  = replacement;
                else
                    pp.right = replacement;
                nodeToDelete.left = nodeToDelete.right = nodeToDelete.parent = null;
            }

            TreeNode<E> r = nodeToDelete.isRed ? root : balanceDeletion(root, replacement);
            if (replacement == nodeToDelete) {
                TreeNode<E> pp = nodeToDelete.parent;
                nodeToDelete.parent = null;
                if (pp != null) {
                    if (nodeToDelete == pp.left)
                        pp.left  = null;
                    else if (nodeToDelete == pp.right)
                        pp.right = null;
                }
            }
            moveRootToFront(tab, r);
        }

        /**
         * Traverses parent links to find and return the root node of this red-black tree.
         *
         * @return the root node of the tree
         */
        private TreeNode<E> root() {
            TreeNode<E> r = this;
            while (r.parent != null)
                r = r.parent;
            return r;
        }

        /**
         * Performs a left rotation on the red-black tree around the specified node.
         *
         * @param root the current root of the tree
         * @param pivotNode    the pivot node for rotation
         * @return the new root of the tree
         */
        private static <E> TreeNode<E> rotateLeft(TreeNode<E> root, final TreeNode<E> pivotNode) {
            TreeNode<E> rightChild = pivotNode.right;
            pivotNode.right = rightChild.left;
            if (rightChild.left != null)
                rightChild.left.parent = pivotNode;
            rightChild.parent = pivotNode.parent;
            if (pivotNode.parent == null)
                root = rightChild;
            else if (pivotNode.parent.left == pivotNode)
                pivotNode.parent.left = rightChild;
            else
                pivotNode.parent.right = rightChild;
            rightChild.left = pivotNode;
            pivotNode.parent = rightChild;
            return root;
        }

        /**
         * Performs a right rotation on the red-black tree around the specified node.
         *
         * @param root the current root of the tree
         * @param pivotNode    the pivot node for rotation
         * @return the new root of the tree
         */
        private static <E> TreeNode<E> rotateRight(TreeNode<E> root, final TreeNode<E> pivotNode) {
            TreeNode<E> leftChild = pivotNode.left;
            pivotNode.left = leftChild.right;
            if (leftChild.right != null)
                leftChild.right.parent = pivotNode;
            leftChild.parent = pivotNode.parent;
            if (pivotNode.parent == null)
                root = leftChild;
            else if (pivotNode.parent.right == pivotNode)
                pivotNode.parent.right = leftChild;
            else
                pivotNode.parent.left = leftChild;
            leftChild.right = pivotNode;
            pivotNode.parent = leftChild;
            return root;
        }

        /**
         * Splits a tree bin into low and high bins during table resizing,
         * untreeifying them if they drop below the threshold.
         *
         * @param tab   the hash table array
         * @param index the current bucket index
         * @param capacityBit   the capacityBit mask used for partitioning
         */
        private void split(final Node<E>[] tab, final int index, final int capacityBit) {
            TreeNode<E> binRoot = this;
            TreeNode<E> lowHead = null, lowTail = null;
            TreeNode<E> highHead = null, highTail = null;
            int lowCount = 0, highCount = 0;
            for (TreeNode<E> currentNode = binRoot, next; currentNode != null; currentNode = next) {
                next = (TreeNode<E>) currentNode.next;
                currentNode.next = null;
                if ((currentNode.hash & capacityBit) == 0) {
                    if ((currentNode.previous = lowTail) == null)
                        lowHead = currentNode;
                    else
                        lowTail.next = currentNode;
                    lowTail = currentNode;
                    ++lowCount;
                } else {
                    if ((currentNode.previous = highTail) == null)
                        highHead = currentNode;
                    else
                        highTail.next = currentNode;
                    highTail = currentNode;
                    ++highCount;
                }
            }
            if (lowHead != null)
                if (lowCount <= UNTREEIFY_THRESHOLD)
                    tab[index] = lowHead.untreeify();
                else {
                    tab[index] = lowHead;
                    if (highHead != null)
                        lowHead.treeify(tab);
                }
            if (highHead != null)
                if (highCount <= UNTREEIFY_THRESHOLD)
                    tab[index + capacityBit] = highHead.untreeify();
                else {
                    tab[index + capacityBit] = highHead;
                    if (lowHead != null)
                        highHead.treeify(tab);
                }
        }

        private static int tieBreakOrder(final Object a, final Object b) {
            int d;
            if (a == null || b == null || (d = a.getClass().getName().compareTo(b.getClass().getName())) == 0)
                d = identityHashCode(a) <= identityHashCode(b) ? -1 : 1;
            return d;
        }



        /**
         * Forms a balanced red-black tree out of the linked list nodes starting from this node.
         *
         * @param tab the hash table array
         */
        private void treeify(final Node<E>[] tab) {
            TreeNode<E> root = null;
            for (TreeNode<E> current = this, nextNode; current != null; current = nextNode) {
                nextNode = (TreeNode<E>) current.next;
                current.left = current.right = null;
                if (root == null) {
                    current.parent = null;
                    current.isRed = false;
                    root = current;
                } else
                    for (TreeNode<E> parent = root; ; ) {
                        int direction = compare(current.hash, parent.hash, current.key, parent.key);
                        TreeNode<E> insertionPoint = parent;
                        if ((parent = (direction <= 0) ? parent.left : parent.right) == null) {
                            current.parent = insertionPoint;
                            if (direction <= 0)
                                insertionPoint.left = current;
                            else
                                insertionPoint.right = current;
                            root = balanceInsertion(root, current);
                            break;
                        }
                    }
            }
            moveRootToFront(tab, root);
        }

        /**
         * Converts this tree node structure back into a standard singly-linked list of {@link Node} instances.
         *
         * @return the head node of the new linked list
         */
        private Node<E> untreeify() {
            Node<E> head = null, tail = null;
            for (Node<E> current = this; current != null; current = current.next) {
                Node<E> newNode = new Node<>(current.hash, current.key, null);
                if (tail == null)
                    head = newNode;
                else
                    tail.next = newNode;
                tail = newNode;
            }
            return head;
        }
    }

    static class CustomSetSpliterator<E> implements Spliterator<E> {
        private final CustomSet<E> set;

        private Node<E> current;
        private Node<E>[] table;

        private long estimatedSize;

        private int expectedModCount;
        private int fence;
        private int index;

        private boolean includeNull;

        public CustomSetSpliterator(final CustomSet<E> set, final int origin, final int fence, final long estimatedSize,
                                    final int expectedModCount, final boolean isRoot) {
            this.set = set;
            this.index = origin;
            this.fence = fence;
            this.estimatedSize = estimatedSize;
            this.expectedModCount = expectedModCount;
            this.includeNull = isRoot && set.containsNull;
            this.table = set.table;
        }

        public int characteristics() {
            return (fence < 0 || estimatedSize == set.size ? Spliterator.SIZED : 0) | Spliterator.DISTINCT;
        }

        public long estimateSize() {
            getFence();
            return Math.max(estimatedSize, 0);
        }

        public void forEachRemaining(final Consumer<? super E> action) {
            requireNonNull(action);
            checkForComodification();
            if (includeNull) {
                includeNull = false;
                action.accept(null);
            }
            Node<E>[] tab = table;
            if (tab == null) {
                getFence();
                tab = table;
            }
            int high = getFence();
            for (Node<E> node = current; node != null; node = node.next)
                action.accept(node.key);
            current = null;
            for (int i = index; i < high; i++)
                for (Node<E> n = tab[i]; n != null; n = n.next)
                    action.accept(n.key);
            index = high;
            estimatedSize = 0;
            checkForComodification();
        }

        public boolean tryAdvance(final Consumer<? super E> action) {
            requireNonNull(action);
            checkForComodification();
            if (includeNull) {
                includeNull = false;
                action.accept(null);
                if (estimatedSize > 0)
                    estimatedSize--;
                return true;
            }
            Node<E>[] tab = table;
            if (tab == null) {
                getFence();
                tab = table;
            }
            int higherIndex = getFence();
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

        public Spliterator<E> trySplit() {
            int high = getFence(), low = index, mid = (low + high) >>> 1;
            if (estimatedSize <= 1 || low >= mid || current != null)
                return null;
            long half = estimatedSize >>> 1;
            estimatedSize -= half;
            index = mid;
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
                expectedModCount = set.modCount;
                higherIndex = fence = (table == null) ? 0 : table.length;
            }
            return higherIndex;
        }
    }
}