package customset;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import static customset.CustomSet.UNTREEIFY_THRESHOLD;
import static customset.CustomSet.keysMatch;
import static java.lang.System.identityHashCode;
import static java.util.Objects.requireNonNullElse;
import static java.util.Objects.requireNonNullElseGet;

final class TreeNode<E> extends Node<E> {

    private TreeNode<E> left;
    private TreeNode<E> right;
    private TreeNode<E> parent;
    TreeNode<E> previous;
    private boolean isRed;

    /**
     * Constructs a new Red-Black tree node.
     *
     * @param hash the cached hash code of the key
     * @param key  the element key stored in this node
     * @param next the next node in the collision chain, or {@code null} if none
     */
    TreeNode(final int hash, final E key, final Node<E> next) {
        super(hash, key, next);
    }

    /**
     * Retrieves the tree node matching the specified hash and object key from the tree root.
     *
     * @param hash the hash code of the object
     * @param o    the object key to search for
     * @return the matching tree node, or {@code null} if not present
     */
    TreeNode<?> getTreeNode(final int hash, final Object o) {
        return ((parent != null) ? root() : this).find(hash, o, null);
    }

    /**
     * Inserts a value into the Red-Black tree bin, treeifying or balancing as necessary.
     *
     * @param tab   the hash table array
     * @param hash  the hash code of the value
     * @param value the element value to insert
     * @return the existing tree node if already present, or {@code null} upon successful insertion
     */
    TreeNode<E> putTreeVal(final Node<E>[] tab, final int hash, final E value) {
        Class<?> kc = null;
        boolean searched = false;
        TreeNode<E> root = (parent != null) ? root() : this;
        for (TreeNode<E> p = root; ; ) {
            int dir;
            if (p.hash > hash)
                dir = -1;
            else if (p.hash < hash)
                dir = 1;
            else if (keysMatch(value, p))
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
     * Removes this node from the Red-Black tree, maintains the secondary
     * linked-list view, rebalances, and converts back to a plain list
     * when the remaining tree falls below the untreeify threshold.
     *
     * @param tab the hash table array
     */
    void removeTreeNode(final Node<E>[] tab) {
        int tableLength;
        if (tab == null || (tableLength = tab.length) == 0)
            return;
        final int index = (tableLength - 1) & hash;
        TreeNode<E> first = (TreeNode<E>) tab[index];
        TreeNode<E> root = first;
        TreeNode<E> nextLinkedNode = (TreeNode<E>) next;
        TreeNode<E> previousLinkedNode = previous;
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
        int binCount = 0;
        TreeNode<E> hd = root;
        while (hd.previous != null)
            hd = hd.previous;
        for (TreeNode<E> node = hd; node != null; node = (TreeNode<E>) node.next)
            binCount++;
        TreeNode<E> rl;
        if (binCount <= UNTREEIFY_THRESHOLD || root.right == null || (rl = root.left) == null || rl.left == null) {
            TreeNode<E> headNode = root;
            while (headNode.previous != null)
                headNode = headNode.previous;
            tab[index] = headNode.untreeify();
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
                inOrderSuccessor.right = nodeToDelete;
            } else {
                TreeNode<E> successorParent = inOrderSuccessor.parent;
                if ((nodeToDelete.parent = successorParent) != null)
                    if (inOrderSuccessor == successorParent.left)
                        successorParent.left = nodeToDelete;
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
                parentNode.left = inOrderSuccessor;
            else
                parentNode.right = inOrderSuccessor;
            replacement = (successorRight != null) ? successorRight : nodeToDelete;
        } else
            replacement = requireNonNullElseGet(leftChild, () -> requireNonNullElse(rightChild, nodeToDelete));
        if (replacement != nodeToDelete) {
            TreeNode<E> pp = replacement.parent = nodeToDelete.parent;
            if (pp == null)
                root = replacement;
            else if (nodeToDelete == pp.left)
                pp.left = replacement;
            else
                pp.right = replacement;
            nodeToDelete.left = nodeToDelete.right = nodeToDelete.parent = null;
        }
        TreeNode<E> r = nodeToDelete.isRed ? root : balanceDeletion(root, replacement);
        if (replacement == nodeToDelete) {
            TreeNode<E> pp = nodeToDelete.parent;
            nodeToDelete.parent = null;
            if (pp != null)
                if (nodeToDelete == pp.left)
                    pp.left = null;
                else if (nodeToDelete == pp.right)
                    pp.right = null;
        }
        moveRootToFront(tab, r);
    }

    /**
     * Splits a tree bin into low and high bins during table resizing,
     * untreeifying them if they drop below the threshold.
     *
     * @param tab         the hash table array
     * @param index       the current bucket index
     * @param capacityBit the capacity bit mask used for partitioning
     */
    void split(final Node<E>[] tab, final int index, final int capacityBit) {
        TreeNode<E> binRoot = this, lowHead = null, lowTail = null, highHead = null, highTail = null;
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

    /**
     * Forms a balanced Red-Black tree out of the linked list nodes starting from this node.
     *
     * @param tab the hash table array
     */
    void treeify(final Node<E>[] tab) {
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
    Node<E> untreeify() {
        Node<E> head = null, tail = null;
        TreeNode<E> current = this;
        while (current.previous != null)
            current = current.previous;
        while (current != null) {
            Node<E> newNode = new Node<>(current.hash, current.key, null);
            if (tail == null)
                head = newNode;
            else
                tail.next = newNode;
            tail = newNode;
            current = (TreeNode<E>) current.next;
        }
        return head;
    }

    /**
     * Re-balances the Red-Black tree after a node deletion.
     *
     * @param root      the current root of the tree
     * @param fixupNode the node that replaced the deleted node (or the deleted node itself if it was a leaf)
     * @return the new root of the tree
     */
    private static <E> TreeNode<E> balanceDeletion(TreeNode<E> root, TreeNode<E> fixupNode) {
        for (TreeNode<E> parentNode, parentLeftChild, parentRightChild; ;) {
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
                    parentNode.isRed = true;
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
                    parentNode.isRed = true;
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
     * Re-balances the Red-Black tree after a new node insertion.
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

    /**
     * Compares two tree node entries by hash code, comparable interface, or tie-breaker order.
     *
     * @param hash1 the hash code of the first value
     * @param hash2 the hash code of the second value
     * @param v1    the first value object
     * @param v2    the second value object
     * @return a negative integer, zero, or a positive integer as the first value is less than, equal to, or greater than the second
     */
    private static int compare(final int hash1, final int hash2, final Object v1, final Object v2) {
        if (hash1 != hash2)
            return hash1 < hash2 ? -1 : 1;
        Class<?> kc = comparableClassFor(v1);
        int cmp;
        if (kc != null && (cmp = compareComparables(kc, v1, v2)) != 0)
            return cmp;
        return tieBreakOrder(v1, v2);
    }

    /**
     * Returns the comparable class for the specified object if it implements {@link Comparable} directly.
     *
     * @param x the object to inspect
     * @return the comparable class type, or {@code null} if none
     */
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

    /**
     * Compares two comparable objects of the specified class type.
     *
     * @param kc the comparable class type
     * @param k  the first comparable object
     * @param x  the second object to compare against
     * @return a negative integer, zero, or a positive integer
     */
    private static int compareComparables(final Class<?> kc, final Object k, final Object x) {
        return (x == null || x.getClass() != kc) ? 0 : ((Comparable) k).compareTo(x);
    }

    /**
     * Finds and returns the tree node matching the specified hash and target object.
     *
     * @param hash   the hash code of the target object
     * @param target the target object to search for
     * @param kc     the comparable class type, if applicable
     * @return the matching {@code customset.TreeNode}, or {@code null} if not found
     */
    private TreeNode<E> find(final int hash, final Object target, Class<?> kc) {
        TreeNode<E> p = this;
        do {
            TreeNode<E> pl = p.left, pr = p.right, q;
            int dir;
            if (p.hash > hash)
                p = pl;
            else if (p.hash < hash)
                p = pr;
            else if (keysMatch(target, p))
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

    /**
     * Traverses parent links to find and return the root node of this Red-Black tree.
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
     * Performs a left rotation on the Red-Black tree around the specified pivot node.
     *
     * @param root      the current root of the tree
     * @param pivotNode the pivot node for rotation
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
     * Performs a right rotation on the Red-Black tree around the specified pivot node.
     *
     * @param root      the current root of the tree
     * @param pivotNode the pivot node for rotation
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
     * Determines a tie-breaker comparison order for non-comparable objects based on class name and identity hash code.
     *
     * @param a the first object
     * @param b the second object
     * @return a negative or positive integer establishing strict ordering
     */
    private static int tieBreakOrder(final Object a, final Object b) {
        int d;
        if (a == null || b == null || (d = a.getClass().getName().compareTo(b.getClass().getName())) == 0)
            d = identityHashCode(a) <= identityHashCode(b) ? -1 : 1;
        return d;
    }
}