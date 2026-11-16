package customset;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

class HashIterator<E> implements Iterator<E> {

    private final CustomSet<E> set;
    private final List<E> elements = new ArrayList<>();

    private int cursor;
    private E lastReturned;
    private boolean canRemove;
    private int expectedModCount;

    HashIterator(CustomSet<E> set) {
        this.set = set;
        this.expectedModCount = set.modCount;
        if (set.containsNull)
            elements.add(null);
        Node<E>[] tab = set.table;
        if (tab != null)
            for (Node<E> bucket : tab)
                for (Node<E> node = bucket; node != null; node = node.next)
                    elements.add(node.key);
    }

    @Override
    public boolean hasNext() {
        return cursor < elements.size();
    }

    @Override
    public E next() {
        checkForComodification();
        if (!hasNext())
            throw new NoSuchElementException();
        lastReturned = elements.get(cursor++);
        canRemove = true;
        return lastReturned;
    }

    @Override
    public void remove() {
        checkForComodification();
        if (!canRemove)
            throw new IllegalStateException();
        set.remove(lastReturned);
        canRemove = false;
        lastReturned = null;
        expectedModCount = set.modCount;
    }

    private void checkForComodification() {
        if (set.modCount != expectedModCount)
            throw new ConcurrentModificationException();
    }
}
