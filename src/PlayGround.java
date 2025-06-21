import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class PlayGround {
    public static void main(String[] args) {
        EnumSet<Color> colors = EnumSet.of(Color.YELLOW, Color.BLACK, Color.WHITE);

        System.out.println(colors); // => [YELLOW, BLACK, WHITE]
    }


}

enum Color {
    RED, YELLOW, GREEN, BLUE, BLACK, WHITE
}



