package customset;

import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

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
 *
 * @author Benjamin Kane
 * LinkedIn - <a href="https://www.linkedin.com/in/benjamin-kane-81149482/"/>
 * GitHub account bk10aao - <a href="https://github.com/bk10aao"/>
 * Repository - <a href="https://github.com/bk10aao/CustomSetV3"/>
 */
public class CustomSet<E> implements Set<E>, Cloneable {
    private boolean CONTAINS_NULL = false;

    private final float loadFactor;

    private int size;
    private int threshold;

    private Node<E>[] table;

    private static final int MAXIMUM_CAPACITY = 1_073_741_824;
    private static final int MIN_TREEIFY_CAPACITY = 64;
    private static final int TREEIFY_THRESHOLD   = 8;
    private static final int UNTREEIFY_THRESHOLD = 6;
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;

    private transient int modCount = 0;

    /**
     * Constructs a new, empty set with an initial capacity of 16 and a default load factor of 0.75.
     */
    public CustomSet() {
        this(16);
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
     * @throws IllegalArgumentException if the initial capacity is negative or the load factor is non-positive, NaN, or infinite
     */
    public CustomSet(final int initialCapacity, final float loadFactor) {
        if (initialCapacity < 0 || loadFactor <= 0 || Float.isNaN(loadFactor) || Float.isInfinite(loadFactor))
            throw new IllegalArgumentException();
        int capacity = tableSizeFor(initialCapacity);
        this.table = (Node<E>[]) new Node[capacity];
        this.size = 0;
        this.loadFactor = loadFactor;
        this.threshold = (int) (capacity * loadFactor);
    }

    /**
     * Constructs a new set containing the elements of the specified collection.
     *
     * @param c the collection whose elements are to be placed into this set
     * @throws NullPointerException if the specified collection is null
     */
    public CustomSet(final Collection<E> c) {
        this(Math.max((int) (c.size() / DEFAULT_LOAD_FACTOR) + 1, 16), DEFAULT_LOAD_FACTOR);
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
        if (e == null) {
            if (CONTAINS_NULL)
                return false;
            CONTAINS_NULL = true;
            size++;
            modCount++;
            return true;
        }
        Node<E>[] tab = table;
        if (tab == null || tab.length == 0)
            tab = expand();
        int hash = hash(e);
        int n = tab.length;
        int index = hash & (n - 1);
        Node<E> first = tab[index];
        if (first == null)
            tab[index] = new Node<>(hash, e, null);
        else {
            if (first instanceof TreeNode) {
                TreeNode<E> existing = ((TreeNode<E>) first).putTreeVal(tab, hash, e);
                if (existing != null)
                    return false;
                size++;
                modCount++;
                if (size > threshold)
                    expand();
                return true;
            }
            Node<E> current = first;
            int binCount = 0;
            while (true) {
                if (current.hash == hash && (current.key == e || current.key.equals(e)))
                    return false;
                Node<E> nextNode = current.next;
                if (nextNode == null) {
                    current.next = new Node<>(hash, e, null);
                    if (binCount >= TREEIFY_THRESHOLD)
                        treeifyBin(tab, index);
                    break;
                }
                current = nextNode;
                binCount++;
            }
        }
        size++;
        if (size > threshold)
            expand();
        modCount++;
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
        long requiredCapacity = ((long) size + c.size()) * 4 / 3;
        int targetCapacity = (requiredCapacity > MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : (int) requiredCapacity;
        while (table == null || table.length < targetCapacity) {
            if (table == null || table.length >= MAXIMUM_CAPACITY)
                break;
            expand();
        }
        for (E e : c)
            add(e);
        return size > initialSize;
    }

    /**
     * Returns a shallow copy of this {@code CustomSet} instance: the elements themselves are not cloned.
     *
     * @return a shallow copy of this set
     */
    public CustomSet<E> clone() {
        return new CustomSet<>(this);
    }

    /**
     * Returns {@code true} if this set contains the specified element.
     *
     * @param o element whose presence in this set is to be tested
     * @return {@code true} if this set contains the specified element
     */
    public boolean contains(final Object o) {
        if (o == null)
            return CONTAINS_NULL;
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
            if (node.hash == hash && (node.key == o || node.key.equals(o)))
                return true;
        return false;
    }

    /**
     * Removes all the elements from this set. The set will be empty after this call returns.
     */
    public void clear() {
        Node<E>[] tab = table;
        CONTAINS_NULL = false;
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
        if (c instanceof Set<?> && c.size() > size)
            return false;
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

    /**
     * Removes the specified element from this set if it is present.
     *
     * @param o object to be removed from this set, if present
     * @return {@code true} if this set contained the specified element
     */
    public boolean remove(final Object o) {
        if (o == null) {
            boolean modified = false;
            if (CONTAINS_NULL) {
                CONTAINS_NULL = false;
                modified = true;
                modCount++;
                size--;
            }
            return modified;
        }
        Node<E>[] tab = table;
        if (tab == null || tab.length == 0)
            return false;
        int hash = hash(o);
        int index = hash & (tab.length - 1);
        Node<E> first = tab[index];
        if (first == null)
            return false;
        if (first instanceof TreeNode) {
            TreeNode<E> node = ((TreeNode<E>) first).getTreeNode(hash, o);
            if (node != null) {
                node.removeTreeNode(tab);
                size--;
                modCount++;
                return true;
            }
            return false;
        }
        Node<E> current = first;
        Node<E> previous = null;
        while (current != null) {
            if (current.hash == hash && (current.key == o || current.key.equals(o))) {
                removeNode(previous, index, current);
                modCount++;
                return true;
            }
            previous = current;
            current = current.next;
        }
        return false;
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
                E element = it.next();
                if (c.contains(element)) {
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

    /**
     * Returns an array containing all the elements in this set.
     *
     * @return an array containing all the elements in this set
     */
    public E[] toArray() {
        E[] arr = (E[]) new Object[size];
        if (table == null || size == 0)
            return arr;
        int index = 0;
        if (CONTAINS_NULL)
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
     * @param a the array into which the elements of this set are to be stored, if it is big enough; otherwise, a new array of the same runtime type is allocated for this purpose
     * @param <T> the runtime type of the array to contain the collection
     * @return an array containing all the elements in this set
     * @throws NullPointerException if the specified array is null
     */
    public <T> T[] toArray(final T[] a) {
        requireNonNull(a);
        T[] arr = a.length < size ? (T[]) newInstance(a.getClass().getComponentType(), size) : a;
        int index = 0;
        if (CONTAINS_NULL)
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
        if (CONTAINS_NULL) {
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

    private Node<E>[] expand() {
        Node<E>[] oldTab = table;
        int oldCap = (oldTab == null) ? 0 : oldTab.length;
        int newCap;
        if (oldCap == 0) {
            newCap = 16;
            threshold = (int) (newCap * loadFactor);
            table = (Node<E>[]) new Node[newCap];
            return table;
        }
        if (oldCap >= MAXIMUM_CAPACITY) {
            threshold = Integer.MAX_VALUE;
            return oldTab;
        }
        newCap = oldCap << 1;
        threshold = (int) (newCap * loadFactor);
        Node<E>[] newTab = (Node<E>[]) new Node[newCap];
        table = newTab;
        for (int j = 0; j < oldCap; ++j) {
            Node<E> e = oldTab[j];
            if (e != null) {
                oldTab[j] = null;
                if (e.next == null)
                    newTab[e.hash & (newCap - 1)] = e;
                else if (e instanceof TreeNode)
                    ((TreeNode<E>) e).split(newTab, j, oldCap);
                else {
                    Node<E> loHead = null, loTail = null;
                    Node<E> hiHead = null, hiTail = null;
                    Node<E> next;
                    do {
                        next = e.next;
                        if ((e.hash & oldCap) == 0) {
                            if (loTail == null)
                                loHead = e;
                            else
                                loTail.next = e;
                            loTail = e;
                        } else {
                            if (hiTail == null)
                                hiHead = e;
                            else
                                hiTail.next = e;
                            hiTail = e;
                        }
                    } while ((e = next) != null);
                    if (loTail != null) {
                        loTail.next = null;
                        newTab[j] = loHead;
                    }
                    if (hiTail != null) {
                        hiTail.next = null;
                        newTab[j + oldCap] = hiHead;
                    }
                }
            }
        }
        return newTab;
    }

    private void removeNode(final Node<E> previous, final int index, final Node<E> entry) {
        Node<E>[] tab = table;
        if (previous == null)
            tab[index] = entry.next;
        else
            previous.next = entry.next;
        size--;
    }

    private static int tableSizeFor(final int cap) {
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }

    private void treeifyBin(final Node<E>[] tab, final int index) {
        if (tab == null || tab.length < MIN_TREEIFY_CAPACITY) {
            expand();
            return;
        }
        Node<E> e = tab[index];
        if (e != null) {
            TreeNode<E> hd = null, tl = null;
            do {
                TreeNode<E> p = new TreeNode<>(e.hash, e.key, null);
                if (tl == null)
                    hd = p;
                else {
                    p.previous = tl;
                    tl.next = p;
                }
                tl = p;
            } while ((e = e.next) != null);
            tab[index] = hd;
            hd.treeify(tab);
        }
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
            current = next = null;
            index = 0;
            includeNull = CONTAINS_NULL;
            Node<E>[] tab = table;
            if (tab != null && size > 0)
                if (index < tab.length && (next = tab[index++]) == null)
                    while (index < tab.length && (next = tab[index++]) == null) { }
        }

        public boolean hasNext() {
            checkForComodification();
            return next != null;
        }

        public E next() {
            checkForComodification();
            if (includeNull) {
                includeNull = false;
                removeNull = true;
                current = null;
                return null;
            }
            Node<E>[] tab = table;
            Node<E> e = next;
            if (e == null)
                throw new NoSuchElementException();
            if ((next = (current = e).next) == null && tab != null)
                while (index < tab.length && (next = tab[index++]) == null) { }
            return e.key;
        }

        public void remove() {
            checkForComodification();
            if (removeNull) {
                CustomSet.this.remove(null);
                removeNull = false;
                expectedModCount = modCount;
                return;
            }
            if (current == null)
                throw new IllegalArgumentException();
            Object keyToRemove = current.key;
            current = null;
            CustomSet.this.remove(keyToRemove);
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

        public final String toString() {
            return String.valueOf(key);
        }
    }

    private static int hash(final Object key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }


    private static final class TreeNode<E> extends Node<E> {
        private TreeNode<E> left;
        private TreeNode<E> right;
        private TreeNode<E> parent;
        private TreeNode<E> previous;

        private boolean RED;

        private TreeNode(final int hash, final E key, final Node<E> next) {
            super(hash, key, next);
        }

        /**
         * Re-balances the red-black tree after a node deletion operation.
         *
         * @param root the root of the tree
         * @param x    the node being adjusted or its replacement
         * @return the updated root of the tree
         */
        private static <E> TreeNode<E> balanceDeletion(TreeNode<E> root, TreeNode<E> x) {
            for (TreeNode<E> grandparentNode, parentLeft, parentRight; ; ) {
                if (x == null || x == root)
                    return root;
                else if ((grandparentNode = x.parent) == null) {
                    x.RED = false;
                    return x;
                } else if (x.RED) {
                    x.RED = false;
                    return root;
                } else if ((parentLeft = grandparentNode.left) == x) {
                    if ((parentRight = grandparentNode.right) != null && parentRight.RED) {
                        parentRight.RED = false;
                        grandparentNode.RED = true;
                        root = rotateLeft(root, grandparentNode);
                        parentRight = (grandparentNode = x.parent) == null ? null : grandparentNode.right;
                    }
                    if (parentRight == null)
                        x = grandparentNode;
                    else {
                        TreeNode<E> siblingLeft = parentRight.left, siblingRight = parentRight.right;
                        if ((siblingRight == null || !siblingRight.RED) && (siblingLeft == null || !siblingLeft.RED)) {
                            parentRight.RED = true;
                            x = grandparentNode;
                        } else {
                            if (siblingRight == null || !siblingRight.RED) {
                                siblingLeft.RED = false;
                                parentRight.RED = true;
                                root = rotateRight(root, parentRight);
                                parentRight = (grandparentNode = x.parent) == null ? null : grandparentNode.right;
                            }
                            if (parentRight != null) {
                                parentRight.RED = grandparentNode.RED;
                                if ((siblingRight = parentRight.right) != null)
                                    siblingRight.RED = false;
                            }
                            if (grandparentNode != null) {
                                grandparentNode.RED = false;
                                root = rotateLeft(root, grandparentNode);
                            }
                            x = root;
                        }
                    }
                } else {
                    if (parentLeft != null && parentLeft.RED) {
                        parentLeft.RED = false;
                        grandparentNode.RED = true;
                        root = rotateRight(root, grandparentNode);
                        parentLeft = (grandparentNode = x.parent) == null ? null : grandparentNode.left;
                    }
                    if (parentLeft == null)
                        x = grandparentNode;
                    else {
                        TreeNode<E> siblingLeft = parentLeft.left, siblingRight = parentLeft.right;
                        if ((siblingLeft == null || !siblingLeft.RED) && (siblingRight == null || !siblingRight.RED)) {
                            parentLeft.RED = true;
                            x = grandparentNode;
                        } else {
                            if (siblingLeft == null || !siblingLeft.RED) {
                                siblingRight.RED = false;
                                parentLeft.RED = true;
                                root = rotateLeft(root, parentLeft);
                                parentLeft = (grandparentNode = x.parent) == null ? null : grandparentNode.left;
                            }
                            if (parentLeft != null) {
                                parentLeft.RED = grandparentNode.RED;
                                if ((siblingLeft = parentLeft.left) != null)
                                    siblingLeft.RED = false;
                            }
                            if (grandparentNode != null) {
                                grandparentNode.RED = false;
                                root = rotateRight(root, grandparentNode);
                            }
                            x = root;
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
            currentNode.RED = true;
            for (TreeNode<E> parentNode, grandparentNode, grandparentLeft, grandparentRight; ; ) {
                if ((parentNode = currentNode.parent) == null) {
                    currentNode.RED = false;
                    return currentNode;
                } else if (!parentNode.RED || (grandparentNode = parentNode.parent) == null)
                    return root;
                if (parentNode == grandparentNode.left) {
                    if ((grandparentRight = grandparentNode.right) != null && grandparentRight.RED) {
                        grandparentRight.RED = false;
                        parentNode.RED = false;
                        grandparentNode.RED = true;
                        currentNode = grandparentNode;
                    } else {
                        if (currentNode == parentNode.right) {
                            root = rotateLeft(root, currentNode = parentNode);
                            grandparentNode = (parentNode = currentNode.parent) == null ? null : parentNode.parent;
                        }
                        if (parentNode != null) {
                            parentNode.RED = false;
                            if (grandparentNode != null) {
                                grandparentNode.RED = true;
                                root = rotateRight(root, grandparentNode);
                            }
                        }
                    }
                } else if ((grandparentLeft = grandparentNode.left) != null && grandparentLeft.RED) {
                    grandparentLeft.RED = false;
                    parentNode.RED = false;
                    grandparentNode.RED = true;
                    currentNode = grandparentNode;
                } else {
                    if (currentNode == parentNode.left) {
                        root = rotateRight(root, currentNode = parentNode);
                        grandparentNode = (parentNode = currentNode.parent) == null ? null : parentNode.parent;
                    }
                    if (parentNode != null) {
                        parentNode.RED = false;
                        if (grandparentNode != null) {
                            grandparentNode.RED = true;
                            root = rotateLeft(root, grandparentNode);
                        }
                    }
                }
            }
        }

        /**
         * Compares two keys for ordering purposes within the red-black tree, utilizing
         * hash codes, {@link Comparable} implementation if available, and fallback identity hash codes.
         *
         * @param h1 the hash of the first key
         * @param h2 the hash of the second key
         * @param v1 the first key
         * @param v2 the second key
         * @return a negative integer, zero, or a positive integer as the first key is less than, equal to, or greater than the second
         */
        private int compare(final int h1, final int h2, final E v1, final E v2) {
            if (h1 != h2)
                return h1 < h2 ? -1 : 1;
            if (v1 instanceof Comparable) {
                int cmp = ((Comparable<? super E>) v1).compareTo(v2);
                if (cmp != 0)
                    return cmp;
            }
            return System.identityHashCode(v1) <= System.identityHashCode(v2) ? -1 : 1;
        }

        /**
         * Finds and returns the tree node matching the specified hash and key starting from this node.
         *
         * @param hash the hash value to look up
         * @param o    the key object to look up
         * @return the matching tree node, or {@code null} if not found
         */
        private TreeNode<E> find(final int hash, final Object o) {
            TreeNode<E> p = this;
            do {
                int ph = p.hash;
                E pe = p.key;
                if (ph > hash)
                    p = p.left;
                else if (ph < hash)
                    p = p.right;
                else if (Objects.equals(o, pe))
                    return p;
                else
                    p = (compare(hash, ph, (E) o, pe) < 0) ? p.left : p.right;
            } while (p != null);
            return null;
        }

        /**
         * Locates a tree node by hash and key, beginning at the root of the tree structure.
         *
         * @param hash the hash value to look up
         * @param o    the key object to look up
         * @return the matching tree node, or {@code null} if not found
         */
        private TreeNode<E> getTreeNode(final int hash, final Object o) {
            return ((parent != null) ? root() : this).find(hash, o);
        }

        /**
         * Ensures that the given root node of a red-black tree bin is placed at the front
         * of the bucket's collision chain in the table.16 * @param tab  the hash table array
         *
         * @param root the root node of the tree
         */
        private static <E> void moveRootToFront(final Node<E>[] tab, final TreeNode<E> root) {
            int n;
            if (root != null && tab != null && (n = tab.length) > 0) {
                int index = (n - 1) & root.hash;
                TreeNode<E> first = (TreeNode<E>) tab[index];
                if (root != first) {
                    Node<E> rn;
                    tab[index] = root;
                    TreeNode<E> rp = root.previous;
                    if ((rn = root.next) != null)
                        ((TreeNode<E>) rn).previous = rp;
                    if (rp != null)
                        rp.next = rn;
                    if (first != null)
                        first.previous = root;
                    root.next = first;
                    root.previous = null;
                }
            }
        }

        /**
         * Inserts a new key-value mapping into the red-black tree structure.
         *
         * @param tab the hash table array
         * @param h   the hash value
         * @param v   the value
         * @return the existing node if the key is already present, or {@code null} if a new node was inserted
         */
        private TreeNode<E> putTreeVal(final Node<E>[] tab, final int h, final E v) {
            TreeNode<E> root = (parent != null) ? root() : this;
            for (TreeNode<E> p = root; ; ) {
                int dir;
                int ph = p.hash;
                E pv = p.key;
                if (ph > h)
                    dir = -1;
                else if (ph < h)
                    dir = 1;
                else if (pv == v || (v.equals(pv)))
                    return p;
                else
                    dir = compare(h, ph, v, pv);
                TreeNode<E> xp = p;
                if ((p = (dir <= 0) ? p.left : p.right) == null) {
                    Node<E> xpn = xp.next;
                    TreeNode<E> x = new TreeNode<>(h, v, xpn);
                    if (dir <= 0)
                        xp.left = x;
                    else
                        xp.right = x;
                    xp.next = x;
                    x.parent = x.previous = xp;
                    if (xpn != null)
                        ((TreeNode<E>) xpn).previous = x;
                    moveRootToFront(tab, balanceInsertion(root, x));
                    return null;
                }
            }
        }

        /**
         * Removes this node from the red-black tree and re-balances the tree structure,
         * untreeifying the bin if the remaining nodes fall below the threshold.
         *
         * @param tab the hash table array
         */
        private void removeTreeNode(final Node<E>[] tab) {
            int n;
            if (tab == null || (n = tab.length) == 0)
                return;
            int index = (n - 1) & hash;
            TreeNode<E> first = (TreeNode<E>) tab[index];
            TreeNode<E> root = first;
            TreeNode<E> rl;
            TreeNode<E> successor = (TreeNode<E>) next;
            TreeNode<E> predecessor = previous;
            if (predecessor == null)
                tab[index] = first = successor;
            else
                predecessor.next = successor;
            if (successor != null)
                successor.previous = predecessor;
            if (first == null)
                return;
            if (root.parent != null)
                root = root.root();
            if (root.right == null || (rl = root.left) == null || rl.left == null) {
                tab[index] = first.untreeify();
                return;
            }

            TreeNode<E> p = this, pl = left, pr = right, replacement;
            if (pl != null && pr != null) {
                TreeNode<E> s = pr, sl;
                while ((sl = s.left) != null)
                    s = sl;
                boolean c = s.RED;
                s.RED = p.RED;
                p.RED = c;
                TreeNode<E> sr = s.right;
                TreeNode<E> pp = p.parent;
                if (s == pr) {
                    p.parent = s;
                    s.right = p;
                } else {
                    TreeNode<E> sp = s.parent;
                    if ((p.parent = sp) != null)
                        if (s == sp.left)
                            sp.left = p;
                        else
                            sp.right = p;
                    s.right = pr;
                    pr.parent = s;
                }
                p.left = null;
                if ((p.right = sr) != null)
                    sr.parent = p;
                s.left = pl;
                pl.parent = s;
                if ((s.parent = pp) == null)
                    root = s;
                else if (p == pp.left)
                    pp.left = s;
                else
                    pp.right = s;
                replacement = (sr != null) ? sr : s;
            } else
                replacement = requireNonNullElseGet(pl, () -> requireNonNullElse(pr, p));
            if (replacement != p) {
                TreeNode<E> pp = replacement.parent = p.parent;
                if (pp == null)
                    root = replacement;
                else if (p == pp.left)
                    pp.left = replacement;
                else
                    pp.right = replacement;
                p.left = p.right = p.parent = null;
            }
            TreeNode<E> r = p.RED ? root : balanceDeletion(root, replacement);
            if (replacement == p) {
                TreeNode<E> pp = p.parent;
                p.parent = null;
                if (pp != null)
                    if (p == pp.left)
                        p.left = null;
                    else if (p == pp.right)
                        p.right = null;
            }
            moveRootToFront(tab, r);
        }

        /**
         * Traverses parent links to find and return the root node of this red-black tree.
         *
         * @return the root node of the tree
         */
        private TreeNode<E> root() {
            for (TreeNode<E> r = this, p; ; ) {
                if ((p = r.parent) == null)
                    return r;
                r = p;
            }
        }

        /**
         * Performs a left rotation on the red-black tree around the specified node.
         *
         * @param root the current root of the tree
         * @param p    the pivot node for rotation
         * @return the new root of the tree
         */
        private static <E> TreeNode<E> rotateLeft(TreeNode<E> root, final TreeNode<E> p) {
            TreeNode<E> r = p.right;
            p.right = r.left;
            if (r.left != null)
                r.left.parent = p;
            r.parent = p.parent;
            if (p.parent == null)
                root = r;
            else if (p.parent.left == p)
                p.parent.left = r;
            else
                p.parent.right = r;
            r.left = p;
            p.parent = r;
            return root;
        }

        /**
         * Performs a right rotation on the red-black tree around the specified node.
         *
         * @param root the current root of the tree
         * @param p    the pivot node for rotation
         * @return the new root of the tree
         */
        private static <E> TreeNode<E> rotateRight(TreeNode<E> root, final TreeNode<E> p) {
            TreeNode<E> l = p.left;
            p.left = l.right;
            if (l.right != null)
                l.right.parent = p;
            l.parent = p.parent;
            if (p.parent == null)
                root = l;
            else if (p.parent.right == p)
                p.parent.right = l;
            else
                p.parent.left = l;
            l.right = p;
            p.parent = l;
            return root;
        }

        /**
         * Splits a tree bin into low and high bins during table resizing,
         * untreeifying them if they drop below the threshold.
         *
         * @param tab   the hash table array
         * @param index the current bucket index
         * @param bit   the bit mask used for partitioning
         */
        private void split(final Node<E>[] tab, final int index, final int bit) {
            TreeNode<E> b = this;
            TreeNode<E> loHead = null, loTail = null;
            TreeNode<E> hiHead = null, hiTail = null;
            int lc = 0, hc = 0;
            for (TreeNode<E> e = b, next; e != null; e = next) {
                next = (TreeNode<E>) e.next;
                e.next = null;
                if ((e.hash & bit) == 0) {
                    if ((e.previous = loTail) == null)
                        loHead = e;
                    else
                        loTail.next = e;
                    loTail = e;
                    ++lc;
                } else {
                    if ((e.previous = hiTail) == null)
                        hiHead = e;
                    else
                        hiTail.next = e;
                    hiTail = e;
                    ++hc;
                }
            }

            if (loHead != null)
                if (lc <= UNTREEIFY_THRESHOLD)
                    tab[index] = loHead.untreeify();
                else {
                    tab[index] = loHead;
                    if (hiHead != null)
                        loHead.treeify(tab);
                }
            if (hiHead != null)
                if (hc <= UNTREEIFY_THRESHOLD)
                    tab[index + bit] = hiHead.untreeify();
                else {
                    tab[index + bit] = hiHead;
                    if (loHead != null)
                        hiHead.treeify(tab);
                }
        }

        /**
         * Forms a balanced red-black tree out of the linked list nodes starting from this node.
         *
         * @param tab the hash table array
         */
        private void treeify(final Node<E>[] tab) {
            TreeNode<E> root = null;
            for (TreeNode<E> x = this, next; x != null; x = next) {
                next = (TreeNode<E>) x.next;
                x.left = x.right = null;
                if (root == null) {
                    x.parent = null;
                    x.RED = false;
                    root = x;
                } else
                    for (TreeNode<E> p = root; ; ) {
                        int dir = compare(x.hash, p.hash, x.key, p.key);
                        TreeNode<E> xp = p;
                        if ((p = (dir <= 0) ? p.left : p.right) == null) {
                            x.parent = xp;
                            if (dir <= 0)
                                xp.left = x;
                            else
                                xp.right = x;
                            root = balanceInsertion(root, x);
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
            Node<E> hd = null, tl = null;
            for (Node<E> q = this; q != null; q = q.next) {
                Node<E> p = new Node<>(q.hash, q.key, null);
                if (tl == null)
                    hd = p;
                else
                    tl.next = p;
                tl = p;
            }
            return hd;
        }
    }
}