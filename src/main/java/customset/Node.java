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

    /**
     * Constructs a new node.
     *
     * @param hash the cached hash code of the key
     * @param key  the element key stored in this node
     * @param next the next node in the collision chain, or {@code null} if none
     */
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