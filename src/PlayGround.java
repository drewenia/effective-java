import autovalue.shaded.com.google.common.hash.Funnel;
import autovalue.shaded.com.google.common.hash.PrimitiveSink;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

import java.math.BigInteger;
import java.util.*;

public class PlayGround {
    public static void main(String[] args) {

    }

    public static BigInteger safeInstance(BigInteger val){
        return val.getClass() == BigInteger.class ? val : new BigInteger(val.toByteArray());
    }
}