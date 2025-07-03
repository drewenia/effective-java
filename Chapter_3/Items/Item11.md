# Always override hashCode when you override equals

equals metodunu override eden her sınıfta hashCode metodunu da override etmelisiniz. Bunu yapmazsanız, sınıfınız
hashCode için general contract'ı ihlal eder ve bu da `HashMap` ve `HashSet` gibi Collection'lar da düzgün çalışmasını
engeller. İşte, Object spesifikasyonundan uyarlanmış contract:

* Bir uygulamanın execution'ı sırasında bir object üzerinde hashCode metodu tekrar tekrar invoke edildiğinde, equals
  comparison'larında kullanılan herhangi bir bilgi değiştirilmediği sürece, her seferinde aynı value'yu döndürmelidir.
  Bu value, bir uygulamanın bir execution'ınından diğerine tutarlı `(consistent)` olmak zorunda değildir.

* Eğer iki object `equals(Object)` metoduna göre equal ise, bu iki object üzerinde `hashCode` metodunu calling aynı
  integer result'ını produce etmelidir.

* Eğer iki object `equals(Object)` metoduna göre `unequal` ise, her iki object üzerinde hashCode metodunu calling
  farklı result'lar produce etmesi gerekmez. Ancak, `unequal` object'ler için farklı result'lar produce etmenin hash
  table'larının performansını artırabileceğini programcı bilmelidir.

`hashCode` metodunu override etmediğinizde ihlal edilen temel hüküm ikinci olanıdır: equal object'lerin hashCode
value'ları da equal olmalıdır. İki ayrı instance, bir sınıfın `equals` metoduna göre logically olarak equal olabilir;
ancak Object’in hashCode metoduna göre, onlar sadece pek ortak noktası olmayan iki object'dir. Bu nedenle, Object’in
hashCode metodu, contract'ın gerektirdiği gibi iki equal number yerine, random görünen iki farklı number döndürür.

Örneğin, PhoneNumber sınıfının instance'larını bir HashMap’te Key olarak kullanmaya çalıştığınızı varsayalım:

```
Map<PhoneNumber,String> map = new HashMap<>();
map.put(new PhoneNumber(707, 867, 5309), "Jenny");
```

Bu noktada, `m.get(new PhoneNumber(707, 867, 5309))` ifadesinin "Jenny" döndürmesini bekleyebilirsiniz, ancak bunun
yerine `null` döner. İki PhoneNumber instance'ının dahil olduğunu fark edin: Biri HashMap’e insertion için
kullanılırken, ikincisi equal bir instance olarak (erişim denemesi için `(attempted)`) kullanılır. PhoneNumber sınıfının
hashCode metodunu override etmemesi, iki equal instance'ın farklı hash code'larına sahip olmasına yol açar ve bu durum
hashCode contract'ının ihlalidir. Bu nedenle, `get` metodu, `put` metoduyla store edilen telefon numarasını farklı bir
hash bucket’ta arama ihtimali yüksektir. İki instance aynı bucket’a hash’lense bile, `get` metodu neredeyse kesinlikle
`null` döndürür; çünkü HashMap, her `entry` ile ilişkili hash code'unu cache'e alan bir optimizasyona sahiptir ve hash
code'ları match olmuyorsa object equality'i kontrol etmez.

Bu sorunu çözmek, PhoneNumber için doğru bir hashCode metodu yazmak kadar basittir. Peki, bir hashCode metodu nasıl
olmalıdır? Kötü bir hashCode metodu yazmak çok kolaydır. Örneğin, bu her zaman legal olsa da asla kullanılmamalıdır:

```
// The worst possible legal hashCode implementation - never use!
@Override 
public int hashCode() { 
    return 42; 
}
```

Bu legal çünkü equal object'lerin aynı hash code'una sahip olmasını garanti eder. Kötüdür çünkü tüm object'lerin aynı
hash code'una sahip olmasını sağlar. Böylece tüm object'ler aynı bucket’a hash’lenir ve hash table'ları linked listelere
dönüşür. Lineer zamanda çalışması gereken programlar, quadratic zamanda çalışır hale gelir. Büyük hash table'ları için
bu, çalışmak ile çalışmamak arasındaki farktır.

İyi bir hash function, `unequal` instance'lar için farklı hash code'ları produce etme eğilimindedir. Bu, hashCode
contract'ının üçüncü maddesinde kastedilen şeydir. İdeal olarak, bir hash function'ı makul bir `unequal` instance
Collection'ınını tüm int value'ları arasında eşit dağıtmalıdır. Bu idealin gerçekleştirilmesi zor olabilir. Neyse ki,
makul bir yaklaşımı sağlamak çok da zor değildir. İşte basit bir yöntem:

