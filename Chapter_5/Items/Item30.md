# Favor generic methods

Tıpkı class'lar generic olabildiği gibi, method'lar da olabilir. Parametrized type'lar üzerinde çalışan static utility
method'lar genellikle generic'tir. Collections içindeki tüm `algorithm` method'ları (örneğin binarySearch ve sort)
generic'tir. Generic method yazmak, generic type yazmaya benzer. İki setin birleşimini `(union)` döndüren bu eksik
method'u düşünün:

```
//Raw type kullanıyor - kabul edilemez!
public static Set union(Set s1, Set s2) {
    Set result = new HashSet(s1);
    result.addAll(s2);
    return result;
}
```

Bu method compile edilir ancak iki warning ile birlikte:

```
Union.java:5: warning: [unchecked] unchecked call to
HashSet(Collection<? extends E>) as a member of raw type HashSet
Set result = new HashSet(s1);
^

Union.java:6: warning: [unchecked] unchecked call to
addAll(Collection<? extends E>) as a member of raw type Set
result.addAll(s2);
^
```

Bu warning'leri düzeltmek ve method'u typesafe yapmak için, declaration'nını üç setin element type'ını represent eden
bir type parameter ile değiştirmeli (iki argüman ve return value) ve bu type parameter'ı method boyunca kullanmalısınız.
Type parameter listesi, type parameter'ları declare eden kısım, method'un modifier'ları ile return type arasında yer
alır. Bu örnekte, type parameter listesi `<E>` ve return type `Set<E>`'dir. Type parameter'lar için isimlendirme
kuralları, generic method'lar ve generic type'lar için aynıdır.

```
static <E> Set<E> union(Set<E> e1, Set<E> e2) {
    Set<E> result = new HashSet<>(e1);
    result.addAll(e2);
    return result;
}
```

En azından basit generic method'lar için, işte bu kadar. Bu method, herhangi bir warning üretmeden compile olur ve hem
type safety hem de kullanım kolaylığı sağlar. İşte method'u test etmek için basit bir program. Bu programda hiç cast
yoktur ve error veya warning olmadan compile edilir:

```
Set<String> guys = Set.of("Ocean", "Joe", "Willy");
Set<String> ladies = Set.of("Marry", "Marilyn", "Daisy");
Set<String> union = union(guys,ladies); 
System.out.println(union); // => [Joe, Ocean, Daisy, Marilyn, Willy, Marry]
```

Output'da ki elementlerin order'i implementasyona bağlıdır. Union method'un bir kısıtlaması, üç setin (hem input
parametrelerin hem de return value'nun) type'larının tamamen aynı olması gerektiğidir. Method'u `bounded wildcard types`
kullanarak daha esnek hale getirebilirsiniz.

Zaman zaman, immutable olan ancak birçok farklı type için geçerli olan bir object oluşturmanız gerekecektir. Generic'ler
silme (erasure) ile implement edildiği için, tüm gerekli type parameterization’lar için tek bir object
kullanabilirsiniz, ancak her istenen type parameterization için object'i tekrar tekrar dağıtacak bir static factory
method yazmanız gerekir. Generic singleton factory olarak adlandırılan bu pattern, `Collections.reverseOrder` gibi
function object’ler için ve bazen `Collections.emptySet` gibi collection’lar için kullanılır.

> Type erasure in java explained `https://www.baeldung.com/java-type-erasure`

Type erasure, type kısıtlamalarını yalnızca compile time’da uygulayıp, element type bilgilerini runtime’da discard etme
süreci olarak açıklanabilir.

```
static <E> boolean containsElement(E[] elements, E element){
    for (E e : elements){
        if (e.equals(element)) return true;
    }
    return false;
}
```

Compiler, `unbound type E`’yi Object’in actual bir type’ı ile replace eder:

```
static boolean containsElement(Object[] elements, Object element){
    for (Object e : elements){
        if(e.equals(element)){
            return true;
        }
    }
    return false;
}
```

Bu nedenle compiler, kodumuzun type safety’sini sağlar ve runtime hatalarını önler. Type erasure, class (veya variable)
ve method seviyelerinde gerçekleşebilir.

### Class Type Erasure

Class seviyesinde, compiler class üzerindeki type parameter’ları discard eder ve onları ilk `bound` ile veya type
parameter `unbound` ise `Object` ile değiştirir.

```
class Stack<E> {
    private E[] stackContent;

    public Stack(int capacity) {
        this.stackContent = (E[]) new Object[capacity];
    }

    public void push(E data){
        // ...
    }

    public E pop(){
        // ...
    }
}
```

Compile sırasında, compiler `unbound type parameter E`’yi Object ile replace eder:

```
public class Stack {
    private Object[] stackContent;

    public Stack(int capacity) {
        this.stackContent = (Object[]) new Object[capacity];
    }

    public void push(Object data) {
        // ..
    }

    public Object pop() {
        // ..
    }
}
```

Type parameter `E`’nin `bound` olduğu bir case de:

