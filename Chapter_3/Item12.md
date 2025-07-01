# Always override toString

Object sınıfı bir toString metodu implementation'ı sağlasa da, döndürdüğü String genellikle sınıfınızın kullanıcısının
görmek istediği şey değildir. Bu, sınıf adından sonra bir `@` işareti ve hash code'unun unsigned hexadecimal
gösteriminden oluşur, örneğin: `PhoneNumber@163b91`. toString için general contract, döndürülen string'in "bir kişinin
okuması kolay, özlü ama bilgilendirici bir represent" olması gerektiğini belirtir. `PhoneNumber@163b91`'in özlü ve
okunması kolay olduğu iddia edilse de, `707-867-5309` ile karşılaştırıldığında pek bilgilendirici değildir. toString
contract'ı devamında şöyle der: "Tüm subclass'ların bu metodu override etmesi önerilir." Gerçekten de iyi bir tavsiye!

equals ve hashCode contract'larına uymak kadar kritik olmasa da, iyi bir `toString` implementation'ı sağlamak,
sınıfınızı kullanmayı çok daha keyifli hale getirir ve sınıfı kullanan sistemlerin debugging'ini kolaylaştırır. toString
metodu, bir object `println`'e, `printf`'e, `string concatenation` operatörüne veya `assert`'e geçirildiğinde ya da bir
debugger tarafından yazdırıldığında otomatik olarak invoke edilir. Siz hiçbir zaman bir object üzerinde toString
metodunu call etmeseniz bile, başkaları call edebilir. Örneğin, object'inize referansı olan bir component, object'in
string representation'ınını loglanmış bir error mesajına dahil edebilir. Eğer toString metodunu override etmezseniz,
mesaj neredeyse işe yaramaz olabilir.

Eğer PhoneNumber için iyi bir toString metodu sağladıysanız, faydalı bir diagnostic mesajı oluşturmak bu kadar kolaydır:

```
System.out.println("Failed to connect to " + phoneNumber);
```

Programcılar, toString metodunu override etseniz de bu şekilde diagnostic mesajları üreteceklerdir; ancak siz bunu
yapmadığınız sürece mesajlar faydalı olmayacaktır. İyi bir toString metodu sağlamanın faydaları, sınıf instance'larının
ötesine, özellikle collection'lar olmak üzere bu instance'lara referans içeren object'lere de uzanır.

Bir map print ederken hangisini görmeyi tercih edersiniz: `{Jenny=PhoneNumber@163b91}` mi yoksa `{Jenny=707-867-5309}`
Mümkün olduğunda, toString metodu, telefon numarası örneğinde gösterildiği gibi, object'de bulunan tüm ilginç bilgileri
döndürmelidir. Object büyükse veya string representation'a elverişli olmayan state içeriyorsa bu pratik değildir. Bu
gibi durumlarda, toString metodu `Manhattan konut telefon rehberi (1487536 kayıt)` veya `Thread[main,5,main]` gibi bir
özet döndürmelidir. İdeal olarak, string kendi kendini açıklayıcı olmalıdır. `(Thread örneği bu testi geçemiyor.)`.
Bir object'in string representation'da tüm ilginç bilgilerini dahil etmemenin özellikle sinir bozucu bir bedeli, şöyle
görünen test failure raporlarıdır:

```
Assertion failure: expected {abc, 123}, but was {abc, 123}.
```

Bir toString metodu implement ederken vermeniz gereken önemli bir karar, döndürülen değerin formatını dokümantasyonda
belirtip belirtmeyeceğinizdir. Bunu telefon numarası veya matrix gibi value class'ları için yapmanız önerilir. Formatı
belirtmenin avantajı, object'in standart, net, insan tarafından okunabilir bir representation sağlamasıdır. Bu
representation, input ve output için ve CSV dosyaları gibi kalıcı `(persistent)`, insan tarafından okunabilir data
object'lerinde kullanılabilir. Formatı belirtirseniz, programcıların object ile string representation'ı arasında kolayca
gidip gelebilmesi için matching bir static factory veya constructor sağlamak genellikle iyi bir fikirdir. Bu yaklaşım,
Java platform library'lerinde ki `BigInteger`, `BigDecimal` ve boxed primitive sınıfların çoğu dahil olmak üzere birçok
value class tarafından benimsenmiştir.

