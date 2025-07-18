# Design and document for inheritance or else prohibit it

Item 18, inheritance için tasarlanmamış ve belgelenmemiş "yabancı `(foreign)`" bir sınıfı subclassing yapmanın
tehlikelerine karşı sizi uyarmıştı. Peki bir sınıfın inheritance için tasarlanmış ve belgelenmiş olması ne anlama
geliyor?

İlk olarak, sınıf herhangi bir method'u override etmenin etkilerini kesin olarak belgelemelidir. Başka bir deyişle,
sınıf, override edilebilir method'ları kendi içinde nasıl kullandığını tam olarak belgelemelidir. Her public veya
protected method için, dokümantasyon, method'un hangi override edilebilir method'ları hangi sıra ile invoke ettiğini ve
her invocation'nın sonuçlarının subsequent processing'i nasıl etkilediğini belirtmelidir. (Overridable derken, nonfinal
ve public ya da protected olanları kastediyoruz.) Daha genel olarak, bir sınıf, override edilebilir bir method'u
invoke edebileceği tüm durumları belgelemelidir. Örneğin, invocation'lar background thread'lerden veya static
initializer'lardan gelebilir.

Override edilebilir method'ları invoke eden bir method, bu invoke'ların bir açıklamasını kendi dokümantasyon
comment'inin sonunda içerir. Açıklama, Javadoc etiketi `@implSpec` tarafından oluşturulan, "Implementation Requirements"
başlıklı spesifikasyonun özel bir bölümündedir. Bu bölüm, method'un inner working'ini açıklar.

İşte `java.util.AbstractCollection` spesifikasyonundan kopyalanmış bir örnek:

```
public boolean remove(Object o)
```

"Bu collection'dan belirtilen element'in tek bir instance'ını, eğer mevcutsa, kaldırır (optional operation). Daha resmi
olarak, bu collection bir veya daha fazla böyle element içeriyorsa, `Objects.equals(o, e)` koşulunu sağlayan bir element
`e`'yi kaldırır. Bu collection belirtilen element'i içeriyorsa (veya equivalently olarak, bu call'un bir sonucu olarak
bu collection değiştiyse) true döndürür."

"Implementation Requirements: Bu implementation, belirtilen element'i aramak için collection üzerinde iterate eder. Eğer
element'i bulursa, iterator'ın remove method'unu kullanarak element'i collection'dan kaldırır. Bu implementation'ın, bu
collection'ın iterator method'u tarafından döndürülen iterator remove method'unu implement etmiyorsa ve bu collection
belirtilen object'i içeriyorsa bir `UnsupportedOperationException` fırlattığını unutmayın."

Bu dokümantasyon, iterator method'unu override etmenin remove method'unun behavior'unu etkileyeceği konusunda hiçbir
şüpheye yer bırakmıyor. Ayrıca, iterator method'u tarafından döndürülen Iterator'ın behavior'unun remove method'unun
behavior'unu tam olarak nasıl etkileyeceğini de açıklar. Bunu, programcının HashSet'i subclassing yaparken `add`
method'unu override etmenin `addAll` method'unun behavior'unu etkileyip etkilemeyeceğini basitçe söyleyemediği Item
18'deki durumla karşılaştırın.

Ama bu, iyi API dokümantasyonunun belirli bir method'un ne yaptığını açıklaması ve bunu nasıl yaptığını değil de ne
yaptığını açıklaması gerektiği kuralını ihlal etmez mi? Evet, öyle! Bu, inheritance'ın encapsulation'ı ihlal etmesinin
talihsiz bir sonucudur. Bir sınıfı güvenli bir şekilde subclass'lanabilir olacak şekilde belgelemek için, aksi takdirde
belirtilmemiş bırakılması gereken implementation detaylarını açıklamanız gerekir. `@implSpec` tag'i Java 8'de eklendi ve
Java 9'da yoğun bir şekilde kullanıldı. Bu tag varsayılan olarak etkin olmalıdır, ancak Java 9 itibarıyla, Javadoc
utility hala onu yok saymaktadır, ta ki `-tag "apiNote:a:API Note:"` command line switch'ini iletmediğiniz sürece.

