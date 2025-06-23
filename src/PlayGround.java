import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class PlayGround {
    public static void main(String[] args) {
        Elvis elvis = Elvis.INSTANCE;
        elvis.leaveTheBuilding();
    }
}

enum Elvis {
    INSTANCE;

    public void leaveTheBuilding(){
        System.out.println("Im outta here!");
    }
}