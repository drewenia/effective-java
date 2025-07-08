# Minimize mutability

Immutable class, instance'larının değiştirilemediği bir class'tır. Her instance'daki tüm bilgiler, object'in lifecycle'ı
boyunca sabittir, bu yüzden herhangi bir değişiklik gözlemlenemez. Java platform library'leri, String, boxed primitive
class'lar ve BigInteger ile BigDecimal dahil olmak üzere birçok immutable class içerir. Bunun için birçok iyi neden
vardır: Immutable class'lar, mutable class'lara göre tasarlanması, implement edilmesi ve kullanılması daha kolaydır.
Hatalara daha az yatkındırlar ve daha güvenlidirler.

Bir class'ı immutable yapmak için şu beş kurala uy:

1 - Object'in state'ini değiştiren method'lar `(mutator olarak bilinen)` sağlamayın.

2 - Class'ın extend edilemediğinden emin olun. Bu, dikkatsiz veya kötü niyetli subclass'ların object'in state'ini
değişmiş gibi davranarak class'ın immutable behavior'unu bozmasını engeller. Subclass oluşturmayı engellemek genellikle
class'ı final yaparak gerçekleştirilir, ancak daha sonra tartışacağımız bir alternatif vardır.

3 - Tüm field'ları final yap. Bu, niyetini sistem tarafından zorunlu kılınan bir şekilde açıkça ifade eder. Ayrıca, yeni
oluşturulmuş bir instance'a ait bir reference, synchronization olmadan bir thread'den başka bir thread'e aktarılırsa
doğru davranışı sağlamak için de gereklidir; bu, memory model'de açıkça belirtilmiştir `[JLS, 17.5]`.

4 - Tüm field'leri private yap. Bu, client'ların field'lar tarafından referans verilen mutable object'lere erişmesini ve
bu object'leri directly değiştirmesini engeller. Immutable class'ların primitive değerler veya immutable object'lere
referanslar içeren `public final field`'lara sahip olması teknik olarak izin verilen bir durum olsa da, önerilmez çünkü
bu, ileriki bir sürümde internal representation'ı değiştirmeyi engeller.

5 - Herhangi bir mutable component'a özel `(exclusive)` erişim `(access)` sağla. Class'ınızın mutable object'lere
referans veren herhangi bir field'ı varsa, class'ın client'larının bu object'lere referans elde edemeyeceğinden emin
olun. Böyle bir field'ı asla client tarafından sağlanan bir object reference'ı ile initialize etme veya bu field'ı bir
accessor'dan döndürme. Constructor'larda, accessor'larda ve `readObject` method'larında defensive copy'ler oluştur.

> JLS 17.5 final Field Semantics

Final olarak declare edilen field'lar bir kez initialize edilir, ancak normal koşullar altında hiç değiştirilmez. Final
field'ların ayrıntılı semantiği, normal field'lardan biraz farklıdır. Özellikle, compiler'lar final field read'lerini
synchronization bariyerleri ve rastgele `(arbitrary)` ya da bilinmeyen `(unknown)` method call'ları arasında hareket
ettirme konusunda geniş bir özgürlüğe sahiptir. Buna bağlı olarak, compiler'ların final bir field'in değerini bir
register'da cache'de tutmasına ve `non-final` bir field'in reload edilmesi gereken durumlarda memory'den yeniden
yüklememesine izin verilir.

Final field'lar, programcıların synchronization olmadan thread-safe immutable object'ler oluşturmasına da olanak tanır.
Thread-safe immutable object, immutable object'a referansların thread'ler arasında data race ile geçirildiği durumlarda
bile tüm thread'ler tarafından immutable olarak görülür. Bu, yanlış veya kötü niyetli kod tarafından immutable class'ın
kötüye kullanımına karşı güvenlik garantileri sağlayabilir. Immutable garantisi sağlamak için final field'lar doğru
şekilde kullanılmalıdır.

Bir object, constructor'ı tamamlandığında tamamen initialize edilmiş kabul edilir. Bir thread, yalnızca object tamamen
initialize edildikten sonra o object'e ait bir referansı görebiliyorsa, bu thread object'in final field'larının doğru
şekilde initialize edilmiş değerlerini göreceği garantilenir.

Final field'ların kullanım modeli basittir: Object'in final field'larını, o object'in constructor'ında set et; ve
object'in constructor'ı bitmeden, constructed object'e ait referansı başka bir thread'in görebileceği bir yere yazma.
Buna uyulursa, Object başka bir thread tarafından görüldüğünde, o thread her zaman object'in final field'larının doğru
şekilde constructed halini görecektir. Ayrıca, bu final field'lar tarafından referans verilen herhangi bir object veya
array'in, final field'lar kadar güncel olan sürümlerini de görecektir.

