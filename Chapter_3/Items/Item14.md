# Consider implementing Comparable

Bu bölümde ele alınan diğer metodların aksine, compareTo metodu Object sınıfında declare edilmemiştir. Daha ziyade,
Comparable interface'inde ki tek metottur. Bu metot, Object'in `equals` metoduna benzer; ancak basit equality
comparison'larına ek olarak sıra `(order)` comparison'larına da izin verir ve generic'tir. Bir sınıf, Comparable'ı
implement ederek, instance'larının natural bir ordering'e sahip olduğunu belirtir. Comparable'ı implement eden bir
object array'ini sorting şu kadar basittir:

```
Arrays.sort(a);
```

Benzer şekilde, Comparable object'lerinin otomatik olarak sorted collection'larını search etmek, extreme value'ları
compute etmek ve sürdürmek `(maintain)` de kolaydır. Örneğin, String'in Comparable'ı implement etmesinden faydalanan
aşağıdaki program, command line argümanlarının duplicate olanlarını kaldırılmış, alfabetik olarak sıralanmış bir
listesini yazdırır:

```
public class WordList {
    public static void main(String[] args) {
        Set<String> s = new TreeSet<>();
        Collections.addAll(s, args);
        System.out.println(s);
    }
}
```

Comparable'ı implement ederek, sınıfınızın bu interface'e bağlı olan `(interoperate)` birçok generic algoritma ve
collection implementasyonu ile birlikte çalışmasını sağlarsınız. Küçük bir çabayla muazzam bir güç kazanırsınız. Java
platform library'lerinde ki hemen hemen tüm value class'ları ve tüm enum type'ları, Comparable'ı implement eder.
alphabetical, numerical veya chronological order gibi açıkça natural bir ordering'e sahip bir value class'ı
yazıyorsanız, Comparable interface'ini implement etmelisiniz:

```
public interface Comparable<T> {
    int compareTo(T t);
}
```

`compareTo` metodunun general contract'ı, equals metodununkiyle benzerdir: Bu object'i, belirtilen object ile order'a
göre compare eder. Bu object belirtilen object'ten küçükse `negative` bir integer, eşitse `zero` veya büyükse `positive`
bir integer döndürür. Belirtilen object'in type'ı, bu object ile compare edilmesini engelliyorsa `ClassCastException`
fırlatır.

Aşağıdaki açıklamada, `sgn(expression)` notasyonu, expression'ın değeri `negative`, `zero` veya `pozitive` olmasına göre
`-1`, `0` veya `1` döndürmek üzere tanımlanmış matematiksel `signum` fonksiyonunu belirtir.

* Implementor, tüm `x` ve `y` için `sgn(x.compareTo(y)) == -sgn(y.compareTo(x))` olmasını sağlamalıdır. (Bu durum,
  `x.compareTo(y)`'nin ancak ve ancak `y.compareTo(x)` bir exception fırlatırsa bir exception fırlatması gerektiği
  anlamına gelir.)

* Implementor, ilişkinin `(relation)` transitive olmasını da sağlamalıdır: `(x.compareTo(y) > 0 ve y.compareTo(z) > 0)`
  `x.compareTo(z) > 0` olduğunu ima eder.

* Sonunda, Implementor, `x.compareTo(y) == 0` olmasının, tüm `z`'ler için `sgn(x.compareTo(z)) == sgn(y.compareTo(z))`
  olmasını ima etmesini sağlamalıdır.

* Şiddetle tavsiye edilmekle birlikte zorunlu değildir: `(x.compareTo(y) == 0) == (x.equals(y))`. Genel olarak
  konuşursak, Comparable interface'ini implement eden ve bu condition'ı ihlal eden herhangi bir sınıf, bu gerçeği açıkça
  belirtmelidir. Önerilen ifade şudur: "Not: Bu sınıfın, equals ile tutarsız natural bir ordering'i vardır."

Bu general contract'ın matematiksel doğası sizi korkutmasın. equals general contract'ı gibi, bu general contract da
göründüğü kadar karmaşık değildir. Tüm object'ler üzerinde global bir equivalence ilişkisi `(relation)` dayatan equals
metodunun aksine, `compareTo` farklı type'larda ki object'ler arasında çalışmak zorunda değildir: Farklı type'larda ki
object'ler ile, `compareTo`'nun `ClassCastException` fırlatmasına izin verilir. Genellikle, yaptığı tam da budur.
Contract, compare edilen object'ler tarafından implement edilen bir interface'de typically olarak define edilen type'lar
arası comparison'lara izin verir.

