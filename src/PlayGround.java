import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class PlayGround {
    public static void main(String[] args) {
        long sum = sum();
        System.out.println(sum);
    }

    // Berbat yavaş! Object creation’ını fark edebiliyor musun?
    private static long sum() {
        long sum = 0L;
        for (long i = 0; i < Integer.MAX_VALUE; i++) {
            sum += i;
        }
        return sum;
    }

}