toString dönüş değerinin formatını belirtmenin dezavantajı, bir kez belirledikten sonra, sınıfınız yaygın olarak
kullanılıyorsa, ömür boyu ona bağlı kalmanızdır. Programcılar, representation'ı ayrıştırmak, oluşturmak ve kalıcı
data'lara yerleştirmek için kod yazacaklardır. Gelecekteki bir sürümde representation'ı değiştirirseniz, onların kodunu
ve data'larını bozarsınız ve onlar da feryat ederler. Bir format belirtmemeyi seçerek, sonraki bir sürümde bilgi ekleme
veya formatı iyileştirme esnekliğini korursunuz.

Formatı belirtmeye karar verseniz de vermeseniz de, niyetinizi açıkça belgelemeniz gerekir. Formatı belirtirseniz, bunu
kesin bir şekilde yapmalısınız. Örneğin, PhoneNumber sınıfına uyan bir toString metodu aşağıdadır:

```
/**
 * Returns the string representation of this phone number.
 * The string consists of twelve characters whose format is
 * "XXX-YYY-ZZZZ"
 * , where XXX is the area code, YYY is the
 * prefix, and ZZZZ is the line number. Each of the capital
 * letters represents a single decimal digit.
 * *
 * * If any of the three parts of this phone number is too small
 * * to fill up its field, the field is padded with leading zeros.
 * * For example, if the value of the line number is 123, the last
 * * four characters of the string representation will be "0123"
 */

@Override
public String toString() {
    return String.format("%03d-%03d-%04d", areaCode, prefix, lineNum);
}
```

Bir format belirtmemeye karar verirseniz, dokümantasyon comment'i şöyle bir şey okumalıdır:

```
/**
* Returns a brief description of this potion. The exact details
* of the representation are unspecified and subject to change,
* but the following may be regarded as typical:
*
* "[Potion #9: type=love, smell=turpentine, look=india ink]"
*/

@Override 
public String toString() { 
    ... 
}
```

Bu comment'i okuduktan sonra, formatın detaylarına bağlı kod veya kalıcı data üreten programcılar, format değiştiğinde
kendilerinden başka kimseyi suçlayamayacaklardır.

Formatı belirtin veya belirtmeyin, toString tarafından döndürülen değerde bulunan bilgilere programatik erişim sağlayın.
Örneğin, PhoneNumber sınıfı `areacode`, `prefix` ve `linenumber` için accessor metotları içermelidir. Bunu yapmazsanız,
bu bilgilere ihtiyaç duyan programcıları string'i parse etmeye zorlarsınız. Performansı düşürmek ve programcılar için
gereksiz iş çıkarmakla birlikte, bu süreç hataya açıktır ve formatı değiştirmeniz durumunda bozulan hassas sistemlerle
sonuçlanır. Accessor metotları sağlamayarak, string formatını, değişime tabi olduğunu belirtmiş olsanız bile, fiili bir
API'ye dönüştürmüş olursunuz.

Static bir utility sınıfta toString metodu yazmanın bir anlamı yoktur. Çoğu enum type'ında toString metodu
yazmamalısınız, çünkü Java sizin için zaten mükemmel bir tane sağlar. Ancak, subclass'ları ortak bir string
representation'ınını paylaşan herhangi bir abstract sınıfta bir toString metodu yazmalısınız. Örneğin, çoğu collection
implementation'ınında ki toString metotları, abstract collection sınıflarından inherit alınır.

Google'ın open source AutoValue tool'u, çoğu IDE gibi sizin için bir toString metodu oluşturacaktır. Bu metotlar her bir
field'in content'ini size söylemek için harikadır ancak sınıfın anlamına özel değildir. Yani, örneğin, PhoneNumber
sınıfımız için otomatik olarak oluşturulmuş bir toString metodu kullanmak uygunsuz olurdu (telefon numaralarının
standart bir string representation'ı olduğundan), ancak Potion sınıfımız için kesinlikle kabul edilebilir olurdu.
Bununla birlikte, otomatik olarak oluşturulan bir toString metodu, bir object'in value'su hakkında size hiçbir şey
söylemeyen, Object sınıfından inherit alınan metoda göre çok daha tercih edilebilir.

Özetle, yazdığınız her instantiable sınıfta Object'in toString implementation'ınını override edin, ta ki bir superclass
bunu zaten yapmamış olsun. Sınıfları kullanmayı çok daha keyifli hale getirir ve debugging'e yardımcı olur. toString
metodu, object'in özlü, kullanışlı ve estetik açıdan hoş bir formatta açıklamasını döndürmelidir.