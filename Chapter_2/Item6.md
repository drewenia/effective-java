# Avoid creating unnecessary objects

# Gereksiz object’ler create etmekten kaçının.

Genellikle, her seferinde functionally olarak eşdeğer `(equivalent)` yeni bir object yaratmak yerine, tek bir object’i
yeniden kullanmak `(reuse)` daha uygundur. Reuse hem daha hızlı hem de daha şık olabilir. Bir object, immutable ise her
zaman yeniden kullanılabilir.

Yapılmaması gerekenlere extreme bir örnek olarak şu statement’ı düşünün:

```
String s = new String("bikini"); // DON'T DO THIS!
```

Bu statement, her çalıştırıldığında yeni bir String instance’ı oluşturur ve bu object creation'ların hiçbiri gerekli
değildir. String constructor’ının argümanı olan ("bikini") kendisi de bir String instance’ıdır ve constructor tarafından
oluşturulan tüm object’lerle functionally olarak aynıdır. Bu kullanım bir `loop` ya da sıkça invoke edilen bir method’da
gerçekleşiyorsa, gereksiz yere milyonlarca String instance’ı oluşturulabilir.

İyileştirilmiş versiyonu ise şu şekildedir:

```
String s = "bikini";
```

Bu versiyon, her çalıştırıldığında yeni bir tane oluşturmak yerine tek bir String instance kullanır. Ayrıca, aynı string
literal’i içeren ve aynı VM'de çalışan diğer kodlar tarafından da bu object’in yeniden kullanılacağı garanti edilir
`[JLS, 3.10.5]`

Genellikle, hem constructor hem de static factory method sağlayan immutable class’larda, constructor yerine static
factory method kullanarak gereksiz object creating'den kaçınabilirsiniz. Örneğin, Java 9’da deprecated olan
`Boolean(String)` constructor’ı yerine `Boolean.valueOf(String)` factory method’u tercih edilir. Immutable object’lerin
yeniden kullanılmasına ek olarak, değiştirilmeyeceklerinden emin olduğunuz mutable object’leri de yeniden
kullanabilirsiniz.

Bazı object creation’ları diğerlerinden çok daha maliyetlidir. Böyle “pahalı object”lere tekrar tekrar ihtiyacınız
olacaksa, yeniden kullanım için bunları cache’lemek faydalı olabilir. Ne yazık ki, böyle bir object oluştururken her
zaman belirgin değildir. Bir string’in geçerli bir Roma rakamı olup olmadığını belirleyen bir method yazmak istediğinizi
varsayalım. İşte bunu regular expression kullanarak yapmanın en kolay yolu:

```
// Performans büyük ölçüde artırılabilir!
static boolean isRomanNumeral (String str){
    return str.matches("^(?=.)M*(C[MD]|D?C{0,3})"
            + "(X[CL]|L?X{0,3})(I[XV]|V?I{0,3})$");
}
```

Bu implementation'nın sorunu, `String.matches` method’una dayanmasıdır. `String.matches`, bir string’in regular
expression'a uyup uymadığını kontrol etmenin en kolay yolu olsa da, performans kritik durumlarda `repeated` kullanım
için uygun değildir. Sorun, `String.matches` method’unun internally olarak regular expression için bir Pattern
instance’ı oluşturması ve bunu yalnızca bir kez kullanmasıdır; ardından bu instance garbage collection tarafından
toplanmaya uygun hale gelir. Bir Pattern instance oluşturmak pahalıdır çünkü regular expression’ı finite state machine
compiling gerektirir.

Performansı artırmak için, regular expression’ı class initialization sırasında explicitly bir Pattern instance’ına (ki
bu immutable’dır) compile edin, cache’leyin ve `isRomanNumeral` method’unun her call'unda aynı instance’ı yeniden
kullanın:

```
// Reusing expensive object for improved performance
class RomanNumerals {
    private static final Pattern ROMAN = Pattern.compile(
            "^(?=.)M*(C[MD]|D?C{0,3})"
                    + "(X[CL]|L?X{0,3})(I[XV]|V?I{0,3})$");

    static boolean isRomanNumeral(String str){
        return ROMAN.matcher(str).matches();
    }
}
```

`isRomanNumeral` method’unun improved versiyonu, sıkça call edildiğinde önemli performans artışları sağlar. Benim
makinemde, orijinal versiyon 8 karakterlik bir input string için `1.1 µs` sürerken, improved versiyon `0.17 µs`
sürüyor; bu da 6.5 kat daha hızlıdır. Sadece performans artmakla kalmaz, aynı zamanda muhtemelen kodun anlaşılabilirliği
de artar. Aksi halde invisible olan Pattern instance’ı için static final bir filed oluşturmak, ona bir isim vermemizi
sağlar; bu da regular expression’ın kendisinden çok daha okunabilir olur.

Improved `isRomanNumeral` method’unu içeren class initialize edildiğinde, ancak method hiç invoke edilmediğinde, `ROMAN`
field'i gereksiz yere initialize edilmiş olur. `ROMAN` field'inin initializing'ini, `isRomanNumeral` method’unun ilk
çağrıldığında lazy initialization ile engellemek mümkün olabilir, ancak bu önerilmez. Lazy initialization sıkça olduğu
gibi, bu case implementation'ı karmaşıklaştırır ancak ölçülebilir bir performans iyileştirmesi sağlamaz.

