import java.util.*;
import java.util.function.UnaryOperator;

public class PlayGround {

    public static void main(String[] args) {
        List<Integer> integerList = List.of(1,11,22,4,9,23);
        System.out.println(max(integerList)); // => 23
    }

    // Bir collection’daki maksimum değeri döner — recursive type bound kullanır
    public static <E extends Comparable<E>> E max(Collection<E> collection) {
        if (collection.isEmpty()) throw new IllegalArgumentException("Empty collection");

        E result = null;
        for (E e : collection) {
            if (result == null || e.compareTo(result) > 0) {
                result = Objects.requireNonNull(e);
            }
        }
        return result;
    }

    private static UnaryOperator<Object> IDENTITY_FN = (t) -> t;

    @SuppressWarnings("unchecked")
    public static <T> UnaryOperator<T> identityFunction() {
        return (UnaryOperator<T>) IDENTITY_FN;
    }
}