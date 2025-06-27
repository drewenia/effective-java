import java.io.*;
import java.lang.ref.Cleaner;

public class PlayGround {
    public static void main(String[] args) throws Exception {
    }


    // catch bloğuyla birlikte try-with-resources
    static String firstLineOfFile(String path, String defaultValue) {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(path))) {
            return bufferedReader.readLine();
        } catch (IOException e) {
            return defaultValue;
        }
    }
}





