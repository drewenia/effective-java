# Combine generics and varargs judiciously

Varargs metotları (Item 53) ve generics, Java 5'te platforma eklendiğinden, birlikte sorunsuz çalışmaları beklenebilir;
ne yazık ki, durum böyle değildir. Varargs'in amacı, client'ların bir metoda değişken sayıda argument geçmesine izin
vermektir, ancak bu sızıntılı `(leaky)` bir abstraction'dır. Bir varargs method invocation'ı yaptığınızda, varargs
parameter'ları tutmak için bir array oluşturulur; implementation detaylarından biri olması gereken bu array görünür
durumdadır. Sonuç olarak, varargs parameter'lar generic veya parameterized type'lara sahip olduğunda kafa karıştırıcı
compiler warning'leri alırsınız. Item 28'den hatırlanacağı üzere, `non-reifiable type`, runtime representation'ı
compile-time representation'ından daha az bilgiye sahip olan type'tır ve neredeyse tüm generic ve parameterized type'lar
`non-reifiable`'dır. Bir method, varargs parameter'ını `non-reifiable` type olarak declare ederse, compiler bu
declaration üzerinde bir warning üretir. Eğer method, inferred type'ı non-reifiable olan varargs parameter'lar ile
invoke edilirse, compiler invocation üzerinde de bir warning üretir. Warning'ler şu şekilde görünür:

```
warning: [unchecked] Possible heap pollution from
parameterized vararg type List<String>
```

`Heap pollution`, parameterized type'a sahip bir variable'ın, o type'a ait olmayan bir object'e referans verdiğinde
meydana gelir `[JLS, 4.12.2]`. Bu durum, compiler tarafından otomatik olarak üretilen cast'lerin başarısız olmasına
neden olabilir ve generic type system'in temel garantisini ihlal eder. Şu method'u ele alalım:

```
public static void main(String[] args) {
    dangerous(List.of("There be dragons!"));
}

static void dangerous(List<String>... stringLists){
    List<Integer> intList = List.of(42);
    Object[] objects = stringLists;
    objects[0] = intList; // Heap pollution
    String s = stringLists[0].get(0); // Class cast exception
}
```

`objects[0] = intList` assignment'i yapıldığı anda `stringsLists` içerisinde ki değer String'den intList'de ki `42`
değerine set edilir. Dolayısıyla `stringLists[0].get(0);` 42 değerini döndürür.

Bu method görünür bir cast içermemesine rağmen, bir veya daha fazla argument ile invoke edildiğinde ClassCastException
fırlatır. Son satırında compiler tarafından otomatik olarak üretilen görünmez bir cast vardır. Bu cast başarısız olur,
bu da type safety'nin ihlal edildiğini gösterir ve generic varargs array parameter'ına değer atamanın unsafe olduğunu
kanıtlar.

Bu örnek ilginç bir soru ortaya çıkarır: Generic bir array'in açıkça oluşturulması yasaklanmışken, generic varargs
parameter'lı bir method declare etmek neden legal? Kod parçası neden bir error üretir? Cevap, generic veya parameterized
type'lara sahip varargs parameter'lı method'ların pratikte çok kullanışlı olabilmesi nedeniyle, dil tasarımcılarının bu
tutarsızlığı kabul etmeyi tercih etmesidir.

Aslında, Java library'leri `Arrays.asList(T... a)`, `Collections.addAll(Collection<? super T> c, T... elements)` ve
`EnumSet.of(E first, E... rest)` gibi birkaç böyle method'u export ederler. Daha önce gösterilen `dangerous` method'un
aksine, bu library method'ları typesafe'dir. Java 7'den önce, generic varargs parameter'a sahip bir method'un yazarı,
call site'larda ki warning'ler için yapabileceği hiçbir şey yoktu. Bu durum, bu API'lerin kullanımını hoş olmayan hale
getiriyordu. Kullanıcılar, warning'lere katlanmak zorundaydı veya tercihen, her call site'ında
`@SuppressWarnings("unchecked")` annotation'ları ile onları ortadan kaldırmalıydı (Item 27). Bu sıkıcıydı,
okunabilirliği düşürüyordu ve gerçek sorunları işaret eden warning'leri gizliyordu.

Java 7'de, generic varargs parameter'a sahip bir method yazarı için client warning'lerini otomatik olarak suppress
etmeye olanak tanıyan `SafeVarargs` annotation'ı platforma eklendi. Özünde, `SafeVarargs` annotation'ı, bir method
yazarının methodun typesafe olduğu yönünde verdiği bir sözdür. Bu söz karşılığında, compiler method kullanıcılarını
call'larının unsafe olabileceği konusunda uyarmamayı kabul eder.

