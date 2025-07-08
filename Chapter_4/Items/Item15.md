# Minimize the accessibility of classes and members

`Well-designed` bir component'i kötü `(poorly)` designed bir component'den ayıran en önemli faktör, component'in
internal datalarını ve diğer implementation detaylarını diğer component'lerden ne derecede gizlediğidir. İyi tasarlanmış
bir component, tüm implementation detaylarını gizler, API'sini implementation'ınından net bir şekilde seperate eder.
Component'ler daha sonra yalnızca API'leri aracılığıyla communicate kurar ve birbirlerinin iç işleyişinden
`(inner workings)` habersiz olurlar. Information hiding veya encapsulation olarak bilinen bu kavram, yazılım tasarımının
temel bir ilkesidir.

Information hiding bir sistemi oluşturan component'leri birbirinden ayırır `(decouples)` ve onların isolated bir şekilde
develop edilmesine, test edilmesine, optimize edilmesine, kullanılmasına, anlaşılmasına ve değiştirilmesine olanak
sağladığı için önemlidir. Bu, system development'ı hızlandırır çünkü component'ler paralel olarak geliştirilebilir.
Maintenance yükünü hafifletir çünkü component'ler daha hızlı anlaşılabilir ve diğer component'lere zarar verme korkusu
olmadan debug edilebilir veya replace edilebilir. Information hiding tek başına iyi performans sağlamaz, ancak effective
performance tuning'i mümkün kılar: Bir sistem tamamlandığında ve profiling hangi component'lerin performans sorunlarına
neden olduğunu belirlediğinde, bu component'ler diğerlerinin doğruluğunu etkilemeden optimize edilebilir. Information
hiding, tightly coupled olmayan component'lerin geliştirildikleri context'ler dışında da faydalı olduğunu kanıtladığı
için software reuse'u artırır. Son olarak, information hiding large system'ler building riskini azaltır çünkü bireysel
`(individual)` component'ler, system başarılı olmasa bile başarılı olabilir.

Java'nın information hiding'e yardımcı olacak birçok özelliği vardır. Access control mekanizması `[JLS, 6.6]`,
class'ların, interface'lerin ve member'larının accessibility'sini belirtir. Entity'nin accessibility'si, declaration'ın
location'ınına ve access modifier'lardan `(private, protected ve public)` herhangi birinin declaration'da bulunup
bulunmadığına göre belirlenir. Bu modifier'ların doğru kullanımı information hiding için çok önemlidir.

Kural basit : Her class'ı veya member'i mümkün olduğunca `inaccessible` yapın. Başka bir deyişle, yazdığınız yazılımın
düzgün çalışmasıyla tutarlı olan mümkün olan en düşük access level'ini kullanın.

Top-level (non-nested) class'lar ve interface'ler için yalnızca iki olası access level var: `package-private ve public`
Bir top-level class'ı veya interface'i public modifier'ı ile declare ederseniz, public olacaktır; aksi takdirde
`package-private` olacaktır. Eğer bir top-level class veya interface `package-private` yapılabilirse, öyle yapılmalıdır.
Onu `package-private` yaparak, exported API yerine implementation'ın bir parçası haline getirirsiniz ve mevcut client'
lara zarar verme korkusu olmadan sonraki bir sürümde onu modify edebilir, replace edebilir veya eliminate edebilirsiniz.
Onu public yaparsanız, uyumluluğu sürdürmek için sonsuza dek maintain etmek zorunda kalırsınız.

Eğer `package-private` bir top-level class veya interface yalnızca tek bir sınıf tarafından kullanılıyorsa, bu top-level
class'ı, onu kullanan tek class'ın `private static nested class`'ı yapmayı düşünebilirsiniz. Bu, onun
accessibility'sini, package'inde ki tüm class'lardan, onu kullanan tek bir class'a indirger. Ancak `package-private` bir
top-level class'a göre, gereksiz yere public bir sınıfın accessibility'sini azaltmak çok daha önemlidir: public class,
package'in API'sinin bir parçasıyken, `package-private` top-level class zaten implementation'ın bir parçasıdır.

Member'lar (field'lar, metotlar, nested class'lar ve nested interface'ler) için, increasing accessibility sırasına göre
listelenen dört olası access level vardır:

* `private` — Member, yalnızca declare edildiği top-level class'dan erişilebilir.

