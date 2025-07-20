# Generic Methods

Generic method'lar, kendi type parameter'larını tanımlayan method'lardır. Bu, generic type declaration'ınına benzer,
ancak type parameter'ın scope'u sadece tanımlandığı method ile sınırlıdır. Static ve non-static generic method'lara izin
verilir, aynı zamanda generic class constructor'larına da.

Generic method syntax'ı, açılı parantezler içinde yer alan type parameter listesini içerir ve bu liste method'un return
type'ından önce gelir. Static generic method'larda, type parameter bölümü method'un return type'ından önce yer
almalıdır.

`Util` class'ı, iki `Pair` object'ini karşılaştıran generic bir method olan compare'ı içerir:

```java
class Pair<K, V> {
    private K key;
    private V value;

    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }
}

class Util {
    public static <K, V> boolean compare(Pair<K, V> p1, Pair<K, V> p2) {
        return p1.getKey().equals(p2.getKey()) && p1.getValue().equals(p2.getValue());
    }
}
```

Bu method'u invoke etmek için tam syntax şu şekildedir:

```
Pair<Integer, String> p1 = new Pair<>(1, "apple");
Pair<Integer, String> p2 = new Pair<>(2, "pear");
boolean same = Util.<Integer,String>compare(p1,p2);
System.out.println(same); // => false
```

Type explicitly verilmiştir. Genellikle bu belirtilmeyebilir ve compiler gereken type'ı infer eder:

```
Pair<Integer, String> p1 = new Pair<>(1, "apple");
Pair<Integer, String> p2 = new Pair<>(2, "pear");
boolean same = Util.compare(p1, p2);
```

Type inference olarak bilinen bu özellik, generic method'u açılı parantezler arasında type belirtmeden ordinary bir
method gibi invoke etmenize olanak tanır.