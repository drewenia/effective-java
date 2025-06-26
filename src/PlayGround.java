import java.lang.ref.Cleaner;

public class PlayGround {
    public static void main(String[] args) throws Exception {
        try(Room myRoom = new Room(7)){
            System.out.println("Goodbye");
        }
    }
}

// Cleaner’ı safety net olarak kullanan bir AutoCloseable sınıfı
class Room implements AutoCloseable{
    private static final Cleaner cleaner = Cleaner.create();

    // Temizlenmesi gereken resource. Room’a referans vermemeli!
    private static class State implements Runnable{
        int numJunkPiles; // Bu odadaki atık yığınlarının sayısı

        public State(int numJunkPiles){
            this.numJunkPiles = numJunkPiles;
        }

        // close metodu veya cleaner tarafından invoke edilir
        @Override
        public void run() {
            System.out.println("Cleaning room");
            numJunkPiles = 0;
        }
    }

    // Bu room'un state'i, cleanable ile paylaşılan
    private final State state;

    // Bizim cleanable’ımız. Room GC’ye uygun olduğunda temizler.
    private final Cleaner.Cleanable cleanable;

    public Room(int numJunkPiles){
        state = new State(numJunkPiles);
        cleanable = cleaner.register(this,state);
    }

    @Override
    public void close() throws Exception {
        cleanable.clean();
    }
}

class Adult{
    public static void main(String[] args) {
        try(Room myRoom = new Room(7)){
            System.out.println("Goodbye");
        }
    }
}