1 - `result` adında bir `int` variable'ı declare edin ve object'inizde ki ilk anlamlı field için adım `2.a`’da
compute edilen hash code'u `c` ile başlatın. (Anlamlı field, equals comparison'larını etkileyen field’dır.)

2 - Object'inizde ki kalan her anlamlı field `f` için aşağıdakileri yapın:

a. Field için bir int hash code'u `c` compute edin:

    i. Eğer field bir primitive type ise, f’nin type'ına karşılık gelen boxed primitive sınıfının Type.hashCode(f) 
    metodunu kullanarak hesaplayın.

    ii. Field bir object referansı ise ve bu sınıfın equals metodu field equals’u recursively invoke ederek 
    compare ediyorsa, field üzerinde recursively olarak hashCode metodunu çağırın. Daha complex bir comparison 
    gerekiyorsa, bu field için bir “canonical representation” compute edin ve hashCode metodunu canonical 
    represent üzerinde invoke edin. Field’ın değeri null ise 0 kullanın (veya başka bir constant, ancak 0 
    gelenekseldir).

    iii. Field bir array ise, her anlamlı element'i ayrı bir field gibi ele alın. Yani, bu kuralları recursively 
    uygulayarak her anlamlı element için bir hash code'u compute edin ve value'ları adım 2.b’ye göre combine edin.
    Array'de anlamlı eleman yoksa, tercihen 0 olmayan bir constant kullanın. Tüm element'ler anlamlı ise 
    Arrays.hashCode kullanın.

b. Step `2.a`’da compute edilen hash code'u `c`’yi `result` ile şu şekilde combine edin: `result = 31 * result + c;`

3 - Return result

hashCode metodunu yazmayı bitirdiğinizde, equal instance'ların equal hash code'larına sahip olup olmadığını kendinize
sorun. Sezgilerinizi doğrulamak için unit testleri yazın (AutoValue ile equals ve hashCode metodlarını
oluşturmadıysanız; aksi halde bu testleri güvenle atlayabilirsiniz). Eğer equal instance'ların hash code'ları equal
değilse, nedenini bulun ve problemi düzeltin.

Türetilmiş `(Derived)` field’ları hash code'u computation'dan hariç `(exclude)` tutabilirsiniz. Başka bir deyişle,
computation'a include edilen field'lerden value'su compute edilebilen herhangi bir field'i ignore edebilirsiniz.
equals comparison'larında kullanılmayan herhangi bir field'i hariç `(exclude)` tutmalısınız, aksi takdirde hashCode
contract'ının ikinci hükmünü ihlal etme riskiyle karşılaşırsınız.

2.b Step'inde ki multiply, result'ı field'lerin sırasına depend hale getirir; bu da sınıfın birden fazla benzer field'i
varsa çok daha iyi bir hash function'ı sağlar. Örneğin, bir String hash functionın'da multiplication çıkarılırsa, tüm
anagramların hash code'ları aynı olurdu. 31 değeri tek bir asal sayı `(prime)` olduğu için seçilmiştir. Eğer çift
olsaydı ve multiplication overflow olsaydı, bilgi kaybolurdu çünkü 2 ile çarpma kaydırmaya `(shifting)` eşdeğerdir.
Asal sayı `(prime)` kullanmanın avantajı daha az net olsa da gelenekseldir. 31'in güzel bir özelliği, bazı mimarilerde
daha iyi performans için multiplication'ın bir kaydırma `(shift)` ve bir çıkarmayla `(subtraction)`
değiştirilebilmesidir: `31 * i == (i << 5) - i`. Modern VM'ler bu tür optimizasyonu otomatik olarak yapar.

TelephoneNumber sınıfına önceki tarifi uygulayalım:

```
@Override
public int hashCode() {
    int result = Short.hashCode(areaCode);
    result = 31 * result + Short.hashCode(prefix);
    result = 31 * result + Short.hashCode(lineNum);
    return result;
}
```

Bu metodun, bir `PhoneNumber` instance'ında ki üç önemli `(significant)` field'in tek inputu olduğu basit bir
`deterministic` computation'ının result'ını döndürmesi nedeniyle, `equal PhoneNumber` instance'larının
`equal hash code`'lara sahip olduğu açıktır. Başka bir deyişle, value'yu computation'a include edilen field’lerden
compute edilebilen herhangi bir field’ı göz ardı edebilirsiniz. Basittir, makul derecede hızlıdır ve `unequal`
TelephoneNumber'ları farklı hash bucket'larına dağıtma `(dispersing)` konusunda makul bir iş çıkarır.