Inheritance için tasarım yapmak, yalnızca `self-use` pattern'larını belgelemekten fazlasını içerir. Programcıların
gereksiz zorluklar yaşamadan verimli subclass'lar yazabilmesini sağlamak için, bir class internal working'ine erişim
sağlayacak şekilde dikkatle seçilmiş `protected method`'lar ya da nadiren `protected field`'lar sunmak zorunda
kalabilir. Örneğin, `java.util.AbstractList` içindeki `removeRange` method'unu ele alalım:

```
protected void removeRange(int fromIndex, int toIndex)
```

Bu listeden, index'i fromIndex (inclusive) ile toIndex (exclusive) arasında olan tüm element'ler kaldırılır. Takip eden
tüm element'leri sola kaydırır (index'lerini azaltır). Bu class, listeyi `(toIndex - fromIndex)` kadar element kısaltır.
(Eğer `toIndex == fromIndex` ise, bu operation'ın hiçbir etkisi olmaz.)

Bu method, bu listede ve sublist'lerinde clear operation'ı tarafından call edilir. Bu method'u, listenin
implementasyonunun internal'larından faydalanacak şekilde override etmek, bu liste ve sublist'lerinde clear
operation'ının performansını önemli ölçüde artırabilir.

Implementation Requirements: Bu implementasyon, fromIndex'ten önce konumlanmış bir list iterator alır ve tüm aralık
kaldırılana kadar `ListIterator.next` ve ardından `ListIterator.remove` call'larını tekrar tekrar yapar. Not: Eğer
`ListIterator.remove` linear time gerektiriyorsa, bu implementasyon quadratic time gerektirir.

Parameters:

fromIndex, kaldırılacak ilk elementin indeksidir.
toIndex, kaldırılacak son elementten sonraki indekstir.

Bu method, bir List implementasyonunun end user'ları için ilgi çekici değildir. Bu method, yalnızca subclass'ların
sublist'lerde hızlı bir clear method'u sağlamasını kolaylaştırmak için sunulmuştur. removeRange method'u olmadığında,
subclass'lar sublist'lerde clear method'u invoke edildiğinde quadratic performansla yetinmek zorunda kalır ya da tüm
subList mekanizmasını baştan yazmak zorunda kalır — bu da kolay bir iş değildir!

Peki, inheritance için bir class tasarlarken hangi protected member'ların açığa `(expose)` çıkarılacağına nasıl karar
verirsiniz? Ne yazık ki, sihirli bir çözüm yoktur. Yapabileceğiniz en iyi şey, iyi düşünmek, en iyi tahmininizi yapmak
ve ardından subclass yazarak test etmektir. Mümkün olduğunca az protected member açığa `(expose)` çıkarmalısınız çünkü
her biri implementasyon detayına yapılmış bir taahhüttür. Öte yandan, çok az protected member açığa `(expose)`
çıkarmamalısınız çünkü eksik bir protected member, bir class'ı inheritance için pratik olarak kullanılamaz hale
getirebilir.

Inheritance için tasarlanmış bir class'ı test etmenin tek yolu subclass yazmaktır. Eğer kritik `(crucial)` bir protected
member'i atladıysanız `(omit)`, bir subclass yazmaya çalışmak bu eksikliği acı bir şekilde ortaya çıkarır. Tersine,
birkaç subclass yazılır ve hiçbiri protected member'i kullanmazsa, muhtemelen onu private yapmalısınız. Deneyim, bir
extendable class'ı test etmek için genellikle üç subclass'ın yeterli olduğunu gösterir. Bu subclass'lardan biri veya
birkaçı, superclass yazarından farklı biri tarafından yazılmalıdır.

Geniş çapta kullanılma ihtimali yüksek bir class'ı inheritance için tasarlarken, belgelediğiniz self-use pattern'larına
ve protected method ve field'larında gizli olan implementasyon decision'larına sonsuza kadar bağlı kaldığınızı fark
edin. Bu taahhütler, sonraki bir sürümde class'ın performansını veya functionality'sini geliştirmeyi zor veya imkansız
hale getirebilir. Bu nedenle, class'ınızı yayımlamadan önce subclass yazarak test etmelisiniz.

Ayrıca, inheritance için gereken özel dokümantasyonun, class'ınızın instance'larını oluşturup methodlarını invoke eden
programcılar için tasarlanmış normal dokümantasyonu karıştırdığını unutmayın. Bu yazının yazıldığı tarihte, sıradan API
dokümantasyonunu yalnızca subclass implementasyonu yapan programcılar için ilgi çekici bilgiden ayıracak pek az araç
vardır.

Inheritance'a izin vermek için bir class'ın uyması gereken birkaç kısıtlama daha vardır. Constructor'lar, directly ya da
indirectly olarak override edilebilir method'ları invoke etmemelidir. Bu kuralı ihlal ederseniz, program failure oluşur.
Superclass constructor, subclass constructor'dan önce çalışır, bu yüzden subclass'taki override edilen method, subclass
constructor çalışmadan önce invoke edilir. Eğer override edilen method, subclass constructor tarafından yapılan herhangi
bir initialization'a depend ise, method beklenen şekilde davranmaz. Bunu somutlaştırmak için, bu kuralı ihlal eden bir
sınıf örneği aşağıdadır:

```
public class Super {
// Broken - constructor invokes an overridable method
    public Super() {
        overrideMe();
    }

    public void overrideMe() {
    }
}
```

İşte `overrideMe` method'unu override eden bir subclass, ki bu method Super'ın tek constructor'ı tarafından hatalı bir
şekilde invoke edilir:

```
public final class Sub extends Super {
    // Blank final, set by constructor
    private final Instant instant;
    Sub() {
        instant = Instant.now();
    }

    // Overriding method invoked by superclass constructor
    @Override 
    public void overrideMe() {
        System.out.println(instant);
    }

    public static void main(String[] args) {
        Sub sub = new Sub();
        sub.overrideMe();
        // => null
        // => 2025-07-06T12:22:11.156580Z
    }
}
```

Bu programın instant'ı iki kez yazdırmasını bekleyebilirsiniz, ancak ilk seferde `null` yazdırır çünkü overrideMe, Sub
constructor instant field'ını initialize'dan önce Super constructor tarafından çağrılır. Bu programın, bir final field'ı
iki farklı state'de observe ettiğine dikkat edin! Ayrıca, `overrideMe` instant üzerinde herhangi bir method invoke etmiş
olsaydı, Super constructor `overrideMe`'yi çağırdığında bir `NullPointerException` fırlatılmış olurdu. Bu programın şu
hâliyle `NullPointerException` fırlatmamasının tek nedeni, println method'unun null parametreleri tolere etmesidir.

Constructor'dan private method'ları, final method'ları ve static method'ları invoke etmek güvenlidir; çünkü bunların
hiçbiri overridable değildir.

Cloneable ve Serializable interface'leri, inheritance için tasarım yaparken özel zorluklar ortaya çıkarır. Inheritance
için tasarlanmış bir class'ın bu interface'lerden herhangi birini implement etmesi genellikle iyi bir fikir değildir,
çünkü bu, class'ı extend eden programcılar üzerine ciddi bir yük getirir. Ancak, subclass'ların bu interface'leri
implement etmesini zorunlu kılmadan bunu yapabilmelerini sağlamak için alabileceğiniz özel önlemler vardır.

Eğer inheritance için tasarlanmış bir class'ta Cloneable veya Serializable interface'lerinden birini implement etmeye
karar verirseniz, `clone` ve `readObject` method'larının constructor'lara çok benzediğini ve benzer bir kısıtlamanın
geçerli olduğunu bilmelisiniz: clone veya readObject method'larının hiçbiri, directly ya da indirectly overridable bir
method invoke etmemelidir. readObject case'inde, override edilen method, subclass'ın state'i deserialize edilmeden önce
çalışacaktır. clone case'inde ise, override edilen method, subclass'ın clone method'u clone'un state'ini düzeltme
fırsatı bulmadan önce çalışır. Her iki casede de, program hatası oluşması muhtemeldir. clone durumunda, hata hem
orijinal object'e hem de clone'a zarar verebilir. Bu, örneğin override edilen method clone'un object'in deep
structure'ının bir kopyasını değiştirdiğini varsayıyorsa ancak kopya henüz oluşturulmamışsa gerçekleşebilir. Son olarak,
inheritance için tasarlanmış bir class'ta Serializable implement etmeye karar verirseniz ve class'ın bir readResolve
veya writeReplace method'u varsa, bu method'ları private yerine protected yapmalısınız. Bu method'lar private ise,
subclass'lar tarafından sessizce göz ardı edilirler. Bu, implementasyon detayının inheritance'a izin vermek için bir
class'ın API'sinin parçası haline geldiği bir başka case'dir.

Artık inheritance için bir class tasarlamanın büyük çaba gerektirdiği ve class üzerinde önemli kısıtlamalar getirdiği
açık olmalıdır. Bu, hafife alınarak yapılacak bir karar değildir. Abstract sınıflar ve interface'lerin skeletal
implementation'ları gibi bazı durumlarda bu kesinlikle doğru bir yaklaşımdır. Immutable class'lar gibi, bunun açıkça
yanlış olduğu başka durumlar da vardır.

Peki ya sıradan `(ordinary)` concrete class'lar? Geleneksel olarak, ne final'dırlar ne de subclassing için tasarlanmış
ve belgelenmişlerdir, ancak bu durum tehlikelidir. Böyle bir sınıfta her değişiklik yapıldığında, sınıfı extend eden
subclass'ların bozulma ihtimali vardır. Bu sadece teorik bir sorun değil. Inheritance için tasarlanmamış ve
belgelenmemiş nonfinal concrete class'ların internal'lerinde değişiklikler yapıldıktan sonra subclassing ile ilgili hata
raporları almak nadir değildir.

Bu sorunun en iyi çözümü, güvenli bir şekilde subclass'lanmak üzere tasarlanmamış ve belgelenmemiş sınıflarda
subclassing'i yasaklamaktır. Subclassing'i yasaklamanın iki yolu vardır. Bunlardan daha kolay olanı, sınıfı final olarak
declare etmektir. Alternatif olarak, tüm constructor'ları `private` veya `package-private` yapmak ve constructor'ların
yerine `public static factories` eklemektir. Bu alternatif (ki bu, internally olarak subclass'ları kullanma esnekliği
sağlar) Item 17'de tartışılmıştır. Her iki yaklaşım da kabul edilebilir.

Bu tavsiye biraz tartışmalı olabilir çünkü birçok programcı, instrumentation, notification ve synchronization gibi
özellikler eklemek veya functionality'i sınırlamak için sıradan concrete class'ları subclassing yapmaya alışmıştır.
Bir sınıf Set, List veya Map gibi özünü capture eden bir interface'i implement ediyorsa, subclassing'i yasaklama
konusunda hiçbir tereddüt duymamalısınız. Item 18'de açıklanan wrapper class pattern, functionality artırmak için
inheritance'a üstün bir alternatif sunar.

Eğer concrete class standart bir interface'i implement etmiyorsa, inheritance'ı yasaklayarak bazı programcıları zor
durumda bırakabilirsiniz. Böyle bir sınıftan inheritance'a izin vermeniz gerektiğini düşünüyorsanız, makul bir yaklaşım,
sınıfın hiçbir override edilebilir method'unu invoke etmediğinden emin olmak ve bu gerçeği belgelemektir. Başka bir
deyişle, sınıfın override edilebilir method'ları self-use'unu tamamen ortadan kaldırın. Bunu yaparak, subclass yapılması
makul ölçüde güvenli bir sınıf oluşturursunuz. Bir method'u override etmek, başka hiçbir method'un davranışını asla
etkilemez.

Bir sınıfın override edilebilir method'ları self-use'unu, davranışını değiştirmeden mekanik olarak ortadan
kaldırabilirsiniz. Her override edilebilir method'un body'sini private bir "helper method"a taşıyın ve her override
edilebilir method'un kendi private helper method'unu invoke etmesini sağlayın. Ardından, override edilebilir bir
method'un her self-use'unu, override edilebilir method'un private helper method'una doğrudan bir invoke ile değiştirin.

Özetle, bir sınıfı inheritance için tasarlamak zor bir iştir. Tüm self-use pattern'lerini belgelemelisiniz ve
belgeledikten sonra, sınıfın ömrü boyunca bunlara uymalısınız. Bunu yapmazsanız, subclass'lar superclass'ın
implementation detaylarına bağımlı hale gelebilir ve superclass'ın implementation'ı değişirse bozulabilirler.
Başkalarının efficient subclass'lar yazmasına izin vermek için, bir veya daha fazla protected method'u da export etmeniz
gerekebilir. Subclass'lara gerçek bir ihtiyaç olduğunu bilmedikçe, sınıfınızı final olarak bildirerek veya erişilebilir
constructor'ların olmadığından emin olarak inheritance'ı yasaklamanız muhtemelen daha iyidir.