Bir method gerçekten safe değilse, ona `@SafeVarargs` annotation'ı eklememen çok önemlidir. Bunu sağlamak için ne
gerekir? Method invoke edildiğinde, varargs parameter'ları tutmak için generic bir array oluşturulduğunu hatırla.
Method, array'e hiçbir şey store etmezse (bu, parameter'ların override edilmesi olur) ve array referansının dışarı
çıkmasına izin vermezse (bu, untrusted kodun array'e erişimini sağlar), o zaman method safe'dir. Başka bir deyişle,
varargs parameter array sadece caller'dan method'a değişken sayıda argument iletmek için kullanılıyorsa—ki varargs'in
amacı budur — o zaman method safe'dir.

Varargs parameter array'e hiçbir şey atamasanız bile type safety'yi ihlal edebileceğinizi belirtmekte fayda var.
Parametrelerini içeren bir array döndüren aşağıdaki generic varargs method'u düşünün. İlk bakışta, kullanışlı küçük bir
utility method gibi görünebilir:

```
// UNSAFE - Generic parameter array'ine bir referans açığa çıkarıyor (expose)!
static <T> T[] toArray(T... args){
    return args;
}
```

Bu method, varargs parameter array'ini basitçe döndürür. Method tehlikeli görünmeyebilir, ama öyledir! Bu array'in
type'ı, method'a geçirilen argument'ların compile-time type'larına göre belirlenir ve compiler, doğru bir belirleme
yapmak için yeterli bilgiye sahip olmayabilir. Bu method, varargs parameter array'ini döndürdüğü için, heap pollution'ı
call stack boyunca yayabilir.

Bunu somutlaştırmak için, `T` type'ında üç argument alan ve bunlardan ikisini rastgele seçerek içeren bir array döndüren
aşağıdaki generic method'u düşünün:

```
// UNSAFE - Generic parameter array'ine bir referans açığa çıkarıyor (expose)!
static <T> T[] toArray(T... args) {
    return args;
}

static <T> T[] pickTwo(T a, T b, T c) {
    switch (ThreadLocalRandom.current().nextInt(3)){
        case 0 : return toArray(a,b);
        case 1 : return toArray(a,c);
        case 2 : return toArray(b,c);
    }
    throw new AssertionError(); // buraya gelemiyorum
}

public static void main(String[] args) {
    String[] attributes = pickTwo("Good","Fast","Cheap");
    System.out.println(Arrays.toString(attributes));
}
```

`pickTwo` method'u, kendi başına tehlikeli değildir ve generic varargs parameter'a sahip olan toArray method'unu invoke
etmesi dışında bir warning üretmezdi. Bu method compile edilirken, compiler, iki `T` instance'ını toArray'e pass etmek
için bir `varargs parameter array`'i oluşturacak kod üretir. Bu kod, `Object[]` type'ında bir array allocate eder; çünkü
call site'ında `pickTwo`'ya hangi type'ta object geçirilirse geçilsin, bu instance'ları tutacağı garanti edilen en
specific type budur. `toArray` method'u bu array'i `pickTwo`'ya döndürür, o da caller'ına geri döndürür, dolayısıyla
`pickTwo` her zaman `Object[]` type'ında bir array döndürür.

Şimdi, `pickTwo`'yu çalıştıran aşağıdaki main method'u düşünün:

```
String[] attributes = pickTwo("Good","Fast","Cheap");
```

Bu method'da hiçbir yanlışlık yoktur, bu yüzden hiçbir warning üretmeden compile edilir. Ancak çalıştırıldığında,
görünür bir cast içermemesine rağmen `ClassCastException` throw eder. Görmediğiniz şey, compiler'ın `pickTwo` tarafından
döndürülen değere `String[]` için gizli bir cast ürettiğidir, böylece attributes içinde saklanabilir. Cast başarısız
olur, çünkü `Object[]` `String[]`'in bir subtype'ı değildir. Bu başarısızlık oldukça rahatsız edicidir çünkü heap
pollution'a aslında neden olan method'dan `(toArray)` iki seviye uzaktadır ve varargs parameter array'i, gerçek
parametreler içine yerleştirildikten sonra modify edilemez.

Bu örnek, başka bir method'a generic varargs parameter array'ine erişim vermenin unsafe olduğunu göstermek içindir, iki
istisna dışında: Array'i doğru şekilde `@SafeVarargs` ile annotated başka bir varargs method'a geçirmek güvenlidir ve
array'i sadece content'inin bir fonksiyonunu compute eden `non-varargs` bir method'a geçirmek de güvenlidir.

İşte generic varargs parameter'ın güvenli kullanımına tipik bir örnek. Bu method, herhangi sayıda listeyi argument
olarak alır ve tüm input list'lerin element'lerini sıralı şekilde içeren tek bir liste döndürür. Method `@SafeVarargs`
ile annotated edildiği için, ne declaration'da ne de call site'larında herhangi bir warning üretmez:

```
// Generic varargs parameter'a sahip güvenli method
@SafeVarargs
static <T> List<T> flatten(List<? extends T>... lists) {
    List<T> result = new ArrayList<>();
    for (List<? extends T> list : lists) {
        result.addAll(list);
    }
    return result;
}
```

`SafeVarargs` annotation'ının ne zaman kullanılacağına karar verme kuralı basittir: Generic veya parameterized type'a
sahip varargs parameter'lı her method'a `@SafeVarargs` kullanın, böylece kullanıcıları gereksiz ve kafa karıştırıcı
compiler warning'lerle yormamış olursunuz. Bu, `dangerous` veya `toArray` gibi `unsafe varargs` method'lar asla
yazmamanız gerektiği anlamına gelir. Compiler, kontrolünüzdeki bir method'daki generic varargs parameter'dan kaynaklanan
olası `heap pollution` konusunda sizi her uyardığında, method'un safe olup olmadığını kontrol edin. Hatırlatma olarak,
bir generic varargs method aşağıdaki durumlarda safe kabul edilir:

1 - varargs parameter array'ine hiçbir şey store etmiyorsa ve

2 - array'i (veya bir clone'unu) untrusted koda görünür hale getirmiyorsa. Bu yasaklardan herhangi biri ihlal
ediliyorsa, düzeltin.

