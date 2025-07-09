import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PlayGround {
    public static void main(String[] args) {

    }

    // Unbounded wildcard type kullanır – typesafe ve esnektir.
    static int numElementsInCommon(Set<?> s1, Set<?> s2) {
        int result = 0;
        for (Object o1 : s1) {
            if (s2.contains(o1))
                result++;
        }
        return result;
    }
}