```
public class BoundStack<E extends Comparable<E>> {
    private E[] stackContent;

    public BoundStack(int capacity) {
        this.stackContent = (E[]) new Object[capacity];
    }

    public void push(E data) {
        // ..
    }

    public E pop() {
        // ..
    }
}
```

Compiler, bound type parameter `E`’yi ilk bound class ile değiştirir, bu case de `Comparable` ile:

```
public class BoundStack {
    private Comparable [] stackContent;

    public BoundStack(int capacity) {
        this.stackContent = (Comparable[]) new Object[capacity];
    }

    public void push(Comparable data) {
        // ..
    }

    public Comparable pop() {
        // ..
    }
}
```

### Method Type Erasure

Method seviyesindeki type erasure için, method’un type parameter’ı saklanmaz; unbound ise parent type olan Object’e,
bound ise ilk bound class’a dönüştürülür.

Herhangi bir verilen array’in content'ini göstermek için bir method düşünelim:

```
static <E> void printArray(E[] array) {
    for (E element : array){
        System.out.println("%s" + element);
    }
}
```

Compile sırasında, compiler type parameter `E`’yi `Object` ile replace eder:

```
public static void printArray(Object[] array) {
    for (Object element : array) {
        System.out.printf("%s ", element);
    }
}
```

Bound bir method type parameter için:

```
public static <E extends Comparable<E>> void printArray(E[] array) {
    for (E element : array) {
        System.out.printf("%s ", element);
    }
}
```

Type parameter `E` erase edilir ve yerine `Comparable` replace edilir:

```
public static void printArray(Comparable[] array) {
    for (Comparable element : array) {
        System.out.printf("%s ", element);
    }
}
```

### Edge Cases

Type erasure process'i sırasında, compiler benzer method’ları ayırt etmek için zaman zaman `synthetic` bir method
oluşturur. Bunlar, aynı ilk bound class’ı extend eden method signature’larından kaynaklanabilir. Önceki Stack
implementasyonumuzu extend eden yeni bir class oluşturalım.

```
class Stack<E> {
    private E[] stackContent;
    private int size = 0; 

    public Stack(int capacity) {
        this.stackContent = (E[]) new Object[capacity];
    }

    public void push(E data) {
        stackContent[size++] = data;
    }
}

class IntegerStack extends Stack<Integer>{
    public IntegerStack(int capacity){
        super(capacity);
    }

    public void push(Integer value){
        super.push(value);
    }
}    
```

Şimdi aşağıdaki koda bakalım:

```
IntegerStack integerStack = new IntegerStack(5);
Stack stack = integerStack;
stack.push("Hello");
Integer data = integerStack.pop();
```

Type erasure’dan sonra elimizde şunlar olur:

```
IntegerStack integerStack = new IntegerStack(5);
Stack stack = (IntegerStack) integerStack;
stack.push("Hello");
Integer data = (String) integerStack.pop();
```

IntegerStack’in parent class olan Stack’ten `push(Object)` method’unu inherit ettiği için, `IntegerStack`’e `String`
push edebildiğimize dikkat edin. Bu elbette yanlıştır — çünkü `integerStack`, `Stack<Integer>` type'ı olduğundan içine
sadece `integer` eklenmelidir. Bu yüzden şaşırtıcı olmayan bir şekilde, bir String’i `pop` edip `Integer`’a atamaya
çalışmak, compiler tarafından push sırasında eklenen cast yüzünden `ClassCastException`’a neden olur.

### Bridge Methods

Yukarıdaki edge case'i çözmek için, compiler bazen `bridge method` oluşturur. Bu, Java compiler tarafından,
parameterized bir class’ı extend eden veya parameterized bir interface’i implement eden bir class ya da interface
compile edilirken oluşturulan `synthetic` bir method’dur; method signature’ları biraz farklı veya belirsiz olabilir.

Yukarıdaki örneğimizde, Java compiler, IntegerStack’in `push(Integer)` method’u ile Stack’in `push(Object)` method’u
arasında method signature uyumsuzluğu olmamasını sağlayarak erasure sonrasında generic type'ların polymorphism’ini
korur.

Bu nedenle, compiler burada bir bridge method oluşturur:

```
public class IntegerStack extends Stack {
    // Bridge method generated by the compiler
    
    public void push(Object value) {
        push((Integer)value);
    }

    public void push(Integer value) {
        super.push(value);
    }
}
```

Sonuç olarak, type erasure sonrası Stack class’ının push method’u, IntegerStack class’ının orijinal push method’una
delegate eder.

> End of documentation

Identity function dispenser `(dağıtıcı)` yazmak istediğinizi varsayalım. Library'ler `Function.identity` sağlar, bu
yüzden kendi fonksiyonunuzu yazmaya gerek yoktur, ancak öğreticidir. İstendiğinde her seferinde yeni bir identity
function object oluşturmak israf olur, çünkü bu stateless’tir. Java’nın generics’i reified olsaydı, her type için bir
identity function gerekirdi, ancak erased oldukları için generic singleton yeterlidir. İşte nasıl göründüğü:

```
private static UnaryOperator<Object> IDENTITY_FN = (t) -> t;

@SuppressWarnings("unchecked")
public static <T> UnaryOperator<T> identityFunction(){
    return (UnaryOperator<T>) IDENTITY_FN;
}
```

`IDENTITY_FN`’in `(UnaryFunction<T>)`’ye cast edilmesi unchecked cast uyarısı üretir, çünkü `UnaryOperator<Object>` her
`T` için `UnaryOperator<T>` değildir. Ancak identity function özeldir: Argümanını değiştirmeden döndürür, bu yüzden
`T`’nin değeri ne olursa olsun `UnaryFunction<T>` olarak kullanmanın `typesafe` olduğunu biliriz. Bu nedenle, bu cast
tarafından oluşturulan `unchecked cast` uyarısını güvenle suppress edebilirsiniz. Bunu yaptıktan sonra, kod hata veya
uyarı olmadan compile edilir.

Generic singleton’ımızı `UnaryOperator<String>` ve `UnaryOperator<Number>` olarak kullanan örnek bir program şudur:
Her zamanki gibi, hiçbir cast içermez ve hata ya da uyarı olmadan compile edilir:

```
String[] strings = {"jute", "hemp", "nylon"};
UnaryOperator<String> sameString = identityFunction();
for (String s : strings) {
    System.out.println(sameString.apply(s)); // => jute, hemp, nylon
}

Number[] numbers = {1, 2.0, 3L, 4D};
UnaryOperator<Number> sameNumber = identityFunction();
for (Number n : numbers){
    System.out.println(sameNumber.apply(n)); // => 1, 2.0, 3, 4.0
}
```

Bir type parameter’ın, kendisini içeren bir expression'la sınırlandırılması `(bounded)` mümkündür, ancak nispeten
nadirdir. Bu, `recursive type bound` olarak bilinir. Recursive type bound’ların yaygın bir kullanım alanı, bir type’ın
natural sıralamasını `(ordering)` tanımlayan `Comparable` interface’iyle bağlantılıdır. Bu interface şu şekilde
gösterilir:

```
public interface Comparable<T> {
    int compareTo(T o);
}
```

Type parameter `T`, `Comparable<T>`’i implement eden type’ın element’lerinin compare edilebileceği type’ı tanımlar.
Pratikte, neredeyse tüm type’lar yalnızca kendi type’larından element’lerle compare edilebilir. Bu yüzden, örneğin,
String `Comparable<String>`’i implement eder, Integer ise `Comparable<Integer>`’i ve benzeri şekilde.

Birçok method, sort, içinde search yapmak, minimum veya maksimumunu hesaplamak gibi işlemler için Comparable implement
eden element’lerden oluşan bir collection alır. Bu işlemleri gerçekleştirebilmek için, collection’daki her element’in
diğer tüm element’lerle comparable olması gerekir; başka bir deyişle, listedeki element’lerin mutually olarak comparable
olması gerekir. Bu kısıtlama şu şekilde ifade edilir:

```
// Mutual comparability ifade etmek için recursive type bound kullanmak
public static <E extends Comparable<E>> E max(Collection<E> c);
```

`<E extends Comparable<E>>` type bound’u “kendisiyle karşılaştırılabilen `(compared)` herhangi bir E tipi,” olarak
okunabilir; bu da mutual comparability kavramına büyük ölçüde karşılık gelir.

İşte önceki declaration'a uygun bir method. Elementlerin natural order'ına göre bir collection’daki maksimum değeri
hesaplar ve hata ya da uyarı olmadan compile edilir:

```
public static void main(String[] args) {
    List<Integer> integerList = List.of(1,11,22,4,9,23);
    System.out.println(max(integerList)); // => 23
}

// Bir collection’daki maksimum değeri döner — recursive type bound kullanır
public static <E extends Comparable<E>> E max(Collection<E> collection) {
    if (collection.isEmpty()) throw new IllegalArgumentException("Empty collection");

    E result = null;
    for (E e : collection) {
        if (result == null || e.compareTo(result) > 0) {
            result = Objects.requireNonNull(e);
        }
    }
    return result;
}
```

Bu method’un, liste boşsa `IllegalArgumentException` fırlattığını unutmayın. Daha iyi bir alternatif, `Optional<E>`
döndürmek olurdu. Recursive type bound’lar çok daha complex hale gelebilir, ancak neyse ki nadiren böyle olur. Bu
idiomu, onun wildcard varyantınıve simulated self-type idiom’unu (Item 2) anlarsanız, pratikte karşılaşacağınız çoğu
recursive type bound ile başa çıkabilirsiniz.

Özetle, generic method’lar, generic type’lar gibi, input parametreler ve return value’lara explicit cast koymayı
gerektiren method’lardan daha güvenli ve kullanımı kolaydır. Type’lar gibi, method’larınızın cast olmadan
kullanılabileceğinden emin olmalısınız; bu genellikle onları generic yapmak anlamına gelir. Ve type’lar gibi, kullanımı
cast gerektiren mevcut method’ları da generic hale getirmelisiniz. Bu, mevcut client’ları bozmadan yeni kullanıcılar
için hayatı kolaylaştırır.