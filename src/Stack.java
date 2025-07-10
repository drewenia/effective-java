import java.util.Arrays;
import java.util.EmptyStackException;

// Stack'i generic hale getirmek için yapılan ilk deneme - compile edilmeyecek!
public class Stack<E> {
    private Object[] elements;
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

    // Unchecked warning'in uygun şekilde suppress edilmesi
    public E pop() {
        if (size == 0) throw new EmptyStackException();

        // push, elements'in type'ının E olmasını gerektirir, bu yüzden cast doğrudur.
        @SuppressWarnings("unchecked")
        E result = (E) elements[--size];
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
}
