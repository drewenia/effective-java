# Use bounded wildcards to increase API flexibility

Item 28’de belirtildiği gibi, parameterized type’lar değişmezdir `(invariant)`. Başka bir deyişle, iki farklı type olan
`Type1` ve `Type2` için, `List<Type1>` ne `List<Type2>`’nin subtype’ı ne de supertype’ıdır. `List<String>`’in
`List<Object>`’in subtype’ı olmaması sezgisel olmasa da gerçekten mantıklıdır. Bir `List<Object>`’e herhangi bir object
koyabilirsiniz, ancak bir `List<String>`’e sadece string koyabilirsiniz. Bir `List<String>`, bir `List<Object>`’in
yapabildiği her şeyi yapamadığı için, subtype değildir (Liskov substitution principle).

Bazen, invariant typing’in sağlayabileceğinden daha fazla esnekliğe ihtiyaç duyarsınız. Item 29’daki Stack class’ını
düşünün. Hatırlamanız için, işte public API’si:

```
public class Stack<E> {
    public Stack();
    public void push(E e);
    public E pop();
    public boolean isEmpty();
}
```

Bir sequence of elements alan ve bunların hepsini stack üzerine push eden bir method eklemek istediğimizi varsayalım.
İşte ilk deneme:

```
// pushAll methodu wildcard type olmadan - yetersiz!
public void pushAll(Iterable<E> src) {
    for (E e : src)
        push(e);
}
```

Bu method sorunsuz compile olur, ancak tamamen tatmin edici değildir. Eğer Iterable src'nin element type'ı stack'in
element type'ı ile tam olarak eşleşiyorsa `(matches)`, sorunsuz çalışır. Ama diyelim ki bir `Stack<Number>` var ve
`push(intVal)` invoke yapıyorsun, burada intVal `Integer` type'ındadır. Bu çalışır çünkü Integer, Number'ın bir
subtype'ıdır. Yani mantıksal olarak, bunun da çalışması gerekir:

```
Stack<Number> numberStack = new Stack<>();
Iterable<Integer> integers = ... ;
numberStack.pushAll(integers);
```

Ancak denersen, parameterized types invariant olduğu için bu hata mesajını alırsın:

```
StackTest.java:7: error: incompatible types: Iterable<Integer>
cannot be converted to Iterable<Number>
numberStack.pushAll(integers);
^
```