Bu maddedeki tarif, makul derecede iyi hash function'ları sağlasa da, bunlar en son teknoloji `(state-of-the-art)`
değildir. Kalite açısından Java platform library'lerinde ki value type'larında bulunan hash function'ları ile comparable
durumdadırlar ve çoğu kullanım için yeterlidirler. Çakışma `(collision)` olasılığı daha düşük hash function'larına
gerçekten ihtiyacınız varsa, Guava'nın `com.google.common.hash.Hashing`'ine bakın.

Objects sınıfı, rastgele `(arbitrary)` sayıda object alan ve bunlar için bir hash code'u döndüren statik bir metoda
sahiptir. Bu yöntem basittir, makul derecede hızlıdır ve `unequal` telefon numaralarını farklı hash bucket’larına
dağıtma `(dispersing)` işini makul şekilde yapar. Ne yazık ki, bu yöntemler daha yavaş çalışır çünkü değişken sayıda
argüman geçirmek için array oluşturmayı ve argümanlar primitive type'da ise `boxing` ve `unboxing` gerektirir. Bu tarz
hash function'ı, yalnızca performansın kritik olmadığı durumlarda kullanılmak üzere önerilir.

İşte bu teknik kullanılarak yazılmış bir PhoneNumber sınıfı için hash fonksiyonu:

```
// One-line hashCode method - mediocre performance
@Override 
public int hashCode() {
    return Objects.hash(lineNum, prefix, areaCode);
}
```

Bir sınıf immutable ise ve hash code'unu computing maliyeti yüksekse, her request geldiğinde recalculating yerine hash
code'unu object içinde cache'e almayı düşünebilirsiniz. Bu type'da ki object'lerin çoğunun hash key'i olarak
kullanılacağını düşünüyorsanız, hash code'unu instance oluşturulduğunda calculate etmelisiniz. Aksi takdirde, hashCode
ilk kez invoke edildiğinde hash code'unu lazily initialize etmeyi seçebilirsiniz. Lazily initialize edilen bir field'in
varlığında sınıfın thread-safe kalmasını sağlamak için biraz dikkat gereklidir. PhoneNumber sınıfımız bu tür bir işlemi
hak etmiyor, ancak nasıl yapıldığını göstermek için işte burada. hashCode field'i için initial value'sunun (bu case'de
0), yaygın olarak oluşturulan bir instance'ın hash code'u olmaması gerektiğini unutmayın:

```
// hashCode method with lazily initialized cached hash code
private int hashCode; // Automatically initialized to 0

@Override 
public int hashCode() {
    int result = hashCode;
    if (result == 0) {
        result = Short.hashCode(areaCode);
        result = 31 * result + Short.hashCode(prefix);
        result = 31 * result + Short.hashCode(lineNum);
        hashCode = result;
    }
    return result;
}
```

Performansı artırmak amacıyla önemli `(significant)` field'leri hash code'u computation'ınından hariç `(exclude)`
tutmaya kalkışmayın. Ortaya çıkan hash function'ı daha hızlı çalışsa da, kalitesizliği hash table'larının performansını
kullanılamaz hale gelecek kadar düşürebilir. Özellikle, hash function'ı, ignore etmeyi seçtiğiniz region'larda farklılık
gösteren geniş bir instance collection'ı ile karşı karşıya kalabilir. Eğer bu olursa, hash function'ı tüm bu instance'
ları birkaç hash code'una map edecek ve linear time'da çalışması gereken programlar bunun yerine `quadratic` time'da
çalışacaktır.

Bu sadece teorik bir problem değil. `Java 2`'den önce, String hash function'ı, first character'den başlayarak string
boyunca eşit aralıklarla yerleştirilmiş en fazla on altı character kullanırdı. URL'ler gibi hiyerarşik isimlerin büyük
collection'ları için, bu function daha önce açıklanan pathological behavior'u tam olarak sergiledi.

hashCode tarafından döndürülen value için ayrıntılı bir spesifikasyon sağlamayın, böylece client'lar buna makul bir
şekilde depend olamaz; bu size onu değiştirme esnekliği verir. Java library'lerinde ki String ve Integer gibi birçok
sınıf, hashCode metotlarının döndürdüğü tam `(exact)` value'yu, instance value'sunun bir fonksiyonu olarak belirtir.
Bu iyi bir fikir değil, yaşamak zorunda kaldığımız bir hata: Gelecek sürümlerde hash function'ını geliştirme yeteneğini
engeller. Detayları belirtmezseniz ve hash function'ının da bir hata bulunursa veya daha iyi bir hash function'ı
keşfedilirse, bunu sonraki bir sürümde değiştirebilirsiniz.

Özetle, her equals metodunu override ettiğinizde hashCode metodunu da override etmelisiniz, aksi takdirde programınız
doğru çalışmayacaktır. hashCode metodunuz, Object sınıfında belirtilen general contract'a uymalı ve `unequal`
instance'lara `unequal` hash code'ları assigning konusunda makul bir iş çıkarmalıdır.