final Fields In The Java Memory Model;

```
public class FinalFieldExample {
    final int x;
    int y;
    static FinalFieldExample f;

    public FinalFieldExample() {
        this.x = 3;
        this.y = 4;
    }

    static void writer() {
        f = new FinalFieldExample();
    }

    static void reader() {
        if (f != null) {
            int i = f.x; // 3 ü görmek garanti
            int j = f.y; // 0'ı görebiliyordu.
        }
    }
}
```

FinalFieldExample class'ının final int field'ı `x` ve non-final int field'ı `y` vardır. Bir thread writer method'unu
çalıştırabilir, başka bir thread ise reader method'unu çalıştırabilir. Writer method'u object'in constructor'ı bittikten
sonra `f`'yi yazdığı için, reader method'u `f.x`'in doğru şekilde initialize edilmiş değerini görmesi garanti edilir.
`3` değerini okuyacaktır. Ancak, `f.y` final değildir; bu nedenle reader method'u onun için `4` değerini görmesi garanti
edilmez.

> End of documentation

Önceki maddelerdeki birçok örnek class immutable'dır. Böyle bir class, her attribute için accessor'lara sahip ancak
karşılık gelen mutator'ları olmayan PhoneNumber'dır. İşte biraz daha complex bir örnek:

```
// Immutable complex number class
public final class Complex {
    private final double real;
    private final double imaginary;

    public Complex(double real, double imaginary) {
        this.real = real;
        this.imaginary = imaginary;
    }

    public double realPart() {
        return real;
    }

    public double imaginaryPart() {
        return imaginary;
    }

    public Complex minus(Complex c) {
        return new Complex(real - c.real, imaginary - c.imaginary);
    }

    public Complex times(Complex c) {
        return new Complex(real * c.real - imaginary * c.imaginary,
                real * c.imaginary + imaginary * c.real);
    }

    public Complex dividedBy(Complex c) {
        double tmp = c.real * c.real + c.imaginary * c.imaginary;
        return new Complex((real * c.real + imaginary * c.imaginary) / tmp,
                (imaginary * c.real - real * c.imaginary) / tmp);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Complex c))
            return false;
        return Double.compare(c.real, real) == 0
                && Double.compare(c.imaginary, imaginary) == 0;
    }

    @Override
    public int hashCode() {
        return 31 * Double.hashCode(real) + Double.hashCode(imaginary);
    }

    @Override
    public String toString() {
        return "(" + real + " + " + imaginary + "i)";
    }
}
```

Bu class, complex bir number'ı (real ve imaginary parçalara sahip bir number) represent eder. Standart Object
method'larına ek olarak, real ve imaginary parçalar için accessor'lar sağlar ve dört temel aritmetik operation'ı sunar:
addition, subtraction, multiplication, ve division. Aritmetik operation'ların, bu instance'ı değiştirmek yerine yeni bir
Complex instance'ı oluşturup döndürdüğüne dikkat et. Bu pattern, functional yaklaşım olarak bilinir çünkü method'lar,
operand'a bir function uygulanmasının sonucunu döndürür, onu değiştirmez. Bu yaklaşımı, method'ların operand'a bir
procedure uygulayarak onun state'ini değiştirdiği procedural ya da imperative yaklaşımla karşılaştır. Method
isimlerinin (add gibi fiiller yerine) preposition'lar (plus gibi) olduğuna dikkat et. Bu, metotların object'lerin
değerlerini değiştirmediği gerçeğini vurgular. BigInteger ve BigDecimal class'ları bu isimlendirme kuralına uymadı ve bu
durum birçok kullanım hatasına yol açtı.

Functional yaklaşım, eğer ona aşina değilsen doğal görünmeyebilir, ancak birçok avantaja sahip olan immutability'yi
mümkün kılar. Immutable object'ler basittir. Immutable bir object tam olarak bir state'de olabilir, o da yaratıldığı
state'dir. Tüm constructor'ların class değişmezlerini `(invariant)` sağladığından emin olursan, bu değişmezlerin
`(invariant)` sonsuza kadar `true` kalacağı garanti edilir; bu senin veya class'ı kullanan programcının ekstra çaba
göstermesine gerek kalmaz. Mutable object'ler ise, keyfi olarak complex state space'lerine sahip olabilirler. Eğer
dokümantasyon, mutator method'ların gerçekleştirdiği state transition'larını tam olarak açıklamıyorsa, mutable bir
class'ı güvenilir şekilde kullanmak zor veya imkansız olabilir.

