import com.google.common.collect.ForwardingList;

import java.util.Collection;
import java.util.List;


public class AddLogging<E> extends ForwardingList<E> {
    final List<E> delegate; // backing list

    public AddLogging(List<E> delegate) {
        this.delegate = delegate;
    }

    @Override
    protected List<E> delegate() {
        return delegate;
    }

    @Override
    public void add(int index, E element) {
        System.out.println("logging");
        super.add(index,element);
    }

    @Override
    public boolean add(E element) {
        return standardAdd(element);
    }

    @Override
    public boolean addAll(Collection<? extends E> collection) {
        return standardAddAll(collection);
    }
}
