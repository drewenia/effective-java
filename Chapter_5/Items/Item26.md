# Don’t use raw types

# Raw Type'ları kullanmayın

Öncelikle birkaç terim: Declaration'ının da bir veya daha fazla type parameter bulunan bir class veya interface, generic
class veya interface olarak adlandırılır `[JLS, 8.1.2, 9.1.2]`. Örneğin, `List` interface’i, element type’ını represent
eden tek bir type parameter’a `(E)` sahiptir. Interface’in tam adı `List<E>`’dir (bu, “E tipinden list” şeklinde
okunur), ancak genellikle kısaca `List` olarak adlandırılır. Generic class ve interface’ler topluca generic type olarak
adlandırılır.

Her generic type, bir dizi parameterized type define eder. Bunlar, generic type’ın resmi `(formal)` type
parameter’larına karşılık gelen gerçek `(actual)` type parameter’larının açılı parantez `(<>)` içinde sınıf veya
interface adını takip etmesiyle oluşur `[JLS, 4.4, 4.5]`. Örneğin, `List<String>` (“String türünden list” şeklinde
okunur) bir parameterized type’tır ve element’leri `String` türünde olan bir listeyi represent eder. (`String`, resmi
`(formal)` type parameter olan `E`’ye karşılık gelen actual type parameter’dır.)

Son olarak, her generic type bir `raw type` define eder; bu, generic type’ın herhangi bir type parameter olmadan,
yalnızca ismiyle kullanılmasıdır `[JLS, 4.8]`. Örneğin, `List<E>` generic type’ına karşılık gelen raw type `List`’tir.
Raw type’lar, sanki type declaration'ınından tüm generic type bilgileri silinmiş gibi davranırlar. Onlar, öncesinde
yazılmış `(pre-generics)` kodlarla uyumluluk için var olurlar.

Java’ya generics eklenmeden önce, bu örnek bir collection declaration'ı olurdu. Java 9 itibarıyla hâlâ geçerli olsa da,
artık örnek olarak kabul edilmez:

```
// Raw collection type - don't do this!
// My stamp collection. Contains only Stamp instances.
private final Collection stamps = ... ;
```

Bugün bu declaration'ı kullanıp yanlışlıkla stamp collection'ınınıza bir madeni para eklerseniz, hatalı ekleme compile
edilir ve hata vermeden çalışır (ancak compiler belirsiz bir uyarı verir):

```
// Stamp collection'ınına madeni para yanlışlıkla eklenmesi
stamps.add(new Coin( ... )); // Emits "unchecked call" warning
```

Madeni parayı stamp collection'ınından almaya çalışana kadar hata almazsınız:

```
// Raw iterator type - don't do this!
for (Iterator i = stamps.iterator(); i.hasNext(); )
    Stamp stamp = (Stamp) i.next(); // Throws ClassCastException
        stamp.cancel();
```

Bu kitap boyunca belirtildiği gibi, hataları yapar yapmaz, ideal olarak compile time'da keşfetmek faydalıdır. Bu case de
hatayı, gerçekleşmesinden çok sonra, hatayı içeren koda uzak olabilecek bir kodda, ancak runtime'da keşfedersiniz.
`ClassCastException` gördüğünüzde, madeni parayı Stamp Collection'nına ekleyen metot invocation'ı bulmak için code
base'inde arama yapmanız gerekir. Compiler size yardımcı olamaz çünkü “Sadece Stamp instance'ları içerir.” comment'ini
anlayamaz.

Generics ile, type declaration information'ı içerir, comment değil:

```
// Parameterized collection type - typesafe
private final Collection<Stamp> stamps = ... ;
```

Bu declaration'a dayanarak, compiler stamps’in yalnızca Stamp instance'ları içermesi gerektiğini bilir ve tüm code
base'iniz uyarı vermeden compile ediliyorsa bunun doğru olduğunu garanti eder. Stamps parameterized type declaration'ı
ile tanımlandığında, hatalı ekleme size tam olarak neyin yanlış olduğunu söyleyen compile time hata mesajı üretir:

```
Test.java:9: error: incompatible types: Coin cannot be converted
to Stamp
c.add(new Coin());
^
```

Compiler, collection'lardan element alırken sizin için görünmez cast ekler ve bunların fail olmayacağını garanti eder.
Yanlışlıkla madeni parayı Stamp koleksiyonuna eklemek kulağa uzak bir ihtimal gibi gelse de, bu problem gerçektir.
Örneğin, sadece BigDecimal instance'ları içermesi gereken bir collection'a yanlışlıkla BigInteger eklemek kolayca hayal
edilebilir.