Bir object immutable olduğunda, safely yeniden kullanılabileceği açıktır, ancak başka durumlar vardır ki bunlar çok
daha az belli olur, hatta sezgiye aykırı olabilir. `Adapters`, diğer adıyla `views` case'ini düşünelim. Adapter, backing
bir object’e delegates yapan ve alternatif bir interface sağlayan bir object’tir. Adapter’ın, backing object’inden başka
bir state'i olmadığından, belirli bir object için birden fazla adapter instance’ı oluşturulmasına gerek yoktur.

Örneğin, Map interface’inin keySet method’u, Map object’inin tüm key'lerinden oluşan bir `Set view` döner. Safça
düşünüldüğünde, her `keySet` call'unun yeni bir Set instance’ı oluşturması gerektiği düşünülebilir, ancak belirli bir
Map object üzerindeki her `keySet` call'u aynı Set instance’ını döndürebilir. Geri dönen Set instance’ı tipik olarak
`mutable` olsa da, dönen tüm object’ler functionally olarak aynıdır: dönen object’lerden biri değiştiğinde, diğerleri de
değişir çünkü hepsi aynı Map instance’ına bağlıdır. `KeySet view object`’inin birden fazla instance’ını oluşturmak büyük
ölçüde zararsız olsa da gereksizdir ve herhangi bir faydası yoktur.

Gereksiz object oluşturmanın bir diğer yolu ise, programcının primitive ve boxed primitive türlerini mix etmesine izin
veren, gerektiğinde otomatik olarak boxing ve unboxing yapan `autoboxing`’dir. Autoboxing, primitive ve boxed primitive
type'ları arasındaki farkı bulanıklaştırır ama ortadan kaldırmaz. İnce anlamsal farklar ve belirgin performans
farklılıkları vardır. Aşağıdaki metodu düşünün; bu metot, tüm pozitif int değerlerin toplamını hesaplar. Bunu yapmak
için, programın long aritmetiği kullanması gerekir çünkü int, tüm pozitif int değerlerin toplamını tutmak için yeterince
büyük değildir:

```
// Berbat yavaş! Object creation’ını fark edebiliyor musun?
private static long sum() {
    Long sum = 0L;
    for (long i = 0; i < Integer.MAX_VALUE; i++) {
        sum += i;
    }
    return sum;
}
```

Bu program doğru sonucu veriyor, ancak olması gerekenden çok daha yavaş çalışıyor, çünkü tek karakterlik bir yazım
hatası var. `sum` değişkeni `long` yerine `Long` olarak declare edilmiştir, bu da programın yaklaşık 231 gereksiz Long
object'i oluşturması anlamına gelir (long i her Long sum’a eklendiğinde yaklaşık bir tane). `sum` değişkeninin
declaration'ı `Long`’dan `long`’a değiştirilmesi, benim makinemde çalışma süresini 6.3 saniyeden 0.59 saniyeye düşürür.
Ders açıktır: boxed primitive type'lar yerine primitive type'ları tercih edin ve istemeden oluşan `autoboxing`’e dikkat
edin.

```
long sum = 0L;
```

Bu madde, object creation'nın pahalı olduğu ve kaçınılması gerektiği anlamına gelmemelidir. Aksine, constructor'ları çok
az explicit iş yapan küçük object'lerin creation ve geri kazanılması `(reclamation)` ucuzdur, özellikle modern JVM
implementation'larında. Bir programın anlaşılırlığını, basitliğini veya gücünü artırmak için ek object'ler oluşturmak
genellikle iyi bir şeydir.

Tam tersine, object pool maintaining object creation'ı engellemek kötü bir fikirdir, pool'da ki object'ler heavyweight
değilse. Object pool'unu haklı çıkaran klasik örnek, bir database bağlantısıdır. Bağlantı kurulmasının maliyeti o kadar
yüksektir ki, bu object'leri yeniden kullanmak mantıklıdır. Genel olarak ise, kendi object pool'larınızı maintaining
kodunuzu karmaşıklaştırır, memory kullanımını artırır ve performansa zarar verir. Modern JVM implementation'ları,
lightweight object'ler için böyle object pool'larını rahatlıkla geride bırakan son derece optimize edilmiş garbage
collector'larına sahiptir.

Bu maddede belirtilene karşıt görüş, defensive copying konusundadır. Bu maddede, “Var olan bir object'i tekrar kullanmak
yerine yeni bir object yaratmamalısın” denirken, karşıt görüş olarak, “Yeni bir object yaratman gerektiğinde var olanı
tekrar kullanmamalısın” denir. Defensive copying gerektiğinde bir object'i tekrar kullanmanın cezası, gereksiz yere bir
kopya object yaratmanın cezasından çok daha büyüktür. Gerektiği yerlerde defensive copies oluşturmamak sinsi
`(insidious)` bug'lara ve güvenlik açıklarına yol açabilirken, gereksiz yere object oluşturmak yalnızca stil ve
performansı etkiler.