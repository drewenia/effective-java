import java.io.FileWriter;
import java.io.IOException;

public class FileLogger implements Logger {
    @Override
    public void log(String message) {
        try (FileWriter fileWriter = new FileWriter("output.txt", true)) {
            fileWriter.write("[File] " + message + "\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

