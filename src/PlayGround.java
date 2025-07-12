import java.util.*;

public class PlayGround {

    public static void main(String[] args) {
        List<String> argList = Arrays.asList("ocean", "joe", "david");
        System.out.println(argList); // => [ocean, joe, david]

        swap(argList, 0, argList.size() - 1);
        System.out.println(argList); // => [david, joe, ocean]
    }

    public static void swap(List<?> list, int i, int j) {
        swapHelper(list, i, j);
    }

    // Wildcard capture için private helper method
    private static <E> void swapHelper(List<E> list, int i, int j) {
        list.set(i, list.set(j, list.get(i)));
    }
}