Neyse ki, bir çıkış yolu var. Dil, bu gibi durumlarla başa çıkmak için `bounded wildcard type` adı verilen özel bir
parameterized type türü sağlar. `pushAll` methodunun input parameter'ının type'ı `Iterable of E` değil,
`Iterable of some subtype of E` olmalıdır tam olarak bunu ifade eden bir wildcard type vardır: `Iterable<? extends E>`
(extends keyword'ünün kullanımı biraz yanıltıcıdır: Item 29'dan hatırlayalım, subtype öyle define edilir ki her type,
kendisinin de subtype'ıdır, kendisini extend etmese bile.) Şimdi `pushAll` methodunu bu type'ı kullanacak şekilde
değiştirelim:

Stack.java;

```
// E producer olarak görev yapan bir parameter için wildcard type
public void pushAll(Iterable<? extends E> src) {
    for (E e : src) {
        push(e);
    }
}
```

Bu değişiklikle birlikte, yalnızca Stack sorunsuz şekilde compile olmaz, aynı zamanda orijinal pushAll declaration'ı ile
compile olmayan client code da compile olur. Çünkü Stack ve client'ı sorunsuz şekilde compile olur, her şeyin typesafe
olduğunu bilirsin.

Şimdi, pushAll ile birlikte kullanılacak bir popAll methodu yazmak istediğini varsayalım. popAll methodu, stack'ten her
bir element'i pop eder ve bu element'leri verilen collection'a ekler. popAll methodunu yazmaya yönelik ilk deneme şöyle
görünebilir:

```
// Wildcard type olmayan popAll methodu - yetersiz!
public void popAll(Collection<E> dst) {
    while (!isEmpty())
        dst.add(pop());
}
```

Yine, bu sorunsuz şekilde compile olur ve target collection'ın element type'ı stack'in element type'ı ile tam olarak
eşleşiyorsa `(matches)` düzgün çalışır. Ama yine, tamamen tatmin edici değildir. Diyelim ki bir `Stack<Number>` ve
Object type'ında bir değişkenin var. Eğer stack'ten bir element pop edip değişkene atarsan, bu compile olur ve hata
olmadan çalışır. O zaman bunu da yapabilmen gerekmez mi?

```
Stack<Number> numberStack = new Stack<Number>();
Collection<Object> objects = ... ;
numberStack.popAll(objects);
```

Eğer önceki popAll versiyonuna karşı bu client kodunu compile etmeye çalışırsan, pushAll'ın ilk versiyonunda aldığımıza
çok benzer bir hata alırsın: `Collection<Object>`, `Collection<Number>`'ın subtype'ı değildir. Yine, wildcard types bir
çıkış yolu sağlar. popAll methodunun input parameter'ının type'ı `collection of E` değil,
`collection of some supertype of E` olmalıdır (burada supertype, `E`'nin kendisinin de supertype'ı olduğu şekilde
tanımlanır `[JLS, 4.10]`). Yine, tam olarak bunu ifade eden bir wildcard type vardır: `Collection<? super E>`. Şimdi
popAll methodunu bunu kullanacak şekilde değiştirelim:

```
// E consumer olarak görev yapan parameter için wildcard type
public void popAll(Collection<? super E> dst) {
    while(!isEmpty()){
        dst.add(pop());
    }
}
```

Bu değişiklikle birlikte, hem Stack hem de client kodu sorunsuz şekilde compile olur.

```
// Wildcard types kullanan bulk method'lara sahip generic stack
public class Stack<E> {
    private E[] elements;
    private int size = 0;
    private static final int DEFAULT_INITIAL_CAPACITY = 16;

    // elements array'i yalnızca push(E) ile gelen E instance'larını içerecektir.
    /* Bu, type safety'yi sağlamak için yeterlidir, ancak array'in runtime type'ı E[] olmayacaktır; her zaman Object[]
       olacaktır!
    */
    @SuppressWarnings("unchecked")
    public Stack() {
        elements = (E[]) new Object[DEFAULT_INITIAL_CAPACITY];
    }

    public void push(E e) {
        ensureCapacity();
        elements[size++] = e;
    }

    public E pop() {
        if (size == 0) throw new EmptyStackException();

        E result = elements[--size];
        elements[size] = null; // Eski referansı eliminate et
        return result;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    private void ensureCapacity() {
        if (elements.length == size)
            elements = Arrays.copyOf(elements, 2 * size + 1);
    }

    // E producer olarak görev yapan bir parameter için wildcard type
    public void pushAll(Iterable<? extends E> src) {
        for (E e : src) {
            push(e);
        }
    }


    // E consumer olarak görev yapan parameter için wildcard type
    public void popAll(Collection<? super E> dst) {
        while (!isEmpty()) {
            dst.add(pop());
        }
    }

    public static void main(String[] args) {
        Stack<Number> numberStack = new Stack<>();
        Iterable<Integer> integers = Arrays.asList(3, 1, 4, 1, 5, 9);
        numberStack.pushAll(integers);

        Collection<Object> objects = new ArrayList<>();
        numberStack.popAll(objects);
        System.out.println(objects); // => [9, 5, 1, 4, 1, 3]
    }
}
```

Ders açıktır. Maksimum esneklik için, `producer` veya `consumer` olan input parameter'larda wildcard types kullan.
Eğer bir input parameter hem `producer` hem de `consumer` ise, wildcard types sana fayda sağlamaz: tam bir type
eşleşmesine `(match)` ihtiyacın vardır ve bunu wildcard olmadan elde edersin.

Hangi wildcard type'ını kullanacağını hatırlamana yardımcı olacak bir mnemonic:

`PECS stands for producer-extends, consumer-super.`

Başka bir deyişle, eğer bir parameterized type bir `T` producer'ı represent ediyorsa, `<? extends T>` kullan; eğer bir
`T` consumer'ı represent ediyorsa, `<? super T>` kullan. Stack örneğimizde, `pushAll`’ın `src` parameter’ı Stack
tarafından kullanılmak üzere `E` instance’ları produce eder, bu yüzden `src` için uygun type
`Iterable<? extends E>`’dir; `popAll`’ın `dst` parameter’ı Stack’ten `E` instance’larını `consume` eder, bu yüzden `dst`
için uygun type `Collection<? super E>`’dir. `PECS mnemonic`, wildcard types kullanımını yönlendiren temel prensibi
ifade eder.

> Get and Put Principle

https://www.codejava.net/java-core/collections/generics-with-extends-and-super-wildcards-and-the-get-and-put-principle

Bu makalede, wildcard'ları ve Get and Put principle'ı anlamana yardımcı olacağız. Java generics'te iki tür wildcard
vardır: `extends wildcard` ve `super wildcard`. İlk türüne bakalım.

1 - `Understand the extends wildcards in Java Generics`

Aşağıda gösterildiği gibi, bir collection içindeki sayıların toplamını hesaplayan bir methodumuz olduğunu varsayalım:

```
public static double sum(Collection<Number> numbers) {
    double result = 0.0D;
    for (Number num : numbers){
        result += num.doubleValue();
    }
    return result;
}
```

Bu makalede açıklandığı gibi, bu method imzasıyla sadece `List<Number>` gönderebiliriz, `List<Double>` veya
`List<Integer>` gönderemeyiz. Eğer böyle bir methodu, aşağıdaki gibi bir integer listesi göndererek çağırmaya
çalışırsak:

```
public static void main(String[] args) {
    List<Integer> integers = Arrays.asList(2, 4, 6);
    double sum = sum(integers);
}
```

Compiler şu hatayı verir:

```
sum(java.util.Collection<java.lang.Number>) in GenericsWildcards
cannot be applied to (java.util.List<java.lang.Integer>)
```

Peki, Number'ın subtype'larından oluşan bir collection'ı nasıl geçebiliriz? Bu case'de, `extends wildcard` çözüm olur.
Bu wildcard, `<? extends E>` kavramına sahiptir. `sum()` methodunu şöyle güncelleyelim:

```
public static double sum(Collection<? extends Number> numbers) {
    double result = 0.0D;
    for (Number num : numbers) {
        result += num.doubleValue();
    }
    return result;
}
```

Artık compile sorunsuz çalışır ve aşağıdaki kod örneğinde olduğu gibi `Number` subtype'larından oluşan bir collection
geçmemize izin verir:

```
public static void main(String[] args) {
    List<Integer> integers = Arrays.asList(2, 4, 6);
    double sum = sum(integers);
    System.out.println("Sum of integers = " + sum); // => 12.0


    List<Double> doubles = Arrays.asList(3.14, 1.68, 2.94);
    sum = sum(doubles);
    System.out.println("Sum of doubles = " + sum); // => 7.76

    List<Number> numbers = Arrays.asList(2, 4, 6, 3.14, 1.68, 2.94);
    sum = sum(numbers);
    System.out.println("Sum of numbers = " + sum); // => 19.76
}
```

Ancak, `extends wildcard` ile declare edilmiş collection’a element ekleyemeyeceğin gibi önemli bir kuralı aklında
tutmalısın. Örneğin, aşağıdaki gibi bir element eklemeye çalışırsak:

```
List<? extends Number> numbers = new ArrayList<>();
numbers.add(123); // => COMPILER ERROR
```

Compiler hata verir! Aksi takdirde, sadece integer sayı kabul eden bir collection’a double sayı ekleyebilirdik. Bu
kuralı anladın mı? Ayrıca, `extends wildcard` ile ilgili birkaç önemli nokta daha şunlardır:

Wildcard context'inde extends keyword'ü, hem subclass'ları hem de interface implementation'larını represent eder.
Örneğin:

```
// Serializable is an interface
List<? extends Serializable> list = new ArrayList<Integer>();
```

List<`?`> ifadesi, `List<? extends Object>` ifadesinin kısaltmasıdır. Bunlar tamamen aynıdır.

`extends wildcard` ile declare edilmiş bir type'a `null` hariç hiçbir şey ekleyemezsin — null her referans type'a
aittir:

```
List<? extends Number> numbers = new ArrayList<Integer>();
numbers.add(null);  // OK
numbers.add(357);   // Compile error
```

2 - `Understand the super wildcard in Java generics`

Aşağıdaki gibi, bir collection’a `N` adet integer sayı ekleyen bir methodumuz olduğunu varsayalım:

```
public static void append(Collection<Integer> integers, int n){
    for (int i = 1; i < n; i++) {
        integers.add(i);
    }
}
```

Ve içine bazı integer’lar eklemek istediğimiz bir number listemiz var:

```
List<Number> numbers = new ArrayList<>();
```

Bu Numbers collection’ını yukarıdaki `append()` methoduna nasıl pass edebiliriz? append() methodu `Collection<Integer>`
kabul ettiği için, `List<Number>` geçirmek yasaktır. extends wildcard bu şekilde kullanılamaz:

```
public static void append(Collection<? extends Integer> integers, int n)
```

Çünkü Number, Integer'ın supertype'ıdır. Peki ya methoda eklenen element’lerin type'ını Integer ile sınırlamak isterken,
aynı zamanda Integer’ın super type’larından oluşan bir collection’ı da kabul etmek istersek — çünkü bir numbers
collection’ına integer eklemek tamamen legaldir? Bu case de, `super wildcard` çözüm olur. `append()` methodunun imzasını
şöyle güncelleyelim:

```
public static void append(Collection<? super Integer> integers, int n){
    for (int i = 1; i < n; i++) {
        integers.add(i);
    }
}
```

Sonra, aşağıdaki gibi bir number listesi geçirmek legal olur:

```
List<Number> numbers = new ArrayList<>();
append(numbers, 5);
System.out.println(numbers); // => [1, 2, 3, 4, 5]

numbers.add(67890);
System.out.println(numbers); // => [1, 2, 3, 4, 5, 67890]
```

`super wildcard` ile, aşağıdaki gibi bir Object listesi de geçebiliriz:

```
List<Object> objects = new ArrayList<>();
append(objects,3);
System.out.println(objects); // => [1, 2, 3]

objects.add("Four");
System.out.println(objects); // => [1, 2, 3, Four]
```

Şimdiye kadar, `super wildcard`'ın ardındaki fikirleri anladığını umuyorum.

Wildcard `<? super T>` ile declare edilmiş bir type ile ilgili diğer önemli noktalar şunlardır:

- T’nin super type’ı olan herhangi bir type’ı kabul edebilir.

- Collection’a element ekleyebiliriz. Ancak type yalnızca `T` ile sınırlıdır.

- `extends wildcard` ile declare edilmiş bir parametreye sahip bir methoda geçirilemez. Aşağıdaki kod örneği bu durumu
  gösterir:

```
public static double sum(Collection<? extends Number> numbers) {
    double result = 0.0D;
    for (Number num : numbers) {
        result += num.doubleValue();
    }
    return result;
}
```

Derived class;

```
List<? super Integer> integers = new ArrayList<>();
sum(integers); // => COMPILER ERROR
```

Peki compiler bunu neden engeller? Aşağıdaki kodu düşün:

```
List<Object> objects = new ArrayList<>();
objects.add("Hello");

List<? super Integer> integers = new ArrayList<>();
integers = objects; // => OK
sum(integers); // => COMPILER ERROR
```

Diyelim compiler kabul etti, `sum()` methodu String'lerin toplamını hesaplayacaktı, ki bu mantıksız olurdu!

3 - `Understand the Get and Put Principle in Java Generics`

Şimdiye kadar, generics ile `extends` ve `super wildcard`'ların temel kavramlarını kavradık. Bir method imzasında mümkün
olduğunca `wildcard` kullanmak iyi bir uygulama olabilir, çünkü bu en geniş call aralığını sağlar. Peki hangi
wildcard’ları kullanacağımıza nasıl karar veririz?

- extends wildcard’ı ne zaman kullanmalıyız?

- super wildcard’ı ne zaman kullanmalıyız?

- Wildcard’ın hiç kullanılması uygun olmayan durumlar nerelerdir?

Neyse ki, karar vermemize yardımcı olan basit bir prensip var. Buna `Get and Put Principle` denir.

Bu prensip şöyle der:

- Sadece bir structure'dan value alıyorsan `extends wildcard` kullan.

- Sadece bir structure'a value ekliyorsan `super wildcard` kullan.

- Hem value `get` edip hem de `put` yapıyorsan wildcard kullanma.

Ve iki istisna vardır:

- `extends wildcard` ile declare edilmiş bir type'a, her referans type'a ait olan `null` dışında hiçbir şey
  ekleyemezsin.

- `super wildcard` ile declare edilmiş bir type'dan, her referans type'ın `super type`'ı olan `Object` type'ı dışında
  hiçbir şey alamazsın.

Yukarıdaki örnekler `(sum() ve append() methodları)` Get and Put principle'ın ilk iki maddesini zaten göstermişti. Son
madde için işte bir örnek:

```
public static double sumNumbers(Collection<Number> numbers, int n) {
    append(numbers, n);
    return sum(numbers);
}
```

Bu method, verilen `N` adet sayının toplamını hesaplar. `sum()` methodu collection’dan değer aldığı için ve `append()`
methodu collection’a değer eklediği için, `sumNumbers()` methodu ilk parametresini hem `sum()` hem de `append()`
methodlarının gereksinimlerini karşılayacak şekilde `Number` türünden bir collection olarak declare etmelidir.

Aşağıdaki kod, örnek bir call'dur:

```
Collection<Number> numbers = new ArrayList<Number>();
double sumOfTen = sumAppend(numbers, 10);
 
System.out.println("Sum = " + sumOfTen); // => Sum = 55.0
```

Java generics'te `extends ve super wildcard`'ların kullanımı böyle olur.

> End of documentation

> Understanding the Get-Put Principle in Generics

https://codingtechroom.com/question/understanding-the-get-put-principle-wildcards-in-java-generics

The get-put principle, generics içinde, özellikle Java’da wildcard kullanımı için bir kılavuzdur. Bu kılavuz, generic
type içine değerler alırken (getting) veya değerler eklerken (putting) `extends`, `super` veya wildcard kullanmama
durumlarını belirlemeye yardımcı olur.

```
// Example of using extends wildcard
List<? extends Number> numbers = new ArrayList<>();
// value'ları okuyabilirsiniz, ancak nonnull herhangi bir value'yu ekleyemezsiniz.

// Example of using super wildcard
List<? super Integer> integers = new ArrayList<>();
// Integer ekleyebilirsiniz, ancak okuma Object type'ı verir
Object o = integers.getFirst();

// Example with no wildcard
List<String> strings = new ArrayList<>();
strings.add("Hello"); // add value
String s = strings.getFirst(); // retrieve value
```

* Generic type'dan value'ları okurken `extends wildcard` kullanın.

* Generic type'a value write ederken `super wildcard` kullanın.

* Hem reading hem de writing operation'ları gerektiğinde wildcard kullanmayın.

### Common mistakes

Hata : Hem value alıp hem value eklemek gerektiğinde `extends` wildcard kullanmak.

Çözüm : Wildcard kullanmadan specific bir type kullanmak.

Hata: `extends wildcard` ile declare edilmiş bir collection'a element eklemeye çalışmak.

Çözüm: `extends wildcard` olan collection'lara sadece `null` eklemek.

Hata: `super wildcard` ile declare edilmiş bir yapıdan her type'da value okuyabileceğinizi varsaymak.

Çözüm: `super wildcard`’dan yalnızca `Object` type'ında value okuyabileceğinizi unutmayın.

> End of documentation

Bu mnemonik akılda tutularak, bu bölümdeki önceki maddelerden bazı method ve constructor declaration’larına bakalım.
Item 28’deki `Chooser` constructor’ı şu declaration’a sahiptir:

```
public Chooser(Collection<T> choices)
```

Bu constructor, `choices` collection’ını yalnızca `T` type'ında value produce etmek için kullanır (ve bunları daha sonra
kullanmak üzere store eder), bu nedenle declaration’ı `T`’yi extend eden bir `wildcard type` kullanmalıdır. İşte ortaya
çıkan constructor declaration:

```
// T producer'i olarak hizmet eden parametre için wildcard type 
public Chooser(Collection<? extends T> choices)
```

Peki bu değişiklik pratikte herhangi bir fark yaratır mı? Evet, yaratır. Elinizde bir `List<Integer>` olduğunu ve bunu
bir `Chooser<Number>` constructor’ına geçirmek istediğinizi varsayalım. Bu, orijinal declaration ile compile edilmezdi,
ancak declaration’a `bounded wildcard type` eklediğinizde compile edilir.

Şimdi de Item 30’daki union method’una bakalım. İşte declaration:

```
public static <E> Set<E> union(Set<E> s1, Set<E> s2)
```

Her iki parametre, `s1` ve `s2`, birer `E` produce'udur, bu yüzden `PECS mnemonik`’i bize declaration’ın şu şekilde
olması gerektiğini söyler:

```
public static <E> Set<E> union(Set<? extends E> s1, Set<? extends E> s2)
```

Return type’ın hâlâ `Set<E>` olduğuna dikkat edin. `Bounded wildcard type`’ları return type olarak kullanmayın.
Kullanıcılarınıza ek esneklik sağlamak yerine, onların client code içinde `wildcard type` kullanmalarını zorunlu kılar.
Gözden geçirilmiş declaration ile bu kod sorunsuz bir şekilde compile edilecektir:

```
public static <E> Set<E> union(Set<? extends E> s1,
                               Set<? extends E> s2) {
    Set<E> result = new HashSet<E>(s1);
    result.addAll(s2);
    return result;
}

Set<Integer> integers = Set.of(1, 2, 3, 4, 5);
Set<Double> doubles = Set.of(1.0, 2.0, 3.0);
Set<? extends Number> union = union(integers, doubles);
System.out.println(union); // => [2.0, 1.0, 1, 2, 3, 4, 5, 3.0]
```

Doğru kullanıldığında, `wildcard type`’lar bir class’ın kullanıcıları için neredeyse görünmezdir. Method’ların kabul
etmesi gereken parametreleri kabul etmesini, reddetmesi gerekenleri ise reddetmesini sağlarlar. Bir class’ın kullanıcısı
wildcard type’lar üzerinde düşünmek zorundaysa, muhtemelen API’sinde bir sorun vardır. Java 8’den önce, type inference
kuralları önceki kod parçasını doğru şekilde handle edebilecek kadar gelişmiş değildi; bu kod, compiler'ın `E`’nin
type'ını infer etmek için bağlamsal `(contextually)` olarak belirtilen return type’ı (veya target type) kullanmasını
gerektirir. Daha önce gösterilen `union` call'unun target type'ı `Set<Number>`’dır. Daha önceki bir Java sürümünde
(Set.of factory için uygun bir alternatifle) bu code parçasını compile etmeye çalışırsanız, aşağıdaki gibi uzun ve
karmaşık bir hata mesajı alırsınız:

```
Union.java:14: error: incompatible types
Set<Number> numbers = union(integers, doubles);
^
required: Set<Number>
found: Set<INT#1>
where INT#1,INT#2 are intersection types:
INT#1 extends Number,Comparable<? extends INT#2>
INT#2 extends Number,Comparable<?>
```

Neyse ki, bu tür hatalarla başa çıkmanın bir yolu vardır. Compiler doğru type'ı infer edemezse, her zaman explicit bir
type argümanı ile hangi type'ı kullanacağını belirtebilirsiniz `[JLS, 15.12]`. Java 8’de hedef tür tanımlaması
`(target typing)` gelmeden önce bile, bunu sık sık yapmanız gerekmezdi, bu iyidir çünkü explicit type argümanları çok
estetik değildir. Burada gösterildiği gibi explicit bir type argümanı eklenince, kod parçası Java 8 öncesi sürümlerde
sorunsuz compile edilir:

```

// Explicit type parametresi - Java 8’den önce gereklidir
Set<Number> numbers = Union.<Number>union(integers, doubles);
```

Şimdi dikkatimizi Item 30’daki max method’una çevirelim. İşte orijinal declaration:

```
public static <T extends Comparable<T>> T max(List<T> list)
```

İşte `wildcard type`’ları kullanan gözden geçirilmiş bir declaration:

```
public static <T extends Comparable<? super T>> T max(List<? extends T> list)
```

Orijinalden gözden geçirilmiş declaration’a geçmek için `PECS` kuralını iki kez uyguladık. Doğrudan `(straightforward)`
uygulama, parametre listesine yapılır. `T` instance'ları produce eder, bu yüzden type'ı `List<T>`’den
`List<? extends T>`’ye değiştirdik. Zor olan uygulama ise type parametresi `T`’ye yapılır. İlk kez bir type
parametresine `wildcard` uygulandığını görüyoruz. Başlangıçta, `T`’nin `Comparable<T>`’yi extend ettiği belirtilmişti,
ancak `Comparable<T>`, `T` instance'larını consume eder (ve order ilişkilerini belirten integer değerler produce
eder). Bu nedenle, parametrized type `Comparable<T>`, `bounded wildcard` type `Comparable<? super T>` ile replace
edilir. Comparable’lar her zaman consumer'dır, bu yüzden genellikle `Comparable<T>` yerine `Comparable<? super T>`
kullanmalısınız. Comparator’lar için de durum aynıdır; bu nedenle, genellikle `Comparator<T>` yerine
`Comparator<? super T>` kullanmalısınız.

Gözden geçirilmiş max declaration muhtemelen bu kitaptaki en karmaşık method declaration’ıdır. Eklenen complexity size
gerçekten bir avantaj sağlar mı? Yine, sağlar. İşte orijinal declaration tarafından dışlanacak `(excluded)`, ancak
gözden geçirilmiş declaration tarafından izin verilen basit bir liste örneği:

```
List<ScheduledFuture<?>> scheduledFutures = ... ;
```

Orijinal method declaration’ını bu listeye uygulayamamanızın sebebi, `ScheduledFuture`’un `Comparable<ScheduledFuture>`
interface'ini implement etmemesidir. Bunun yerine, `Delayed`’in subinterface'idir ve `Delayed`, `Comparable<Delayed>`’i
extend eder. Başka bir deyişle, bir `ScheduledFuture` instance'ı sadece diğer `ScheduledFuture` instance'ları ile
comparable değildir; herhangi bir Delayed instance'ı ile comparable ve bu da orijinal declaration’ın onu reddetmesine
neden olur. Daha genel olarak, `wildcard`, Comparable (veya Comparator) interface'ini doğrudan implement etmeyen ancak
implement eden bir type'ı extend eden type'ları desteklemek için gereklidir.

Wildcard ile ilgili tartışılması gereken bir konu daha var. Type parametreleri ile wildcard’lar arasında bir ikilik
vardır ve birçok method, bunlardan biri kullanılarak tanımlanabilir. Örneğin, bir listedeki iki index'li item'i
değiştirmek için static methoda ait iki olası declaration şunlardır. İlki `unbounded type` parametresi (Item 30)
kullanır, ikincisi ise `unbounded wildcard` kullanır:

```
// swap method için iki olası declaration
public static <E> void swap(List<E> list, int i, int j);
public static void swap(List<?> list, int i, int j);
```

Bu iki declaration'dan hangisi tercih edilir ve neden? Bir public API'de, ikincisi daha iyidir çünkü daha basittir. Bir
list — herhangi bir list — pass edersin ve method, indexlenmiş element'leri swap eder. Dikkate alınması gereken herhangi
bir type parameter yoktur. Genel bir kural olarak, eğer bir type parameter bir method declaration içinde yalnızca bir
kez geçiyorsa, onu bir  `wildcard` ile değiştir. Eğer bu bir `unbounded type parameter` ise, onu bir
`unbounded wildcard` ile replace et; eğer bu bir `bounded type parameter` ise, onu bir `bounded wildcard` ile replace
et.

swap için ikinci declaration'da bir problem var. Doğrudan `(straightforward)` yapılan implementation compile olmaz:

```
public static void swap(List<?> list, int i, int j) {
    list.set(i, list.set(j, list.get(i)));
}
```

Compile etmeye çalışmak, pek yardımcı olmayan bu hata mesajını üretir:

```
Swap.java:5: error: incompatible types: Object cannot be
converted to CAP#1
list.set(i, list.set(j, list.get(i)));
^
where CAP#1 is a fresh type-variable:
CAP#1 extends Object from capture of ?
```

Az önce çıkardığımız bir elementi listeye geri koyamıyor olmamız doğru görünmüyor. Problem, list'in tipi List<`?`> 
olması ve List<`?`> içine `null` dışında hiçbir değer koyamamanızdır. Neyse ki, bu method'u unsafe cast veya raw type
kullanmadan implement etmenin bir yolu var. Fikir, wildcard type'ı capture etmek için private bir helper method
yazmaktır. Helper method, type'ı capture etmek için generic bir method olmalıdır. İşte nasıl göründüğü:

```
public static void main(String[] args) {
    List<String> argList = Arrays.asList("ocean", "joe", "david");
    System.out.println(argList); // => [ocean, joe, david]
    
    swap(argList, 0, argList.size() - 1);
    System.out.println(argList); // => [david, joe, ocean]
}

public static void swap(List<?> list, int i, int j) {
    swapHelper(list, i, j);
}

// Wildcard capture için private helper method
private static <E> void swapHelper(List<E> list, int i, int j) {
    list.set(i, list.set(j, list.get(i)));
}
```

swapHelper method, list'in bir `List<E>` olduğunu bilir. Bu nedenle, bu list'ten aldığı herhangi bir değerin type `E`
olduğunu ve type `E` olan herhangi bir değeri listeye koymanın safe olduğunu bilir. swap'in bu biraz complex
implementation'ı sorunsuz bir şekilde compile olur. Bu, daha complex generic method'dan internally olarak faydalanırken,
`wildcard based` güzel declaration'ı export etmemize olanak tanır. swap method'unun client'ları daha complex olan
swapHelper declaration'ı ile karşılaşmak zorunda kalmaz, ancak ondan fayda sağlarlar. Helper method'un, public method
için fazla complex olarak değerlendirdiğimiz signature'a tam olarak sahip olduğunu belirtmek gerekir.

Özetle, API'lerinde `wildcard type`'ları kullanmak zor olsa da, API'leri çok daha esnek hale getirir. Geniş çapta
kullanılacak bir library yazıyorsan, `wildcard type`'ların doğru kullanımı zorunlu olarak değerlendirilmelidir.
Temel kuralı hatırla: `producer-extends, consumer-super (PECS)`. Ayrıca tüm `comparables` ve `comparators`'ın `consumer`
olduğunu unutma.