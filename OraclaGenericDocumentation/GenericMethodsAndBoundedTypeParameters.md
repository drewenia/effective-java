# Generic Methods and Bounded Type Parameters

Bounded type parameter'lar, generic algoritmaların implementation'ınında anahtar öneme sahiptir. Aşağıdaki method'u
düşünün; belirtilen `elem`'den büyük olan `T[]` array'inde ki element sayısını sayar.

```java
public static <T> int countGreaterThan(T[] array, T elem) {
    int count = 0;
    for (T e : array) {
        if (e > elem){ // => COMPILER ERROR
            ++count;
        }
    }
    return count;
}
```

Method'un implementasyonu basittir, ancak compile olmaz çünkü greaten than operator (`>`) yalnızca short, int, double,
long, float, byte ve char gibi primitive type'lara uygulanabilir. Object'leri compare etmek için `>` operatörünü
kullanamazsınız. Sorunu çözmek için `Comparable<T>` interface ile bounded bir type parameter kullanın:

```java
public static <T extends Comparable<T>> int countGreaterThan(T[] array, T elem) {
    int count = 0;
    for (T e : array) {
        if (e.compareTo(elem) > 0)
            ++count;
    }
    return count;
}
```