Immutable object'ler doğal olarak `(inherently)` `thread-safe`'dir; synchronization'a ihtiyaç duymazlar. Multiple thread
concurrently erişse bile bozulamazlar. Bu, thread safety'i sağlamak için kesinlikle en kolay yaklaşımdır. Hiçbir thread,
immutable bir object üzerinde başka bir thread'in etkisini göremediği için, immutable objeler serbestçe paylaşılabilir.
Bu nedenle immutable class'lar, client'ların mevcut instance'ları mümkün olduğunca reuse etmesini teşvik etmelidir.
Bunu yapmanın kolay yollarından biri, sık kullanılan value'lar için `public static final` constant'lar sağlamaktır.
Örneğin, Complex class şu constant'ları sağlayabilir:

```
public static final Complex ZERO = new Complex(0, 0);
public static final Complex ONE = new Complex(1, 0);
public static final Complex I = new Complex(0, 1);
```

Bu yaklaşım bir adım daha ileri götürülebilir. Immutable bir class, yeni instance'lar oluşturmak yerine mevcut olanların
kullanılmasını sağlamak için sık talep edilen instance'ları cache'e alan `static factory method`'lar sağlayabilir. Tüm
`boxed primitive class`'lar ve `BigInteger` bunu yapar. Bu tür static factory'lerin kullanılması, client'ların yeni
instance'lar oluşturmak yerine instance'ları paylaşmasını sağlar, böylece memory kullanımı ve garbage collection
maliyetleri azalır. Yeni bir class tasarlarken `public constructor` yerine `static factory` kullanmayı tercih etmek,
client'ları değiştirmeden daha sonra `caching` ekleme esnekliği sağlar.

Immutable object'lerin serbestçe paylaşılabilmesinin bir sonucu olarak, onları defensive copies yapmana hiç gerek
kalmaz. Aslında, kopyalarını hiç yapmana gerek yoktur çünkü kopyalar orijinalleriyle sonsuza dek eşdeğer olacaktır. Bu
yüzden, immutable bir class'ta clone method'u veya copy constructor sağlamana gerek yoktur ve sağlamamalısın. Bu, Java
platformunun ilk dönemlerinde iyi anlaşılmamıştı, bu yüzden String class'ın bir copy constructor'ı vardır, ancak
nadiren, hatta hiç kullanılmamalıdır.

Sadece immutable object'leri paylaşmakla kalmazsın, aynı zamanda internals'larını da paylaşabilirler. Örneğin,
`BigInteger` class'ı internally olarak işaret-büyüklük `(sign-magnitude)` representation'ı kullanır. İşaret `(sign)` bir
`int` ile, büyüklük `(magnitude)` ise bir `int array` ile represent edilir. Negate method'u, benzer büyüklükte
`(magnitude)` ve zıt işarette `(sign)` yeni bir `BigInteger` oluşturur. Array mutable olmasına rağmen, copy'e gerek
yoktur; yeni oluşturulan `BigInteger`, orijinalle aynı internal array'e işaret eder.

Immutable object'ler, mutable veya immutable olsun, diğer object'ler için mükemmel building block'larıdır. Component
object'lerin altında değişmeyeceğini bildiğinde, complex bir object'in değişmezlerini `(invariant)` korumak çok daha
kolaydır. Bu ilkenin special bir case'i olarak, immutable object'ler mükemmel `map` key'leri ve `set` element'leri olur:
Bir kez `map` veya `set`'e eklendikten sonra değerlerinin değişmesi konusunda endişelenmene gerek yoktur; bu durum map
veya set'in değişmezlerini `(invariant)` bozabilir.

Immutable object'ler free olarak failure atomicity sağlar. State'leri asla değişmez, bu yüzden geçici bir tutarsızlık
olasılığı yoktur.

Immutable class'ların en büyük dezavantajı, her farklı `(distinct)` value için seperate bir object gerektirmeleridir.
Bu object'leri oluşturmak maliyetli olabilir, özellikle de büyüklerse. Örneğin, bir milyon bitlik bir BigInteger'a sahip
olduğunu ve onun low-order bitini değiştirmek istediğini varsayalım:

```
BigInteger moby = ...;
moby = moby.flipBit(0);
```

