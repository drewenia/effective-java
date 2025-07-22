# Answer to Questions and Exercises: Generics

1 - Bir Collection'da ki belirli bir property’ye sahip element'lerin sayısını hesaplayan generic bir method yazınız.
(Örneğin, tek integer'lar, prime sayılar, palindrome'lar.)

* Answer

```java

@FunctionalInterface
interface UnaryPredicate<T> {
    boolean test(T object);
}

class OddPredicate implements UnaryPredicate<Integer> {

    @Override
    public boolean test(Integer i) {
        return i % 2 != 0;
    }
}

class EvenPredicate implements UnaryPredicate<Integer> {
    @Override
    public boolean test(Integer i) {
        return i % 2 == 0;
    }
}

class Palindrome implements UnaryPredicate<Integer> {
    @Override
    public boolean test(Integer i) {
        int reverse = 0;
        int temp = Math.abs(i);
        while (temp != 0) {
            reverse = (reverse * 10) + (temp % 10);
            temp = temp / 10;
        }
        return (reverse == Math.abs(i));
    }
}
```

Algorithm.java;

```java
class Algorithm {
    public static <T> int countIf(Collection<T> coll, UnaryPredicate<T> predicate) {
        int count = 0;
        for (T elem : coll) {
            if (predicate.test(elem))
                count++;
        }
        return count;
    }
}
```

Derived.java;

```java
public static void main(String[] args) {
    Collection<Integer> coll = Arrays.asList(1, 2, 3, 4, 5, 6, 7);
    int count = Algorithm.countIf(coll, new OddPredicate());
    System.out.println("Number of odd integers = " + count); // => Number of odd integers = 4
}
```

2 - Aşağıdaki class compile eder mi? Eğer etmezse, neden?

```java
public final class Algorithm {
    public static <T> T max(T x, T y) {
        return x > y ? x : y;
    }
}
```

* Answer

Hayır. Greater than `(>)` operator'ı yalnızca primitive numeric type'lara uygulanabilir.

3 - Bir array'deki iki farklı element'in konumlarını değiştiren generic bir method yazınız.

* Answer

```java
final class Algorithm {
    public static <T> void swap(T[] array, int i, int j) {
        T temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }
}
```

4 - Eğer compiler tüm type parameter'ları compile zamanında siliyorsa, neden generic'leri kullanmalısınız?

* Answer

Java compiler, generic kod üzerinde compile time'da daha sıkı type kontrolü uygular.

Generic'ler, programming type'larını parameter olarak kullanmayı destekler.

Generic'ler, generic algorithm'ların implement edilmesini sağlar.

5 - Aşağıdaki class, type erasure sonrası neye dönüştürülür?

```java
public class Pair<K, V> {
    private K key;
    private V value;

    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey();

    {
        return key;
    }

    public V getValue();

    {
        return value;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public void setValue(V value) {
        this.value = value;
    }
}
```

* Answer

```java
public class Pair {
    private Object key;
    private Object value;

    public Pair(Object key, Object value) {
        this.key = key;
        this.value = value;
    }

    public Object getKey() {
        return key;
    }

    public Object getValue() {
        return value;
    }

    public void setKey(Object key) {
        this.key = key;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
```

6 - Aşağıdaki method, type erasure sonrası neye dönüştürülür?

```java
public static <T extends Comparable<T>>
int findFirstGreaterThan(T[] at, T elem) {
    // ...
}
```

* Answer

```java
public static int findFirstGreaterThan(Comparable[] at, Comparable elem) {
    // ...
}
```

7 - Aşağıdaki method compile eder mi? Eğer etmezse, neden?

```java
public static void print(List<? extends Number> list) {
    for (Number n : list)
        System.out.print(n + " ");
    System.out.println();
}
```

* Answer

Evet compile edilir

8 - Bir list'in `[begin, end]` aralığındaki maximal element'i bulan generic bir method yazınız.

* Answer

```java
final class Algorithm {
    public static <T extends Object & Comparable<? super T>> T max(List<? extends T> list, int begin, int end) {
        T maxElem = list.get(begin);
        for (++begin; begin < end; ++begin)
            if (maxElem.compareTo(list.get(begin)) < 0)
                maxElem = list.get(begin);
        return maxElem;
    }
}
```

9 - Aşağıdaki class compile eder mi? Eğer etmezse, neden?

```java
public class Singleton<T> {

    public static T getInstance() {
        if (instance == null)
            instance = new Singleton<T>();

        return instance;
    }

    private static T instance = null;
}
```

* Answer

Hayır. Type parameter `T` türünde static field oluşturamazsınız.

10 - Aşağıdaki class'lar verildiğinde:

```java
class Shape { /* ... */
}

class Circle extends Shape { /* ... */
}

class Rectangle extends Shape { /* ... */
}

class Node<T> { /* ... */
}
```

Aşağıdaki kod compile eder mi? Eğer etmezse, neden?

```java
Node<Circle> nc = new Node<>();
Node<Shape> ns = nc;
```

* Answer

Hayır. Çünkü `Node<Circle>`, `Node<Shape>`'in subtype'ı değildir.

11 - Bu class'ı dikkate alınız:

```java
class Node<T> implements Comparable<T> {
    public int compareTo(T obj) { /* ... */ }
    // ...
}
```

Aşağıdaki kod compile eder mi? Eğer etmezse, neden?

```java
Node<String> node = new Node<>();
Comparable<String> comp = node;
```

* Answer

Evet

12 - Aşağıdaki method'u, belirtilen integer'lar listesindeki her birine göre relatively prime olan ilk integer'ı bulmak
için nasıl invocation yaparsınız?

```java
public static <T> int findFirst(List<T> list, int begin, int end, UnaryPredicate<T> p) {
}
```

İki integer `a` ve `b`'nin relatively prime olması, `gcd(a, b) = 1` olması anlamına gelir; burada `gcd`, greatest common
divisor'ın kısaltmasıdır.

* Answer

```java
public final class Algorithm {

    public static <T>
        int findFirst(List<T> list, int begin, int end, UnaryPredicate<T> p) {

        for (; begin < end; ++begin)
            if (p.test(list.get(begin)))
                return begin;
        return -1;
    }

    // x > 0 and y > 0
    public static int gcd(int x, int y) {
        for (int r; (r = x % y) != 0; x = y, y = r) { }
            return y;
    }
}
```

Generic UnaryPredicate interface'i aşağıdaki gibi tanımlanmıştır:

```java
public interface UnaryPredicate<T> {
    boolean test(T obj);
}
```

Aşağıdaki program, findFirst method'unu test eder:

```java
class RelativelyPrimePredicate implements UnaryPredicate<Integer> {
    public RelativelyPrimePredicate(Collection<Integer> c) {
        this.c = c;
    }

    public boolean test(Integer x) {
        for (Integer i : c)
            if (Algorithm.gcd(x, i) != 1)
                return false;

        return c.size() > 0;
    }

    private Collection<Integer> c;
}

public class Test {
    public static void main(String[] args) throws Exception {

        List<Integer> li = Arrays.asList(3, 4, 6, 8, 11, 15, 28, 32);
        Collection<Integer> c = Arrays.asList(7, 18, 19, 25);
        UnaryPredicate<Integer> p = new RelativelyPrimePredicate(c);

        int i = ALgorithm.findFirst(li, 0, li.size(), p);

        if (i != -1) {
            System.out.print(li.get(i) + " is relatively prime to ");
            for (Integer k : c)
                System.out.print(k + " ");
            System.out.println();
        }
    }
}
```

Output;

```
11 is relatively prime to 7 18 19 25
```