Daha önce belirtildiği gibi, raw type’ları (type parameter’ları olmadan generic type’lar) kullanmak legal olsa da, asla
yapmamalısınız. Raw type’ları kullanırsanız, generics’in sağladığı tüm güvenlik ve ifade gücü avantajlarını
kaybedersiniz. Kullanılmaması gerektiği halde, dil tasarımcıları neden raw type’lara izin verdi? Compability için.
Java generics eklenirken ikinci on yılına giriyordu ve o zamana kadar generics kullanmayan çok büyük bir code-base'i
vardı. Tüm bu kodun legal kalması ve generics kullanan yeni kodlarla birlikte çalışması kritik olarak görüldü.
Parameterized type'ların instance'larını raw type’lar için tasarlanmış metodlara ve tam tersi şekilde geçirmek legal
olmalıydı. Bu gereksinim, migration compatibility olarak bilinir ve raw type’ları destekleme ile generics’i erasure
`(silme)` kullanarak implementation kararlarını etkiledi.

List gibi raw type’ları kullanmamanız gerekirken, içine herhangi bir Object eklemeye izin veren `List<Object>` gibi
parameterized type'ları kullanmanız sorun değildir. Raw type List ile parametreli type List<Object> arasındaki fark
nedir? Genel olarak, ilki generic type sisteminden çıkmayı seçmişken, ikincisi compiler'a her türden object
tutabileceğini explicitly belirtir. List<String> type'ında ki bir object'i List type'ında ki bir parametreye
geçirebilirsiniz, ancak `List<Object>` type'ında ki bir parametreye geçiremezsiniz. Generics için sub-typing kuralları
vardır ve `List<String>`, raw type List’in subtype'ıdır, ancak parameterized type List<Object>’in subtype'ı değildir.
Bunun sonucu olarak, List gibi raw type’ları kullanırsanız type safety’yi kaybedersiniz, ancak `List<Object>` gibi
parameterized type'ları kullanırsanız kaybetmezsiniz.

Bunu somutlaştırmak için, aşağıdaki programı ele alalım:

```
// Fails at runtime - unsafeAdd method uses a raw type (List)!
public static void main(String[] args) {
    List<String> strings = new ArrayList<>();
    unsafeAdd(strings, Integer.valueOf(42));
    String s = strings.get(0);

}

private static void unsafeAdd(List list, Object o){
    list.add(o);
}
```

Bu program compile edilir, ancak `raw type` olan `List` kullanıldığı için bir uyarı alırsınız:

```
Test.java:10: warning: [unchecked] unchecked call to add(E) as a
member of the raw type List
list.add(o);
^
```

Ve gerçekten de, programı çalıştırırsanız, `strings.get(0)` invocation'ının sonucunu — ki bu bir `Integer`’dır —
`String`’e cast etmeye çalıştığında bir `ClassCastException` alırsınız. Bu, compiler tarafından oluşturulmuş bir cast
işlemidir, bu yüzden normalde başarılı olması garanti edilir — ancak bu durumda compiler uyarısını görmezden geldik ve
bunun bedelini ödedik. Eğer `unsafeAdd` metodunun declaration'ınında ki raw type `List` yerine parameterized type
`List<Object>` kullanırsanız ve programı yeniden compile etmeye çalışırsanız, artık compile edilmediğini ve şu hata
mesajını verdiğini görürsünüz:

```
Test.java:5: error: incompatible types: List<String> cannot be
converted to List<Object>
unsafeAdd(strings, Integer.valueOf(42));
^
```

Element type’ı bilinmeyen ve önemli olmayan bir collection için raw type kullanmaya eğilimli olabilirsiniz. Örneğin, iki
set alan ve ortak element sayısını döndüren bir metot yazmak istediğinizi varsayalım.

Generics konusunda yeniyseniz, böyle bir metodu şu şekilde yazabilirsiniz:

```
// Unknown element type'ı için raw type kullanımı – bunu yapmayın!
static int numElementsInCommon(Set s1, Set s2){
    int result = 0;
    for (Object o1 : s1){
        if (s2.contains(o1))
            result++;
    }
    return result;
}
```