FlipBit method'u, orijinalden yalnızca bir bit farklı olan, yine bir milyon bit uzunluğunda yeni bir BigInteger
instance'ı oluşturur. Bu işlem, BigInteger'ın boyutuyla orantılı time ve space gerektirir. Bunu `java.util.BitSet` ile
karşılaştır. BigInteger gibi, BitSet de keyfi uzunlukta bir bit sequence'ini represent eder, ancak BigInteger'ın aksine
BitSet mutable'dır. BitSet class'ı, milyon bitlik bir instance'ın tek bir bitinin state'ini constant time'da
değiştirmeye olanak tanıyan bir method sağlar:

```
BitSet moby = ...;
moby.flip(0);
```

Performans sorunu, her adımda yeni bir object oluşturan, sonunda final result dışındaki tüm object'leri discard ederek
multistep bir operation gerçekleştirirseniz büyür. Bu sorunla başa çıkmak için iki yaklaşım vardır. İlki, hangi
multistep operation'ların yaygın olarak gerekeceğini tahmin etmek ve bunları primitive olarak sağlamaktır. Eğer
multistep bir operation primitive olarak sağlanırsa, immutable sınıfın her adımda ayrı bir object oluşturmasına gerek
kalmaz. Internally, immutable class keyfi derecede akıllı olabilir. Örneğin, BigInteger'ın, modular üs alma gibi
multistep operation'ları hızlandırmak için kullandığı `package-private` mutable bir "companion class'ı" vardır. Daha
önce belirtilen tüm nedenlerden dolayı, mutable companion class'ı kullanmak, BigInteger'ı kullanmaktan çok daha zordur.
Neyse ki, onu kullanmak zorunda değilsiniz: BigInteger'ı implementor'lar sizin için zor işi yaptı.

