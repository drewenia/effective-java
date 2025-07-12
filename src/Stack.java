import java.util.*;

// Wildcard types kullanan bulk method'lara sahip generic stack
public class Stack<E> {
    private E[] elements;
    private int size = 0;
    private static final int DEFAULT_INITIAL_CAPACITY = 16;

    // elements array'i yalnızca push(E) ile gelen E instance'larını içerecektir.
    /* Bu, type safety'yi sağlamak için yeterlidir, ancak array'in runtime type'ı E[] olmayacaktır; her zaman Object[]
       olacaktır!
    */
    @SuppressWarnings("unchecked")
    public Stack() {
        elements = (E[]) new Object[DEFAULT_INITIAL_CAPACITY];
    }

    public void push(E e) {
        ensureCapacity();
        elements[size++] = e;
    }

    public E pop() {
        if (size == 0) throw new EmptyStackException();

        E result = elements[--size];
        elements[size] = null; // Eski referansı eliminate et
        return result;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    private void ensureCapacity() {
        if (elements.length == size)
            elements = Arrays.copyOf(elements, 2 * size + 1);
    }

    // E producer olarak görev yapan bir parameter için wildcard type
    public void pushAll(Iterable<? extends E> src) {
        for (E e : src) {
            push(e);
        }
    }


    // E consumer olarak görev yapan parameter için wildcard type
    public void popAll(Collection<? super E> dst) {
        while (!isEmpty()) {
            dst.add(pop());
        }
    }

    public static void main(String[] args) {
        Stack<Number> numberStack = new Stack<>();
        Iterable<Integer> integers = Arrays.asList(3, 1, 4, 1, 5, 9);
        numberStack.pushAll(integers);

        Collection<Object> objects = new ArrayList<>();
        numberStack.popAll(objects);
        System.out.println(objects); // => [9, 5, 1, 4, 1, 3]
    }
}
