# Restrictions on Generics

## Primitive Types ile Generic Types’in Instantiation’ı Yapılamaz

Aşağıdaki parameterized type’ı dikkate alın:

```java
class Pair<K, V> {
    private K key;
    private V value;

    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }
    // ...
}
```

Bir Pair object oluştururken, type parameter `K` veya `V` yerine primitive type kullanamazsınız:

```java
Pair<int, char> p = new Pair<>(8, 'a');  // compile-time error
```

Type parameter `K` ve `V` için sadece `non-primitive` type’lar kullanılabilir:

```java
Pair<Integer, Character> p = new Pair<>(8, 'a');
```

Java compiler’ın 8 değerini `Integer.valueOf(8)` ve `a` karakterini `Character('a')` olarak `autobox` ettiğini
unutmayın:

## Type Parameter’ların Instance’ları Oluşturulamaz

Bir type parameter’ın instance’ı oluşturulamaz. Örneğin, aşağıdaki kod compile-time error’a neden olur:

```java
public static <E> void append(List<E> list) {
    E elem = new E(); // => COMPILE-TIME ERROR
    list.add(elem);
}
```

Bir geçici çözüm olarak, bir type parameter’ın object’ini reflection yoluyla oluşturabilirsiniz:

```java
public static <E> void append(List<E> list, Class<E> cls) throws Exception {
    E elem = cls.newInstance();   // OK - DEPRECATED
    list.add(elem);
}
```

append method’unu aşağıdaki şekilde invoke edebilirsiniz:

```
List<String> ls = new ArrayList<>();
append(ls, String.class);
```

## Type’ı Type Parameter olan Static Field’lar Declare Edilemez

Bir sınıfın static field’ı, sınıfın tüm non-static object’leri tarafından paylaşılan class-level bir variable’dır. Bu
nedenle, type parameter türünde static field’lara izin verilmez. Aşağıdaki sınıfı dikkate alın:

```java
public class MobileDevice<T> {
    private static T os;

    // ...
}
```

Eğer type parameter türünde static field’lara izin verilseydi, aşağıdaki kod karışıklığa neden olurdu:

```
MobileDevice<Smartphone> phone = new MobileDevice<>();
MobileDevice<Pager> pager = new MobileDevice<>();
MobileDevice<TabletPC> pc = new MobileDevice<>();
```

Çünkü static field `os`, phone, pager ve pc tarafından paylaşılır; `os`’nin actual type’ı nedir? Aynı anda hem
Smartphone, hem Pager, hem de TabletPC olamaz. Bu nedenle, type parameter türünde static field oluşturamazsınız.

## Parameterized Type’larla Cast veya instanceof Kullanılamaz

Java compiler, generic kodda tüm type parameter’ları erase ettiği için, runtime’da hangi parameterized type’ın
kullanıldığını doğrulamak mümkün değildir:

```
public static <E> void rtti(List<E> list) {
    if (list instanceof ArrayList<Integer>) {  // => COMPILE-TIME ERROR
        // ...
    }
}
```

rtti method’una geçirilen parameterized type seti şudur:

```
S = { ArrayList<Integer>, ArrayList<String> LinkedList<Character>, ... }
```

Runtime, type parameter’ları takip etmez, bu yüzden `ArrayList<Integer>` ile `ArrayList<String>` arasındaki farkı ayırt
edemez. Yapabileceğiniz en fazla şey, liste’nin bir ArrayList olduğunu doğrulamak için unbounded wildcard kullanmaktır:

```
public static void rtti(List<?> list) {
    if (list instanceof ArrayList<?>) {  // => OK; instanceof requires a reifiable type
        // ...
    }
}
```

Genellikle, parameterized type’a cast yapamazsınız, ta ki unbounded wildcard ile parameterize edilmiş olana kadar.
Örneğin:

```
List<Integer> li = new ArrayList<>();
List<Number>  ln = (List<Number>) li;  // => COMPILE-TIME ERROR
```

Ancak, bazı durumlarda compiler, bir type parameter’ın her zaman geçerli olduğunu bilir ve cast işlemine izin verir.
Örneğin:

```
List<String> l1 = ...;
ArrayList<String> l2 = (ArrayList<String>)l1;  // => OK
```

## Parameterized Type’lardan Array Oluşturulamaz

Parameterized type’lardan array oluşturamazsınız. Örneğin, aşağıdaki kod compile olmaz:

```
List<Integer>[] arrayOfLists = new List<Integer>[2];  // => COMPILE-TIME ERROR
```

Aşağıdaki kod, farklı türlerin bir array’e eklendiğinde neler olduğunu gösterir:

```
Object[] strings = new String[2];
strings[0] = "hi";   // => OK
strings[1] = 100;    // => An ArrayStoreException is thrown.
```

Aynı şeyi generic bir liste ile yapmaya çalışırsanız, bir sorun olur:

```
Object[] stringLists = new List<String>[2]; // compiler error, ancak izin verilmiş gibi varsayalım
stringLists[0] = new ArrayList<String>();   // OK
stringLists[1] = new ArrayList<Integer>(); // ArrayStoreException fırlatmalı, ancak runtime bunu tespit edemez
```

Eğer parameterized list’lerden array oluşturulmasına izin verilseydi, önceki kod istenen ArrayStoreException’ı
fırlatmakta başarısız olurdu.

## Parameterized Type’ların Object’leri Oluşturulamaz, Catch Edilemez veya Throw Edilemez

Bir generic sınıf directly veya indirectly Throwable sınıfını extend edemez. Örneğin, aşağıdaki sınıflar compile olmaz:

```java
// Extends Throwable indirectly
class MathException<T> extends Exception { /* ... */ }    // compile-time error

// Extends Throwable directly
class QueueFullException<T> extends Throwable { /* ... */} // compile-time error
```

Bir method, bir type parameter’ın instance’ını catch edemez:

```j
public static <T extends Exception, J> void execute(List<J> jobs) {
    try {
        for (J job : jobs)
            // ...
    } catch (T e) {   // compile-time error
        // ...
    }
}
```

Ancak, throws clause’da bir type parameter kullanabilirsiniz:

```java
class Parser<T extends Exception> {
    public void parse(File file) throws T {     // OK
        // ...
    }
}
```

## Formal Parameter Type’ları Erased Olduğunda Aynı Raw Type’a Dönüşen Overload’lar Declare Edilemez

Bir sınıf, type erasure’dan sonra aynı signature’a sahip olacak iki overloaded method’a sahip olamaz.

```java
public class Example {
    public void print(Set<String> strSet) { }
    public void print(Set<Integer> intSet) { }
}
```

Overload’ların tümü aynı classfile representation'ınını paylaşır ve compile-time error üretir.