Tıpkı hashCode contract'ını ihlal eden bir sınıfın, hashing'e bağlı diğer sınıfları bozabileceği gibi, compareTo
contract'ını ihlal eden bir sınıf da comparison'a depend diğer sınıfları bozabilir. Comparison'a depend sınıflar
arasında sorted collection'lar `TreeSet` ve `TreeMap` ile searching ve sorting algoritmaları içeren utility sınıflar
`Collections` ve `Arrays` bulunur.

`compareTo` contract'ının hükümlerini gözden geçirelim.

1 - İlk hüküm, iki reference arasındaki bir comparison'un yönünü tersine çevirirseniz, beklenen şeyin olduğunu söyler:
Eğer ilk object ikinciden küçükse, o zaman ikinci, birinciden büyük olmalıdır; Eğer ilk object ikinciye eşitse, o zaman
ikinci, birinciye eşit olmalıdır; Ve eğer ilk object ikinciden büyükse, o zaman ikinci, birinciden küçük olmalıdır.

2 - İkinci hüküm, eğer bir object ikinciden büyükse ve ikinci de üçüncüden büyükse, o zaman birincinin üçüncüden büyük
olması gerektiğini söyler.

3 - Son hüküm, eşit olarak compare edilen tüm object'lerin başka herhangi bir object ile compare edildiğinde aynı
sonuçları vermesi gerektiğini söyler.

Bu üç hükmün bir sonucu olarak, bir compareTo metodu tarafından uygulanan eşitlik testi, equals contract'ı tarafından
uygulanan aynı kısıtlamalara uymalıdır: `reflexivity, symmetry, transitivity`. Bu nedenle, aynı uyarı geçerlidir:
compareTo contract'ını koruyarak yeni bir value component'i ile instantiable bir sınıfı extend etmenin bir yolu yoktur;
ancak object-oriented abstraction'ın faydalarından vazgeçmeye istekli değilseniz. Aynı geçici çözüm burada da
geçerlidir. Comparable interface'ini implement eden bir sınıfa value component'i eklemek istiyorsanız, onu extend
etmeyin; İlk sınıfın bir instance'ini içeren, onunla ilişkisiz `(unrelated)` bir sınıf yazın. Ardından, contained
instance'ı döndüren bir "view" metodu sağlayın. Bu, containing sınıf üzerinde istediğiniz compareTo metodunu implement
etmenizi sağlar; aynı zamanda client'ının, ihtiyaç duyulduğunda containing sınıfın bir instance'ını, contained sınıfın
bir instance'ı olarak görmesine olanak tanır.

compareTo contract'ının son paragrafı, gerçek bir gereklilikten ziyade güçlü bir öneri olup, compareTo metodu tarafından
uygulanan equality testinin genel olarak equals metoduyla aynı sonuçları döndürmesi gerektiğini belirtir. Eğer bu hükme
uyulursa, compareTo metodu tarafından uygulanan ordering'in equals ile tutarlı olduğu söylenir. Eğer ihlal edilirse,
ordering'in equals ile tutarsız olduğu söylenir. compareTo metodu, equals ile tutarsız bir order dayatan bir sınıf
yine de çalışacaktır, ancak bu sınıfın element'lerini içeren sorted collection'lar, ilgili collection interface'lerinin
`(Collection, Set veya Map)` general contract'larına uymayabilir. Bunun nedeni, bu interface'ler için general contract'
ların equals metodu cinsinden tanımlanmış olmasıdır, ancak sorted collection'lar equals yerine compareTo tarafından
uygulanan eşitlik testini kullanır. Bu durum bir felaket değil, ancak farkında olunması gereken bir şey.

Örneğin, compareTo metodu equals ile tutarsız olan `BigDecimal` sınıfını düşünün. Eğer empty bir HashSet instance'ı
oluşturup ardından `new BigDecimal("1.0")` ve `new BigDecimal("1.00")` eklerseniz, set iki element içerecektir çünkü
set'e eklenen iki `BigDecimal` instance'ı, equals metodu kullanılarak compare edildiğinde eşit değildir. Ancak, aynı
prosedürü bir `HashSet` yerine bir `TreeSet` kullanarak yaparsanız, set yalnızca bir element içerecektir çünkü iki
`BigDecimal` instance'ı, compareTo metodu kullanılarak compare edildiğinde eşittir.

