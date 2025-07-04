// Immutable class with static factories instead of constructors
public final class Complex {
    private final double real;
    private final double imaginary;

    private Complex(double real, double imaginary) {
        this.real = real;
        this.imaginary = imaginary;
    }

    public static Complex valueOf(double real, double imaginary){
        return new Complex(real,imaginary);
    }

}