`SafeVarargs` annotation'ının yalnızca `override edilemeyen` method'lar üzerinde legal olduğunu unutmayın; çünkü her
olası overriding method'un safe olacağı garanti edilemez. Java 8'de, bu annotation yalnızca static method'lar ve final
instance method'lar üzerinde legaldi; Java 9'da ise, `private instance method`'lar üzerinde de legal hale geldi.
SafeVarargs annotation'ı kullanmaya alternatif olarak, Item 28'in önerisini takip edebilir ve (aslında bir array olan)
varargs parameter yerine bir List parameter kullanabilirsiniz. Bu yaklaşım flatten method'umuza uygulandığında şöyle
görünür: Sadece parameter declaration'ının değiştiğine dikkat edin:

```
List<Integer> flatten = flatten(List.of(
        List.of(1, 2),
        List.of(3, 4, 5),
        List.of(6, 7)
));

System.out.println(flatten); // => [1, 2, 3, 4, 5, 6, 7]

static <T> List<T> flatten(List<List<? extends T>> lists) {
    List<T> result = new ArrayList<>();
    for (List<? extends T> list : lists) {
        result.addAll(list);
    }
    return result;
}
```

Bu method, değişken sayıda argumente izin vermek için static factory method `List.of` ile birlikte kullanılabilir.
Bu yaklaşımın, `List.of` declaration'ının `@SafeVarargs` ile annotated olmasına dayandığını unutmayın:

```
audience = flatten(List.of(friends, romans, countrymen));
```

Bu yaklaşımın avantajı, compiler'ın methodun typesafe olduğunu kanıtlayabilmesidir. Methodun güvenliği için SafeVarargs
annotation'ı ile taahhüt vermenize gerek yoktur ve güvenli olduğunu belirlerken hata yapmış olabileceğiniz endişesi
taşımazsınız. Ana dezavantajı, client kodunun biraz daha ayrıntılı olması ve biraz daha yavaş çalışabilmesidir. Bu
yöntem, `toArray` method'unda olduğu gibi, safe bir varargs method yazmanın mümkün olmadığı durumlarda da
kullanılabilir. List muadili, `List.of` method'u olduğundan, onu yazmamıza bile gerek yoktur; Java library yazarları bu
işi bizim için yapmıştır. `pickTwo` method'u şu hale gelir:

```
public static void main(String[] args) {
    List<String> attributes = pickTwo("good","bad","ugly");
    System.out.println(attributes); // => Degiskenlik gosterir [bad,ugly]
}

static <T> List<T> pickTwo(T a, T b, T c) {
    return switch (ThreadLocalRandom.current().nextInt(3)) {
        case 0 -> List.of(a, b);
        case 1 -> List.of(a, c);
        case 2 -> List.of(b, c);
        default -> throw new AssertionError();
    };
}
```

Ortaya çıkan kod typesafe'dir çünkü sadece generics kullanır, array'leri değil.

Özetle, varargs ve generics iyi etkileşmez çünkü varargs, array'ler üzerine kurulmuş sızıntılı `(leaky)` bir
abstraction'dır ve array'lerin generics'ten farklı type kuralları vardır. Generic varargs parameter'lar typesafe
olmasalar da legaldirler. Generic (veya parameterized) varargs parameter'a sahip bir method yazmayı seçerseniz, önce
methodun typesafe olduğundan emin olun ve sonra kullanımını zorlaştırmamak için @SafeVarargs ile annotate edin.