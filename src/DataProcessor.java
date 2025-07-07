public interface DataProcessor {
    default void process() {
        readData();
        processData();
        writeData();
    }

    void readData();
    void processData();
    void writeData();
}

class CSVDataProcessor implements DataProcessor{
    @Override
    public void readData() {
        System.out.println("Reading CSV data");
    }

    @Override
    public void processData() {
        System.out.println("Processing CSV data");
    }

    @Override
    public void writeData() {
        System.out.println("Writing CSV data");
    }
}
