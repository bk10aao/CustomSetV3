package customset;

/**
 * Basic collision chain node stored directly within hash table buckets.
 *
 * <p>Each node holds an element key, its cached hash code, and a reference
 * to the next node in the singly-linked collision chain.
 *
 * @param <E> the type of element stored in this node
 */
class Node<E> {
    final int hash;
    final E key;
    Node<E> next;

    protected Node(final int hash, final E key, final Node<E> next) {
        this.hash = hash;
        this.key = key;
        this.next = next;
    }

    @Override
    public final String toString() {
        return String.valueOf(key);
    }
}