Package-private mutable companion class yaklaşımı, client'ların immutable sınıfınız üzerinde hangi complex
operation'ları gerçekleştirmek isteyeceğini doğru bir şekilde tahmin edebiliyorsanız gayet iyi çalışır. Eğer tahmin
edemiyorsanız, en iyi seçeneğiniz public mutable companion class sağlamaktır. Java platform library'lerinde bu
yaklaşımın ana örneği String sınıfıdır; onun mutable companion'ı `StringBuilder`'dır (ve eskide kalmış öncüsü
`StringBuffer`'dır).

Artık immutable bir sınıfın nasıl oluşturulacağını ve immutability'nin artılarını ve eksilerini bildiğinize göre, birkaç
tasarım alternatifini tartışalım. Immutability'yi garanti etmek için, bir sınıfın kendisinin subclass'lara izin
vermemesi gerektiğini hatırlayın. Bu, sınıfı `final` yaparak yapılabilir, ancak başka, daha esnek bir alternatif daha
vardır. Immutable bir sınıfı final yapmak yerine, tüm constructor'larını `private` veya `package-private` yapabilir
ve `public constructor`'lar yerine `public static factory` metotları ekleyebilirsiniz. Bunu concrete etmek gerekirse, bu
yaklaşımı benimsediğinizde `Complex` şöyle görünecektir:

```
// Immutable class with static factories instead of constructors
public final class Complex {
    private final double real;
    private final double imaginary;

    private Complex(double real, double imaginary) {
        this.real = real;
        this.imaginary = imaginary;
    }

    public static Complex valueOf(double real, double imaginary){
        return new Complex(real,imaginary);
    }

    ... // Remainder unchanged
}
```

Bu yaklaşım genellikle en iyi alternatiftir. En esneğidir, çünkü birden fazla `package-private` implementation sınıfının
kullanılmasına izin verir. Kendi package'i dışında bulunan client'ları için immutable sınıf, effectively finaldir
çünkü başka bir package'den gelen ve `public` veya `protected` bir constructor'ı olmayan bir sınıfı extend etmek
imkansızdır. Multiple implementation sınıfının esnekliğini sağlamanın yanı sıra, bu yaklaşım, static factory
metotlarının object caching yeteneklerini geliştirerek, sonraki sürümlerde sınıfın performansını ayarlamayı mümkün
kılar.

`BigInteger` ve `BigDecimal` yazıldığında, immutable sınıfların effectively final olması gerektiği geniş çapta
anlaşılmamıştı, bu yüzden tüm metotları override edilebilir. Ne yazık ki, backward compatibility korunurken bu durum
sonradan düzeltilemedi. Eğer güvenilmeyen bir client'dan gelen `BigInteger` veya `BigDecimal` argümanının
immutability'sine dayalı bir sınıf yazıyorsanız, bu argümanın güvenilmeyen bir subclass'ın instance'ı değil, "real" bir
`BigInteger` veya `BigDecimal` olup olmadığını kontrol etmelisiniz. Eğer durum ikincisiyse, mutable olabileceği
varsayımıyla onu defensively olarak kopyalamanız gerekir.

```
public static BigInteger safeInstance(BigInteger val){
    return val.getClass() == BigInteger.class ? val : new BigInteger(val.toByteArray());
}
```

Bu maddenin başındaki immutable class kuralları listesi, hiçbir method'un object'i değiştirmemesi ve tüm field'ların
final olması gerektiğini söyler. Aslında bu kurallar gereğinden biraz daha katıdır ve performansı artırmak için
gevşetilebilir. Gerçekte, hiçbir method object'in state'inde externally visible bir değişiklik üretmemelidir. Ancak,
bazı immutable class'lar, ihtiyaç duyulduklarında pahalı computation'ların sonuçlarını ilk seferinde cache'e almak için
bir veya daha fazla `non-final field`'a sahiptir. Aynı value tekrar istenirse, cached value döndürülür ve yeniden
computation maliyeti tasarruf edilir. Bu yöntem tam olarak object immutable olduğu için işe yarar; bu da
computation'ının tekrarlanması durumunda aynı sonucu vereceğini garanti eder.

Örneğin, PhoneNumber class'ının `hashCode` method'u, ilk kez invoke edildiğinde hash code'unu hesaplar ve tekrar
invoke edilmesi durumunda kullanmak üzere cache'e alır. Bu teknik, lazy initialization'ın bir örneğidir ve String
tarafından da kullanılır.

Serializability konusunda bir uyarı eklenmelidir. Eğer immutable sınıfınızın Serializable interface'ini implement
etmesini seçerseniz ve bu sınıf mutable object'lere referans veren bir veya daha fazla field içeriyorsa, default
serialized form kabul edilebilir olsa bile, açık bir `readObject` veya `readResolve` metodu sağlamalı ya da
`ObjectOutputStream.writeUnshared` ve `ObjectInputStream.readUnshared` metotlarını kullanmalısınız. Aksi takdirde, bir
saldırgan class'ınızın mutable bir instance'ını oluşturabilir.

Özetle, her getter için bir setter yazma isteğine diren. Sınıflar, mutable olmaları için çok iyi bir neden olmadıkça
immutable olmalıdır. Immutable sınıflar birçok avantaj sağlar ve tek dezavantajları belirli koşullar altında performans
sorunları potansiyelidir. PhoneNumber ve Complex gibi small value object'lerini her zaman immutable yapmalısınız. Java
platform library'lerinde `java.util.Date` ve `java.awt.Point` gibi immutable olması gerekirken olmayan birkaç sınıf
vardır. String ve BigInteger gibi daha büyük value object'lerini de immutable yapmayı ciddiye almalısınız. Immutable
sınıfınız için public mutable companion class sağlamalısınız, ancak bunu ancak tatmin edici performans elde etmek için
gerekli olduğunu onayladıktan sonra yapmalısınız.

Bazı sınıflar için immutability pratik değildir. Eğer bir sınıf immutable yapılamıyorsa, mutable'lığını olabildiğince
sınırlayın. Bir object'in bulunabileceği state sayısını azaltmak, o object hakkında akıl yürütmeyi kolaylaştırır ve hata
olasılığını düşürür. Bu nedenle, her field'i final yapın, aksi takdirde `non-final` yapmak için zorlayıcı bir neden
yoksa. Aksi yönde iyi bir neden olmadıkça, her field'i `private final` olarak bildirmek olmalıdır.

Constructor'lar, tüm değişmezleri `(invariants)` oluşturulmuş, fully initialized object'ler yaratmalıdır.
Constructor'dan veya static factory metodundan ayrı bir public initialization metodu sağlamayın, aksi takdirde bunu
yapmak için zorlayıcı bir neden olmadıkça. Benzer şekilde, bir object'in farklı bir initial state'i ile oluşturulmuş
gibi yeniden kullanılmasına olanak tanıyan bir "reinitialize" metodu sağlamayın. Bu tür metotlar, artan complexity
pahasına, genellikle çok az performans faydası sağlar veya hiç sağlamaz.

CountDownLatch sınıfı bu ilkeleri örneklendirir. O mutable'dır, ancak state space kasıtlı olarak küçük tutulmuştur. Bir
instance oluşturursunuz, onu bir kez kullanırsınız ve işi biter: CountDownLatch'ın counter'ı sıfıra ulaştığında, onu
yeniden kullanamazsınız.

Bu maddedeki Complex sınıfı ile ilgili son bir not eklenmelidir. Bu örnek yalnızca immutability'yi göstermek amacıyla
verilmiştir. Bu, endüstriyel güçte `(industrial-strength)` bir karmaşık sayı `(complex number)` implementation'ı
değildir.