* `package-private` - Member, declare edildiği package'de ki herhangi bir sınıftan erişilebilir. Teknik olarak
  default access olarak bilinen bu, hiçbir access modifier belirtilmediğinde (default olarak public olan interface
  member'ları hariç) elde ettiğiniz access seviyesidir.

* `protected` - Member, declare edildiği sınıfın subclass'larından (bazı kısıtlamalara tabi olarak `[JLS, 6.6.2]`) ve
  declare edildiği package'de ki herhangi bir class'dan erişilebilir.

* `public` - Member her yerden erişilebilir.

Sınıfınızın `public` API'sini dikkatlice tasarladıktan sonra, refleksiniz diğer tüm member'ları `private` yapmak
olmalıdır. Yalnızca aynı package'de ki başka bir class'ın bir member'a gerçekten erişmesi gerekiyorsa, `private`
modifier'ını kaldırmalı ve member'i `package-private` yapmalısınız. Eğer bunu sık sık yaparken buluyorsanız kendinizi,
sisteminizin tasarımını yeniden gözden geçirmeli ve başka bir ayrıştırmanın `(decomposition)` birbirinden daha iyi
ayrıştırılmış `(decoupled)` class'lar ortaya çıkarıp çıkarmayacağına bakmalısınız. Bununla birlikte, hem `private` hem
de `package-private` member'lar bir sınıfın implementation'ının bir parçasıdır ve normalde exported API'sini
etkilemezler. Ancak bu field'ler, sınıf `Serializable`'ı implement ediyorsa dışa aktarılan `API`'ye "sızabilir (leak)".

public sınıfların member'ları için, access level'i `package-private`'dan `protected`'a çıktığında accessibility'de çok
büyük bir artış meydana gelir. `Protected` member, sınıfın exported API'sinin bir parçasıdır ve sonsuza dek
desteklenmelidir. Ayrıca, exported bir sınıfın `protected` member'i, bir implementation detayına yönelik public bir
taahhüdü represent eder. Protected member'ların gerekliliği nispeten nadir olmalıdır.

Metotların accessibility'sini azaltma yeteneğinizi kısıtlayan önemli bir kural vardır. Eğer bir metot bir superclass
metodunu override ediyorsa, subclass'da superclass'dakinden daha kısıtlayıcı bir access level'ina sahip olamaz
`[JLS, 8.4.8.3]`. Bu, subclass'ın bir instance'ının, superclass'ın bir instance'ının kullanılabildiği her yerde
kullanılabilmesini sağlamak için gereklidir `(Liskov substitution principle)`. Bu kuralı ihlal ederseniz, subclass'ı
compile etmeye çalıştığınızda compiler bir hata mesajı oluşturacaktır. Bu kuralın özel bir case'i de şudur: Eğer bir
sınıf bir interface implement ediyorsa, interface'de yer alan tüm sınıf metotları o sınıfta public olarak
bildirilmelidir.

Kodunuzu test etmeyi kolaylaştırmak için bir sınıfı, interface'i veya member'i normalden daha accessible yapmayı cazip
bulabilirsiniz. Bir yere kadar sorun yok. Bir public sınıfın private member'ini test etmek amacıyla `package-private`
yapmak kabul edilebilir, ancak accessibility'i daha fazla artırmak kabul edilemez. Başka bir deyişle, test etmeyi
kolaylaştırmak için bir sınıfı, interface'i veya member'i bir package'in exported API'sinin bir parçası yapmak kabul
edilemez. Neyse ki, bu da gerekli değil çünkü testler, test edilen package'in bir parçası olarak çalıştırılabilir ve
böylece `package-private` element'lerine erişim sağlayabilir.

Public sınıfların instance field'leri nadiren public olmalıdır. Eğer bir instance field'i non-final veya mutable bir
object'e referans ise, onu public yaparak, field'de saklanabilecek değerleri sınırlama yeteneğinizden vazgeçmiş
olursunuz. Bu, field ile ilgili sabitleri `(invariants)` uygulama `(involving)` yeteneğinizden vazgeçtiğiniz anlamına
gelir. Ayrıca, field modify edildiğinde herhangi bir action yapma yeteneğinizden vazgeçersiniz, bu nedenle public
mutable field'lere sahip sınıflar genellikle `thread-safe` değildir. Bir field final olsa ve immutable bir object'e
referans verse bile, onu public yaparak, field'in var olmadığı yeni bir internal data representation'ınına geçme
esnekliğinden vazgeçmiş olursunuz.

Aynı tavsiye static field'ler için de geçerlidir, tek bir istisna dışında. Constant'ların sınıf tarafından sağlanan
abstraction'ın ayrılmaz bir parçası olduğunu varsayarak, bunları `public static final field`'ler aracılığıyla dışarıya
açabilirsiniz. Kural olarak, bu tür field'lerin adları büyük harflerden oluşur ve kelimeler alt çizgiyle ayrılır. Bu
field'lerin ya primitive değerler ya da immutable object'lere referanslar içermesi çok önemlidir. Mutable bir object'e
referans içeren bir field, non-final bir field'in tüm dezavantajlarına sahiptir. Referans değiştirilemese de, referans
verilen object değiştirilebilir ve bu da feci sonuçlara yol açabilir.

`Non-zero` uzunluktaki bir array'in her zaman mutable olduğunu unutmayın, bu nedenle bir sınıfın public static final
array field'i veya böyle bir field'i döndüren bir accessor'e sahip olması yanlıştır. Eğer bir sınıfın böyle bir field'i
veya accessor'u varsa, client'lar array'in content'ini değiştirebilecektir. Bu, sıkça karşılaşılan bir güvenlik açığı
kaynağıdır:

```
// Potential security hole!
public static final Thing[] VALUES = { ... };
```

Bazı IDE'lerin private array field'lerine referans döndüren accessor'ler ürettiği ve tam olarak bu soruna yol açtığı
gerçeğine dikkat edin. Bu sorunu çözmenin iki yolu var. Public array'yi private yapabilir ve public immutable bir liste
ekleyebilirsiniz:

```
private static final Thing[] PRIVATE_VALUES = { ... };

public static final List<Thing> VALUES = Collections.unmodifiableList(Arrays.asList(PRIVATE_VALUES));
```

Alternatif olarak, array'i private yapabilir ve private array'in bir kopyasını döndüren public bir metot
ekleyebilirsiniz:

```
private static final Thing[] PRIVATE_VALUES = { ... };

public static final Thing[] values() {
    return PRIVATE_VALUES.clone();
}
```

Bu alternatifler arasında seçim yapmak için, client'in sonuçla ne yapması muhtemel olduğunu düşünün. Hangi dönüş tipi
daha uygun olur? Hangisi daha iyi performans sağlar?

Java 9 itibarıyla, module sisteminin bir parçası olarak iki ek, implicit access level'i tanıtıldı. Module, tıpkı bir
package'in sınıfları gruplandırması gibi, package'ların bir gruplandırılmasıdır. Bir module, module declaration'ınında
ki export declaration'ları aracılığıyla (geleneksel olarak `module-info.java` adlı bir source file'da bulunur) bazı
package'lerini explicitly export edebilir. Bir module'de ki `unexported` package'lerin public ve protected member'ları,
module dışında inaccessible'dir: module içinde, accessibility export declaration'larından etkilenmez. Module sistemini
kullanmak, sınıfları bir module içindeki package'ler arasında paylaşmanıza olanak tanır, ancak onları tüm dünyaya
görünür hale getirmez. `Unexported` package’lerdeki public sınıfların public ve protected member'ları, normal public ve
protected erişim seviyelerinin `intramodular` benzerleri olan iki implicit access level'i ortaya çıkarır. Bu tür
paylaşım ihtiyacı nispeten nadirdir ve genellikle sınıflarınızı package’ler içinde yeniden düzenleyerek `(rearranging)`
ortadan kaldırılabilir.