Bir compareTo metodu yazmak, bir equals metodu yazmaya benzer, ancak birkaç temel fark vardır. Comparable interface'i
parameterized olduğundan, compareTo metodu statically olarak type'landırılmıştır, bu nedenle argümanını type check
etmenize veya cast etmenize gerek yoktur. Eğer argüman yanlış type'da ise, invocation compile edilmeyecektir bile. Eğer
argüman null ise, metot member'larına erişmeye çalıştığı anda bir `NullPointer-Exception` fırlatmalıdır ve
fırlatacaktır.

Bir compareTo metodunda, field'ler equality'den ziyade order'a göre compare edilir. Object reference field'lerini
compare için compareTo metodunu recursively olarak invoke edin. Eğer bir field Comparable interface'ini implement
edemiyorsa veya standart olmayan bir ordering'e ihtiyacınız varsa, bunun yerine bir `Comparator` kullanın. Kendi
comparator'unuzu yazabilir veya CaseInsensitiveString için bu compareTo metodunda olduğu gibi mevcut bir tane
kullanabilirsiniz:

```
public class CaseInsensitiveString implements Comparable<CaseInsensitiveString> {
    private final String s;

    public CaseInsensitiveString(String s) {
        this.s = Objects.requireNonNull(s);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof CaseInsensitiveString &&
                ((CaseInsensitiveString) o).s.equalsIgnoreCase(s);
    }

    @Override
    public int hashCode() {
        return s.hashCode();
    }

    @Override
    public String toString() {
        return s;
    }

    public int compareTo(CaseInsensitiveString cis) {
        return String.CASE_INSENSITIVE_ORDER.compare(s, cis.s);
    }
}
```

