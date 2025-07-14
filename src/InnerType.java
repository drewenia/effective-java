public class InnerType {
    public static class Internal<T>{}

    public static void main(String[] args) {
        Internal<String> internal = new Internal<>();
        Class<?> classType = internal.getClass();
        System.out.println(classType + ", " + classType.getGenericSuperclass());
        // => class InnerType$Internal, class java.lang.Object
    }
}
