import java.util.*;

public class PlayGround {
    public static void main(String[] args) {
        Map.Entry<String, Integer> entry = new AbstractMap.SimpleEntry<>("apple", 10);

        System.out.println(entry.getKey()); // => apple
        System.out.println(entry.getValue()); // => 10

        entry.setValue(20);
        System.out.println(entry.getValue()); // => 20
    }
}