import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;


public class LoggerManager {
    private static LoggerManager instance;
    private final ServiceLoader<Logger> serviceLoader;

    private LoggerManager() {
        serviceLoader = ServiceLoader.load(Logger.class);
    }

    public static synchronized LoggerManager of() {
        if (instance == null)
            instance = new LoggerManager();
        return instance;
    }

    public void log(String message) {
        try {
            for (Logger logger : serviceLoader) {
                logger.log(message);
                System.out.println();
            }
        } catch (ServiceConfigurationError serviceError) {
            serviceError.printStackTrace();
        }
    }
}