Bu metot çalışır, ancak raw type’lar kullandığı için tehlikelidir. Güvenli alternatif ise `unbounded wildcard type`’ları
kullanmaktır. Bir generic type kullanmak istiyor ancak actual type parametresinin ne olduğunu bilmiyor ya da
umursamıyorsanız, onun yerine bir soru işareti `(?)` kullanabilirsiniz. Örneğin, `Set<E>` generic type’ı için unbounded
wildcard type, `Set<?>` şeklindedir (şöyle okunur: “set of some type”). Bu, herhangi bir set’i tutabilen en genel
parameterized Set type’ıdır.

İşte `numElementsInCommon` metodunun unbounded wildcard type’larla yazılmış hali:

```
// Unbounded wildcard type kullanır – typesafe ve esnektir.
static int numElementsInCommon(Set<?> s1, Set<?> s2) {
    int result = 0;
    for (Object o1 : s1) {
        if (s2.contains(o1))
            result++;
    }
    return result;
}
```

Unbounded wildcard type `Set<?>` ile raw Set type'ı arasındaki fark nedir. Soru işareti gerçekten size bir avantaj
sağlar mı `(?)`. Detaya girmemek için, wildcard type safe'dir, raw type ise güvenli değildir. Raw type ile bir
collection'a herhangi bir elementi koyabilirsiniz, bu da collection'ın type değişmezini `(invariant)` kolayca bozabilir
(unsafeAdd methodu ile gösterildiği gibi):

`Collection<?>` içine (null dışında) herhangi bir element koyamazsınız. Bunu yapmaya çalışmak compile time'da
aşağıdaki gibi bir hata mesajı oluşturur:

```
WildCard.java:13: error: incompatible types: String cannot be
converted to CAP#1
c.add("verboten");
^
where CAP#1 is a fresh type-variable:
CAP#1 extends Object from capture of ?
```

```
List<?> strings = new ArrayList<>();
strings.add("Test"); // => COMPILER ERROR
```

Kabul etmek gerekir ki bu hata mesajı pek tatmin edici değildir, ancak compiler görevini yerine getirmiştir; element
türü ne olursa olsun, collection'ın type değişmezlerinin `(invariant)` bozulmasını engellemiştir. `Collection<?>`
içine (null dışında) herhangi bir element koyamayacağınız gibi, içinden aldığınız object'lerin türü hakkında da herhangi
bir varsayımda bulunamazsınız. Bu kısıtlamalar kabul edilemezse, generic metotlar veya bounded wildcard type'ları
kullanabilirsiniz.

Raw type'ları kullanmamanız gerektiği kuralının birkaç küçük istisnası vardır. Class literal'larında raw type'ları
kullanmanız gerekir. Spesifikasyon, parameterized type'ların kullanılmasına izin vermez (ancak array type'larına ve
primitive type'lara izin verir) `[JLS, 15.8.2]`. Başka bir deyişle, `List.class`, `String[].class` ve `int.class` legal
ifadelerdir, ancak `List<String>.class` ve `List<?>.class` legal değildir.

Kuralın ikinci bir istisnası `instanceof` operatörüyle ilgilidir. Generic type information runtime'da silindiği için,
unbounded wildcard type'lar dışındaki parameterized türlerde `instanceof` operatörünü kullanmak legal değildir. Raw
type'lar yerine unbounded wildcard type'larının kullanılması, `instanceof` operatörünün davranışını hiçbir şekilde
etkilemez. Bu case de, açılı parantezler `(<>)` ve soru işaretleri `(<?>)` sadece gürültüdür. Generic türlerle
`instanceof` operatörünü kullanmanın tercih edilen yolu budur:

```
//Raw type'ın legal kullanımı – instanceof operatörü
if (o instanceof Set) { // Raw type
    Set<?> s = (Set<?>) o; // Wildcard type
    ...
}
```

`o`’nun bir `Set` olduğunu belirledikten sonra, onu raw type `Set` yerine wildcard type'ı `Set<?>` olarak dönüştürmeniz
gerektiğini unutmayın. Bu, checked bir cast'dir, bu yüzden compiler uyarısına neden olmaz.

Özetle, raw type'ların kullanımı runtime'da exception'lara yol açabilir, bu yüzden onları kullanmayın. Raw type'lar
yalnızca generic'lerin tanıtılmasından önceki legacy code'lar ile uyumluluk ve birlikte çalışabilirlik sağlamak amacıyla
sunulmuştur. Hızlı bir özet olarak, `Set<Object>` herhangi bir type'da object içerebilen parameterized bir type'dır,
`Set<?>` sadece `unknown` bir type'da object içerebilen wildcard type'dır ve `Set` ise generic type sisteminden vazgeçen
raw type'dır. İlk ikisi güvenlidir, sonuncusu ise güvenli değildir.