`CaseInsensitiveString` sınıfının `Comparable<CaseInsensitiveString>` interface'ini implement ettiğine dikkat edin. Bu,
bir `CaseInsensitiveString` referansının yalnızca başka bir `CaseInsensitiveString referansıyla compare edilebileceği
anlamına gelir. Bir sınıfı Comparable implement edecek şekilde declare ederken izlenecek normal pattern budur.

Bu kitabın önceki baskıları, compareTo metotlarının integer primitive field'leri `<and>` relational operatörlerini
kullanarak compare edilmesini ve floating-point primitive field'leri `Double.compare` ve `Float.compare` static
metotlarını kullanarak compare etmesini önermiştir. Java 7'de, Java'nın tüm boxed primitive sınıflarına static compare
metotları eklendi. compareTo metotlarında `<and>` ilişkisel `(relational)` operatörlerinin kullanılması uzun ve hataya
açıktır ve artık önerilmemektedir.

Eğer bir sınıfın birden fazla önemli `(significant)` field'i varsa, bunları compare ettiğiniz sıra kritiktir. En önemli
field'den başlayın ve aşağı doğru ilerleyin. Eğer bir comparison sıfırdan (ki bu equality'i represent eder) farklı bir
sonuç verirse, işiniz bitmiş demektir; sadece result'ı döndürün. Eğer en önemli field equal ise, bir eşit olmayan field
bulana kadar veya en önemsiz field'i compare edinceye kadar bir sonraki en önemli field'i compare edin ve bu şekilde
devam edin. İşte PhoneNumber sınıfı için bu tekniği gösteren bir compareTo metodu:

```
// Multiple-field Comparable with primitive fields
public int compareTo(PhoneNumber pn) {
    int result = Short.compare(areaCode, pn.areaCode);
    if (result == 0) {
        result = Short.compare(prefix, pn.prefix);
        if (result == 0)
          result = Short.compare(lineNum, pn.lineNum);
        }
    return result;
}
```

Java 8'de, Comparator interface'i, comparator'lerin akıcı `(fluent)` bir şekilde oluşturulmasını sağlayan bir dizi
comparator constructions metodu ile donatılmıştır. Bu comparator'ler daha sonra Comparable interface'inin gerektirdiği
şekilde bir compareTo metodu implement etmek için kullanılabilir. Birçok programcı bu yaklaşımın kısa ve öz olmasını
tercih eder, ancak bunun mütevazı bir performans maliyeti vardır: PhoneNumber instance'larının array'lerini sort etmek
benim makinemde yaklaşık `%10` daha yavaştır. Bu yaklaşımı kullanırken, netlik ve kısa ve öz olmak adına static
comparator construction metotlarına basit adlarıyla başvurabilmek için Java'nın static import facility özelliğini
kullanmayı düşünün. İşte bu yaklaşımı kullanarak PhoneNumber için compareTo metodunun görünümü:

```
// Comparable with comparator construction methods
private static final Comparator<PhoneNumber> COMPARATOR =
            comparingInt((PhoneNumber pn) -> pn.areaCode)
                    .thenComparingInt(pn -> pn.prefix)
                    .thenComparingInt(pn -> pn.lineNum);
}
```

Bu implementation, sınıfın initialization'ı sırasında, iki comparator oluşturma metodunu kullanarak bir comparator
oluşturur. Ilki `comparingInt`'tir. Bu, bir object referansını `int` type'ında bir key'e map eden bir key extractor
function alan ve instance'ları bu key'e göre sıralayan bir comparator döndüren static bir metottur. Önceki örnekte,
`comparingInt`, bir PhoneNumber'dan `areaCode`'unu extract eden bir `lambda ()` alır ve telefon numaralarını
`areaCode`'larına göre order eden bir `Comparator`. Lambda'nın input parametresinin type'ını `(PhoneNumber pn)`
explicitly belirttiğine dikkat edin. Görünüşe göre bu durumda, Java'nın `type inference`'ı type'ı kendi başına bulmak
için yeterince güçlü değil, bu yüzden programın compile edilmesini sağlamak için ona yardım etmek zorunda kalıyoruz.

Eğer iki telefon numarasının `areaCode`'u aynıysa, comparison'u daha da inceltmemiz gerekir ve ikinci comparator
construction metodu olan `thenComparingInt` tam da bunu yapar. Bu, bir `int` key extractor fonksiyonu alan ve önce
orijinal comparator'ı uygulayan, ardından equality durumlarını çözmek için extracted key'i kullanan bir comparator
döndüren `Comparator` üzerinde bir instance metottur. İstediğiniz kadar `thenComparingInt` call'unu art arda
kullanabilirsiniz, bu da lexicographic bir ordering ile sonuçlanır. Yukarıdaki örnekte, secondary key `prefix` ve
üçüncül key'i `lineNum` olan bir ordering ile sonuçlanan iki `thenComparingInt` call'unu art arda kullanıyoruz.
`thenComparingInt` call'larının hiçbirine iletilen key extractor fonksiyonunun parametre type'ını belirtmemize gerek
kalmadığına dikkat edin: Java'nın type inference'ı bunu kendi başına çözebilecek kadar akıllıydı.

Comparator sınıfı, tam bir construction metod setine sahiptir. `comparingInt` ve `thenComparingInt` metotlarının `long`
ve `double` primivite type'ları için benzerleri bulunmaktadır. `int` versiyonları, bizim PhoneNumber örneğimizdeki gibi
`short` gibi daha dar `(narrower)` integral type'lar için de kullanılabilir. `double` versiyonları `float` için de
kullanılabilir. Bu, Java'nın tüm numerical primitive type'larını kapsar.

Object referans type'ları için de comparator oluşturma metotları bulunmaktadır. `comparing` adlı static metodun iki
overloading'i vardır. Biri bir key extractor alır ve key'in natural order'ını kullanır. İkincisi hem bir key extractor
hem de extracted key'ler üzerinde kullanılacak bir comparator alır. `thenComparing` adlı instance metodun üç
overloading'i bulunmaktadır. Bir overloading yalnızca bir comparator alır ve bunu ikincil bir order sağlamak için
kullanır. İkinci bir overloading yalnızca bir key extractor alır ve key'in natural order'ını ikincil bir order olarak
kullanır. Son overloading hem bir key extractor hem de extracted key'ler üzerinde kullanılacak bir comparator alır.

Bazen, iki value arasındaki farkın, ilk value ikinciden küçükse negatif, iki value eşitse sıfır ve ilk value daha
büyükse pozitif olduğu gerçeğine dayanan `compareTo` veya `compare` metotları görebilirsiniz. İşte örnek;

```
// BROKEN difference-based comparator - violates transitivity!
static Comparator<Object> hashCodeOrder = new Comparator<>() {
    public int compare(Object o1, Object o2) {
        return o1.hashCode() - o2.hashCode();
    }
};
```

Bu tekniği kullanmayın. Integer overflow ve `IEEE 754` floating-point aritmetiği kusurları nedeniyle tehlikelerle
doludur `[JLS 15.20.1, 15.21.1]`. Ayrıca, ortaya çıkan metotların bu maddede açıklanan tekniklerle yazılanlardan önemli
ölçüde daha hızlı olması olası değildir. Ya da static bir compare metodu kullanın:

```
// Comparator based on static compare method
static Comparator<Object> hashCodeOrder = new Comparator<>() {
    public int compare(Object o1, Object o2) {
        return Integer.compare(o1.hashCode(), o2.hashCode());
    }
};
```

ya da bir comparator construction metodu:

```
// Comparator based on Comparator construction method
static Comparator<Object> hashCodeOrder = Comparator.comparingInt(o -> o.hashCode());
```

> JLS 15.20.1 Numerical Comparison Operators <, <=, >, and >=

Numerical comparison operatorlerinin her bir operandının tipi, primitive bir numeric type'a convertible olmalıdır; aksi
takdirde compile time error oluşur. Operandlar üzerinde binary numeric promotion gerçekleştirilir. Binary numeric
promotion'ın value set'i conversion'ı gerçekleştirdiğini ve `unboxing` conversion'ı da yapabileceğini unutmayın.
Operandların promoted type'ı `int` veya `long` ise, `signed integer` comparison'u gerçekleştirilir. Promoted type
`float` veya `double` ise, `floating-point` comparison gerçekleştirilir.

Floating-point value'ları üzerinde comparison, representing value set'leri ne olursa olsun, doğru şekilde
gerçekleştirilir.

`IEEE 754` standardının spesifikasyonuna göre, floating-point comparison'un sonucu şöyledir:

* Eğer operandlardan herhangi biri `NaN` ise, sonuç `false` olur.

* `NaN` dışındaki tüm value'lar sıralıdır `(ordered)`; negatif infinity tüm finite value'lardan küçüktür ve pozitif
  infinity tüm finite value'lardan büyüktür.

* Pozitif `zero` ve negatif `zero` equal kabul edilir. Örneğin, `-0.0 < 0.0` false, ancak `-0.0 <= 0.0` true’dur. Ancak,
  `Math.min` ve `Math.max` metodlarının negatif sıfırı pozitif sıfırdan kesinlikle daha küçük olarak ele aldığını
  unutmayın.

Floating-point number'lar için bu hususlar dikkate alındığında, `NaN` dışındaki `integer` operandlar veya floating-point
operandlar için aşağıdaki kurallar geçerlidir:

* `<` operatorünün ürettiği değer, sol operandın değeri sağ operandın değerinden küçükse `true`, aksi halde `false’dur.

