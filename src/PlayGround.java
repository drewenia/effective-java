import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class PlayGround {
    public static void main(String[] args) {

    }
}

// Noninstantiable utility class
class UtilityClass{

    // Suppress default constructor for noninstantiability
    private UtilityClass(){
        throw new AssertionError();
    }
    // Remainder omitted
}