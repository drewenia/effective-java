import autovalue.shaded.com.google.common.hash.Funnel;
import autovalue.shaded.com.google.common.hash.PrimitiveSink;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PlayGround {
    public static void main(String[] args) {

        Funnel<PhoneNumber> funnel = new Funnel<PhoneNumber>() {
            @Override
            public void funnel(PhoneNumber phoneNumber, PrimitiveSink into) {
                into
                        .putShort(phoneNumber.getAreaCode())
                        .putShort(phoneNumber.getPrefix())
                        .putShort(phoneNumber.getLineNum());
            }
        };

        Map<PhoneNumber, String> map = new HashMap<>();
        map.put(new PhoneNumber((short) 707, (short) 867, (short) 5309), "Jenny");
        String s = map.get(new PhoneNumber((short) 707, (short) 867, (short) 5309));
        System.out.println(s);
    }
}

final class PhoneNumber {
    private final short areaCode, prefix, lineNum;

    public PhoneNumber(short areaCode, short prefix, short lineNum) {
        this.areaCode = rangeCheck(areaCode, 999, "area code");
        this.prefix = rangeCheck(prefix, 999, "prefix");
        this.lineNum = rangeCheck(lineNum, 9999, "line num");
    }

    private static short rangeCheck(int val, int max, String arg) {
        if (val < 0 || val > max)
            throw new IllegalArgumentException(arg + ": " + val);
        return (short) val;
    }

    public short getAreaCode() {
        return areaCode;
    }

    public short getPrefix() {
        return prefix;
    }

    public short getLineNum() {
        return lineNum;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PhoneNumber that)) return false;
        return areaCode == that.areaCode && prefix == that.prefix && lineNum == that.lineNum;
    }

    @Override
    public int hashCode() {
        int result = Short.hashCode(areaCode);
        result = 31 * result + Short.hashCode(prefix);
        result = 31 * result + Short.hashCode(lineNum);
        return result;
    }

    /**
     * Returns the string representation of this phone number.
     * The string consists of twelve characters whose format is
     * "XXX-YYY-ZZZZ"
     * , where XXX is the area code, YYY is the
     * prefix, and ZZZZ is the line number. Each of the capital
     * letters represents a single decimal digit.
     * *
     * * If any of the three parts of this phone number is too small
     * * to fill up its field, the field is padded with leading zeros.
     * * For example, if the value of the line number is 123, the last
     * * four characters of the string representation will be "0123"
     */

    @Override
    public String toString() {
        return String.format("%03d-%03d-%04d", areaCode, prefix, lineNum);
    }
}