Dört main access level'in aksine, module-based iki erişim seviyesi büyük ölçüde tavsiye niteliğindedir. Bir module'un
JAR dosyasını uygulamanızın module path’i yerine class path’ine yerleştirirseniz, module'de ki package’ler non-modular
behavior'larına geri döner: Package’ler module tarafından export edilip edilmediğine bakılmaksızın, package’lerdeki
public sınıfların tüm public ve protected member'ları normal accessibility'lerine sahip olur. Yeni tanıtılan access
level'larının kesin olarak uygulandığı tek yer JDK’nin kendisidir: Java kütüphanelerindeki `unexported` package’ler,
module'lerin dışından gerçekten erişilemez durumdadır.

Module'lerin sağladığı access protection, tipik bir Java programcısı için sınırlı fayda sağlar ve büyük ölçüde tavsiye
niteliğindedir; bunun avantajından yararlanmak için package’lerinizi module'ler halinde gruplamalı, tüm
dependencies'leri module declaration'larında explicit belirtmeli, source tree'nizi yeniden düzenlemeli `(rearrange)` ve
module'lerinizden non-modularized package’lere erişimi sağlamak için özel önlemler almalısınız. Module'lerin JDK’nin
dışında yaygın olarak kullanılıp kullanılmayacağını söylemek için henüz çok erken. Bu arada, güçlü bir ihtiyacınız yoksa
module'lerden kaçınmak en iyisi gibi görünüyor.

Özetlemek gerekirse, program element'lerini accessibility'sini mümkün olduğunca (makul ölçüde) azaltmalısınız. Özenle
tasarlanmış minimal bir public API’den sonra, rastgele `(stray)` sınıfların, interface'lerin veya member'ların API’nin
bir parçası olmasını engellemelisiniz. Constant'lar olarak hizmet eden `public static final field`'ler hariç, public
sınıfların hiçbir public field'i olmamalıdır. `public static final field`'ler tarafından referans verilen object'lerin
immutable olmasını sağlayın.