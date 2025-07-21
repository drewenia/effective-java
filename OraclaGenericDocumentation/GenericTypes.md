# Generic Types

Generic type, type'lar üzerinde parameterized olan generic bir class veya interface'dir. Aşağıdaki Box class'ı, kavramı
göstermek için değiştirilecektir.

## A Simple Box Class

Herhangi bir type'daki object'ler üzerinde çalışan generic olmayan bir Box class'ını inceleyerek başlayalım. Sadece iki
method sağlaması gerekir: object'i `box`'a ekleyen `set` ve onu geri alan `get`.

```java
class Box {
    private Object object;

    public Object get() {
        return object;
    }

    public void set(Object object) {
        this.object = object;
    }
}
```

Method'ları Object kabul ettiğinden veya döndürdüğünden, primitive type'lardan biri olmadığı sürece istediğinizi
geçebilirsiniz. Sınıfın nasıl kullanıldığı compile time'da verify edilemez. Kodun bir bölümü `box`'a Integer koyup
Integer beklerken, başka bir bölümü yanlışlıkla String geçirebilir ve bu da runtime hatasına yol açar.

## A Generic Version of the Box Class

Generic bir class aşağıdaki formatla tanımlanır:

```
class name<T1, T2, ..., Tn> { /* ... */ }
```

Type parameter bölümü, açılı parantezler `<>` ile sınırlanmış olarak, class adını takip eder. Bu bölüm,
`T1, T2, ..., Tn` olarak adlandırılan type parameter'ları (type variable'lar) belirtir. Box class'ını generics
kullanacak şekilde güncellemek için, `public class Box` kodunu `public class Box<T>` olarak değiştirerek generic type
declaration'ı oluşturursunuz. Bu, class içinde her yerde kullanılabilecek `T` type variable'ını tanımlar.

Bu değişiklikle, Box class şu hale gelir:

```java
/**
 * Generic version of the Box class.
 *
 * @param <T> the type of the value being boxed
 */
class Box<T> {
    // T stands for "Type"
    private T t;

    public T get() {
        return t;
    }

    public void set(T t) {
        this.t = t;
    }
}
```

Görüldüğü gibi, Object'in tüm kullanımları `T` ile değiştirilmiştir. Bir type variable, belirttiğiniz herhangi bir
`non-primitive` type olabilir: Herhangi bir class type, herhangi bir interface type, herhangi bir array type veya hatta
başka bir type variable. Aynı teknik generic interface'ler oluşturmak için de uygulanabilir.

## Type Parameter Naming Conventions

Konvansiyon olarak, type parameter isimleri tek ve büyük harf olur. Bu, zaten bildiğiniz değişken isimlendirme
konvansiyonlarından keskin bir şekilde farklıdır ve bunun iyi bir sebebi vardır: Bu konvansiyon olmazsa, bir type
variable ile sıradan bir class veya interface adı arasındaki farkı anlamak zor olurdu.

En yaygın kullanılan type parameter isimleri şunlardır:

```
E - Element (Java Collections Framework tarafından yaygın olarak kullanılır)
K - Key
N - Number
T - Type
V - Value
S,U,V etc. - 2nd, 3rd, 4th types
```

## Invoking and Instantiating a Generic Type

Generic Box class'ına kodunuzun içinde referans vermek için, `T`'nin `Integer` gibi concrete bir value ile
değiştirildiği `generic type invocation` yapmanız gerekir:

```
Box<Integer> integerBox;
```

Generic type invocation'ı, sıradan bir method invocation'a benzetebilirsiniz; ancak burada metoda argüman geçmek yerine,
Box class'ına type argüman — bu durumda Integer — geçirirsiniz.

* Type Parameter and Type Argument Terminology - Birçok geliştirici "type parameter" ve "type argument" terimlerini
  birbirinin yerine kullanır, ancak bu terimler aynı değildir. Kod yazarken, parameterized type oluşturmak için type
  argument'lar sağlanır. Bu nedenle, `Foo<T>` içindeki `T` bir type parameter iken, `Foo<String> f` içindeki String bir
  type argument'tir. Bu ders, bu terimleri kullanırken bu tanımı esas alır.

Diğer değişken declaration'ları gibi, bu kod aslında yeni bir Box object'i oluşturmaz. Bu sadece integerBox'ın "Box of
Integer" referansını tutacağını belirtir; `Box<Integer>` böyle okunur. Generic bir type'ın invocation'ı genellikle
parameterized type olarak adlandırılır.

Bu sınıfı instantiate etmek için, her zamanki gibi `new` keyword'ünü kullanın, ancak class adı ile parantez arasına
`<Integer>` koyun:

```
Box<Integer> integerBox = new Box<Integer>();
```

## The Diamond

Java SE 7 ve sonrasında, compiler context'den type argument'ları belirleyebildiği `(determine)` veya infer edebildiği
sürece, generic class'ın constructor'ını çağırmak için gereken type argument'ları empty set (`<>`) ile
değiştirebilirsiniz. Bu açılı parantez pair'ı `<>`, gayri resmi olarak `diamond (elmas)` olarak adlandırılır. Örneğin,
aşağıdaki statement ile `Box<Integer>` instance'ı oluşturabilirsiniz:

```
Box<Integer> integerBox = new Box<>();
```

## Multiple Type Parameters

Daha önce belirtildiği gibi, generic bir class birden fazla type parameter'a sahip olabilir. Örneğin, generic `Pair`
interface'ini implemente eden generic `OrderedPair` class:

```java
interface Pair<K, V> {
    K getKey();

    V getValue();
}

class OrderedPair<K, V> implements Pair<K, V> {
    private K key;
    private V value;

    public OrderedPair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public K getKey() {
        return key;
    }

    @Override
    public V getValue() {
        return value;
    }
}
```

Aşağıdaki statement'lar, `OrderedPair` class'ının iki instance'ını oluşturur:

```java
public static void main(String[] args) {
    Pair<String, Integer> p1 = new OrderedPair<String, Integer>("Even", 8);
    System.out.println(p1.getKey()); // => Even
    System.out.println(p1.getValue()); // => 8

    Pair<String, String> p2 = new OrderedPair<String, String>("Hello", "World");
    System.out.println(p2.getKey()); // => Hello
    System.out.println(p2.getValue()); // => World

}
```

`new OrderedPair<String, Integer>` kodu, `K`'yı String ve `V`'yi Integer olarak instantiate eder. Bu nedenle,
`OrderedPair` constructor'ının parameter type'ları sırasıyla `String` ve `Integer`'dır. Autoboxing sayesinde, sınıfa bir
String ve bir int geçirmek geçerlidir.

The Diamond bölümünde belirtildiği gibi, Java compiler `OrderedPair<String, Integer>` declaration'ının da `K` ve `V`
type'larını infer edebildiği için, bu statement'lar diamond notasyonu kullanılarak kısaltılabilir:

```
OrderedPair<String, Integer> p1 = new OrderedPair<>("Even", 8);
OrderedPair<String, String>  p2 = new OrderedPair<>("hello", "world");
```

Generic bir interface oluşturmak için, generic bir class oluştururken kullanılan konvansiyonların aynısını takip edin.

## Parameterized Types

Bir type parameter'ı (yani `K` veya `V`), bir parameterized type (yani `List<String>`) ile de değiştirebilirsiniz.
Örneğin, `OrderedPair<K, V>` örneğini kullanarak:

```
OrderedPair<String, Box<Integer>> p = new OrderedPair<>("primes", new Box<Integer>(...));
```