* `<=` operatorünün ürettiği değer, sol operandın değeri sağ operandın değerine küçük veya eşitse `true`, aksi halde
  `false`’dur.

* `>` operatorünün ürettiği değer, sol operandın değeri sağ operandın değerinden büyükse `true`, aksi halde `false`’dur.

* `>=` operatorünün ürettiği değer, sol operandın değeri sağ operandın değerine büyük veya eşitse `true`, aksi halde
  `false`’dur.

> End of documentation

> JLS 15.21.1 Numerical Equality Operators == and !=

Equality operatörünün operandları her ikisi de numeric type'da ise veya biri numeric type'da olup diğeri numeric type'a
convertible ise, operandlar üzerinde binary numeric promotion gerçekleştirilir. Binary numeric promotion'ın value set'i
conversion'ı gerçekleştirdiğini ve `unboxing` conversion'ı yapabileceğini unutmayın.

Operandların promoted type'ı `int` veya `long` ise, integer equality testi gerçekleştirilir. Promoted type `float` veya
`double` ise, floating-point equality testi gerçekleştirilir.

Floating-point value'ları üzerinde comparison, representing value set'leri ne olursa olsun, doğru şekilde
gerçekleştirilir.

Floating-point equality testi `IEEE 754` standardının kurallarına uygun olarak gerçekleştirilir: Operandlardan herhangi
biri `NaN` ise, `==` sonucu `false`, `!=` sonucu ise true olur. Gerçekten de, `x!=x` testi ancak ve ancak `x`’in değeri
`NaN` ise `true` olur. Bir değerin `NaN` olup olmadığını test etmek için `Float.isNaN` ve `Double.isNaN` metodları da
kullanılabilir.

Pozitif sıfır ve negatif sıfır equal kabul edilir. Örneğin, `-0.0==0.0` true’dur.

Aksi takdirde, iki farklı floating-point value'su equality operatörleri tarafından `unequal` olarak değerlendirilir.

Özellikle, pozitif infinity'i represent eden bir value ve negatif infinity'i represent eden bir value vardır; her biri
yalnızca kendisiyle equal kabul edilir ve diğer tüm value'lar ile `unequal` olarak compare edilir.

Floating-point number'lar için bu hususlar dikkate alındığında, `NaN` dışındaki integer operandları veya floating-point
operandları için aşağıdaki kurallar geçerlidir:

* `==` operatörünün ürettiği değer, sol operandın değeri sağ operandın değerine eşitse `true`, aksi halde `false`’dur.

* `!=` operatörünün ürettiği değer, sol operandın değeri sağ operandın değerine equal değilse `true`, aksi halde `false`
  ’dur.

> End of documentation