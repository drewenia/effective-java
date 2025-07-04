public class FinalFieldExample {
    final int x;
    int y;
    static FinalFieldExample f;

    public FinalFieldExample() {
        this.x = 3;
        this.y = 4;
    }

    static void writer() {
        f = new FinalFieldExample();
    }

    static void reader() {
        if (f != null) {
            int i = f.x; // 3 ü görmek garanti
            int j = f.y; // 0'ı görebiliyordu.
        }
    }
}
