# Use enums instead of int constants

Enumerated type, legal value'ları yılın mevsimleri, güneş sistemindeki gezegenler veya bir deste oyun kartındaki
suit'ler gibi fixed bir constant setinden oluşan bir type'tır. Enum type'lar dile eklenmeden önce, enumerated type'ları
represent etmek için yaygın bir pattern, type'ın her bir member'i için bir tane olacak şekilde, isimlendirilmiş bir grup
int constant declare etmekti:

```java
// int enum pattern – ciddi şekilde yetersiz
public static final int APPLE_FUJI = 0;
public static final int APPLE_PIPPIN = 1;
public static final int APPLE_GRANNY_SMITH = 2;
public static final int ORANGE_NAVEL = 0;
public static final int ORANGE_TEMPLE = 1;
public static final int ORANGE_BLOOD = 2;
```

int enum pattern olarak bilinen bu teknik, birçok eksikliğe sahiptir. Bu teknik, type safety açısından hiçbir şey
sağlamaz ve expressive power açısından çok az şey sunar. Bir method orange beklerken ona bir apple geçirirsen, `==`
operator'ü ile apple'ları orange'larla karşılaştırırsan veya daha kötüsü, compiler şikayet etmez.

```java
// Lezzetli citrus aromalı applesauce!
int i = (APPLE_FUJI - ORANGE_TEMPLE) / APPLE_PIPPIN;
```

Her apple constant'ın adı `APPLE_` ile, her orange constant'ın adı ise `ORANGE_` ile prefixlenmiştir. Bunun sebebi,
Java'nın `int enum` grupları için namespace sağlamamasıdır. Prefix'ler, `ELEMENT_MERCURY` ile `PLANET_MERCURY` arasında
olduğu gibi, iki int enum grubunun aynı ada sahip constant'ları olduğunda isim çakışmalarını önler.

int enum kullanan programlar kırılgandır. int enumlar constant variable'lardır `[JLS, 4.12.4]`, bu yüzden int değerleri
onları kullanan client'ların içine compile edilir `[JLS, 13.1]`. Bir int enum ile ilişkili value değiştirilirse,
client'larının yeniden compile edilmesi gerekir. Eğer yapılmazsa, client'lar yine çalışır ama davranışları yanlış olur.

int enum constant'larını printable string'lere çevirmek için kolay bir yol yoktur. Böyle bir constant'ı yazdırırsanız
veya debugger'dan görüntülerseniz, gördüğünüz sadece bir sayı olur ve bu çok faydalı değildir. Bir int enum grubundaki
tüm int enum constant'ler üzerinde güvenilir bir şekilde iterate etmek veya int enum grubunun boyutunu almak için bir
yol yoktur.

Bu pattern'ın bir varyantıyla karşılaşabilirsiniz; burada int constant'lar yerine String constant'lar kullanılır. String
enum pattern olarak bilinen bu varyant, daha da az tercih edilir. Constantları için printable string'ler sağlasa da,
deneyimsiz kullanıcıların field adları yerine string constant'ları client koduna hard-code etmesine yol açabilir. Böyle
bir hard-coded string constant yazım hatası içeriyorsa, compile time'da tespit edilmez ve runtime'da bug'lara yol
açar. Ayrıca, string comparison'lara dayandığı için performans sorunlarına yol açabilir.

Neyse ki, Java, int ve string enum pattern'larının tüm eksikliklerinden kaçınan ve birçok ek avantaj sağlayan bir
alternatif sunar. Bu, enum type'dır `[JLS, 8.9]`. En basit haliyle şöyle görünür:

```java
public enum Apple {FUJI, PIPPIN, GRANNY_SMITH}

public enum Orange {NAVEL, TEMPLE, BLOOD}
```

Görünüşte, bu enum type'lar `C, C++, C#` gibi diğer dillerdekinlere benzer görünebilir, ancak görünüşler aldatıcıdır.
Java'nın enum type'ları tam teşekküllü `(full-fledged)` class'lardır ve bu diğer dillerdeki, esasen int değerler olan
enum'lardan çok daha güçlüdür.

Java'nın enum type'larının temel fikri basittir: Her `enumeration constant` için `public static final field`
aracılığıyla bir instance export edilir. Enum type'lar, erişilebilir constructor'ları olmaması nedeniyle effectively
final'dir. Client'lar enum type'ın instance'larını oluşturamaz veya onu extend edemez, bu yüzden enum constant'lar
dışında başka instance olamaz. Başka bir deyişle, enum type'lar `instance-controlled`'dır. Bunlar, esasen tek elemanlı
enum olan singleton'ların bir generalization'ıdır (Item 3).

Enum'lar `compile-time type safety` sağlar. Bir parametreyi Apple type'ı olarak declare edersen, parametreye geçen
herhangi bir `non-null` object reference'in üç geçerli Apple value'dan biri olduğu garanti edilir. Yanlış type değerleri
geçirmeye çalışmak, bir enum type expression'nını başka bir değişkene assign etmeye veya farklı enum type değerlerini
`==` operatörüyle karşılaştırmaya çalışmak `compile-time error` ile sonuçlanır.

Aynı ada sahip constant'lar içeren enum type'lar, her type'ın kendi namespace'i olduğu için sorunsuzca bir arada
bulunabilir. Bir enum type'daki constant'ları client'larını yeniden compile etmeden ekleyebilir veya sıralamasını
değiştirebilirsiniz çünkü constant'ları export eden field'ler enum type ile client'ları arasında bir izolasyon katmanı
sağlar: constant değerler, int enum pattern'larında olduğu gibi client'ların içine compile edilmez. Son olarak,
enum'ları printable string'lere çevirmek için `toString` method'unu çağırabilirsiniz.

int enum'ların eksikliklerini gidermenin yanı sıra, enum type'lar istediğiniz method ve field'leri eklemenize ve
istediğiniz interface'leri implement etmenize olanak tanır. Tüm Object method'larının high-quality implementasyonlarını
sağlarlar, Comparable ve Serializable implement ederler ve serialized halleri enum type'daki çoğu değişikliğe dayanacak
şekilde tasarlanmıştır.

Neden bir enum type'a method veya field eklemek istersiniz? Öncelikle, constant'larıyla data ilişkilendirmek
isteyebilirsiniz. Örneğin, Apple ve Orange type'larımız, meyvenin rengini döndüren veya görüntüsünü döndüren bir
method'dan fayda görebilir. Bir enum type'ı uygun görülen herhangi bir method ile güçlendirebilirsiniz. Bir enum type,
basit bir enum constant collection'ı olarak başlayabilir ve zamanla tam özellikli `(full-featured)` bir abstraction
haline gelebilir.

Zengin bir enum type için güzel bir örnek olarak güneş sistemimizdeki sekiz gezegeni düşünün. Her gezegenin bir `mass`'i
ve bir `radius`'u vardır ve bu iki attribute'tan `surface gravity`'si hesaplanabilir. Bu da, bir object'in `mass`'i
verildiğinde, gezegenin yüzeyindeki `weight`'ini hesaplamanıza olanak tanır. Bu enum şöyle görünür. Her enum
constant'tan sonra parantez içinde yer alan sayılar, constructor'ına geçirilen parameter'lardır. Bu case de, bunlar
gezegenin `mass`'i ve `radius`'udur.

```java
enum Planet {
    MERCURY(3.302e+23, 2.439e6),
    VENUS(4.869e+24, 6.052e6),
    EARTH(5.975e+24, 6.378e6),
    MARS(6.419e+23, 3.393e6),
    JUPITER(1.899e+27, 7.149e7),
    SATURN(5.685e+26, 6.027e7),
    URANUS(8.683e+25, 2.556e7),
    NEPTUNE(1.024e+26, 2.477e7);

    private final double mass;            // In kilogram
    private final double radius;          // In meters
    private final double surfaceGravity;  // In m / s^2

    // Universal gravitational constant in m^3 / kg s^2
    private static final double G = 6.67300E-11;

    Planet(double mass, double radius) {
        this.mass = mass;
        this.radius = radius;
        surfaceGravity = G * mass / (radius * radius);
    }

    public double mass() {
        return mass;
    }

    public double radius() {
        return radius;
    }

    public double surfaceGravity() {
        return surfaceGravity;
    }

    public double surfaceWeight(double mass) {
        return mass * surfaceGravity; // F = ma
    }
}
```

Planet gibi zengin bir enum type yazmak kolaydır. Enum constant'larıyla data ilişkilendirmek için, instance field'lar
declare edin ve data'yı alıp bu field'lara store eden bir constructor yazın. Enum'lar doğaları gereği immutable'dır, bu
yüzden tüm field'lar final olmalıdır. Field'lar public olabilir, ancak onları private yapmak ve public accessor'lar
sağlamak daha iyidir. Planet case'inde, constructor ayrıca surface gravity'sini de hesaplayıp store eder, ancak bu
sadece bir optimizasyondur. Gravity, surfaceWeight method'u tarafından her kullanıldığında mass ve radius'tan recompute
edilebilir; bu method bir object'in mass'ini alır ve constant tarafından represent edilen gezegendeki weight'ini
döndürür.

Planet enum basit olmasına rağmen, şaşırtıcı derecede güçlüdür. Bir object'in earth weight'ini (herhangi bir birimde)
alıp object'in weight'inin tüm sekiz gezegendeki değerini (aynı birimde) güzel bir tablo olarak yazdıran kısa bir
program:

```java
class WeightTable {
    public static void main(String[] args) {
        double earthWeight = Double.parseDouble(args[0]);
        double mass = earthWeight / Planet.EARTH.surfaceGravity();
        for (Planet p : Planet.values()) {
            System.out.printf("Weight on %s is %f%n", p, p.surfaceWeight(mass));
        }
    }
}
```

Planet'in, tüm enum'lar gibi, declare edildikleri sırada value'larını döndüren static `values` method'u olduğunu
unutmayın. Ayrıca, `toString` method'unun her enum value'nun declare edilmiş adını döndürdüğünü ve böylece `println` ve
`printf` ile kolay yazdırmayı sağladığını unutmayın. Bu string representation'dan memnun değilseniz, toString method'unu
override ederek değiştirebilirsiniz. WeightTable programımızı (toString'ı override etmeyen) `185` command line
argümanıyla çalıştırmanın sonucu:

```
Weight on MERCURY is 69.912739
Weight on VENUS is 167.434436
Weight on EARTH is 185.000000
Weight on MARS is 70.226739
Weight on JUPITER is 467.990696
Weight on SATURN is 197.120111
Weight on URANUS is 167.398264
Weight on NEPTUNE is 210.208751
```

Enum'lar Java'ya eklendikten iki yıl sonra, 2006'ya kadar, Pluto bir gezegendi. Bu şu soruyu gündeme getirir: “Bir enum
type'dan bir element kaldırıldığında ne olur?” Cevap şudur: Kaldırılan elemente referans vermeyen herhangi bir client
program sorunsuz çalışmaya devam eder. Örneğin, WeightTable programımız bir satır daha az içeren bir tablo yazdırır.
Peki kaldırılan elemente (bu durumda `Planet.Pluto`) referans veren bir client program ne olur? Client programı yeniden
compile ederseniz, eski gezegene referans veren satırda faydalı bir hata mesajıyla compile başarısız olur; eğer client'ı
yeniden compile etmezseniz, bu satırda runtime'da faydalı bir exception fırlatır. Bu, int enum pattern'ında elde
edeceğinizden çok daha iyi, umabileceğiniz en iyi davranıştır.

Enum constant'larıyla ilişkili bazı davranışlar yalnızca enum'un tanımlandığı class veya package içinde kullanılmalıdır.
Bu tür davranışlar en iyi şekilde `private` veya `package-private` method'lar olarak implement edilir. Her constant,
enum'u içeren class veya package'ın constant ile karşılaştığında uygun şekilde tepki vermesini sağlayan gizli bir
davranış koleksiyonunu taşır. Diğer class'larda olduğu gibi, bir enum method'unu client'larına expose etmek için güçlü
bir nedeniniz yoksa, onu `private` veya gerekirse `package-private` olarak declare edin.

Bir enum genel olarak faydalıysa, top-level class olmalıdır; kullanımı belirli bir top-level class'a bağlıysa, o
top-level class'ın member class'ı olmalıdır. Örneğin, `java.math.RoundingMode` enum, ondalık kesirler
`(decimal fractions)` için bir rounding mode'u represent eder. Bu rounding mode'lar `BigDecimal` class'ı tarafından
kullanılır, ancak temel olarak BigDecimal'a bağlı olmayan `(tied)` faydalı bir abstraction sağlarlar. RoundingMode'u
top-level enum yaparak, kütüphane tasarımcıları, rounding mode'lara ihtiyaç duyan programcıların bu enum'u yeniden
kullanmalarını teşvik eder ve bu da API'ler arasında tutarlılığı artırır.

Planet örneğinde gösterilen teknikler çoğu enum type için yeterlidir, ancak bazen daha fazlasına ihtiyaç duyarsınız.
Her Planet constant'ıyla farklı data ilişkilendirilmiş olsa da, bazen her constant ile temelde farklı davranış
ilişkilendirmeniz gerekir. Örneğin, temel dört işlem yapan bir hesap makinesini represent eden bir enum type yazdığınızı
ve her constant tarafından represent edilen aritmetik işlemi gerçekleştiren bir method sağlamak istediğinizi varsayalım.
Bunu başarmanın bir yolu, enum değerine göre switch yapmaktır:

```java
// Kendi value'su üzerinde switch yapan enum type – şüpheli
enum Operation {
    PLUS, MINUS, TIMES, DIVIDE;

    // Bu constant tarafından represent edilen aritmetik işlemi yapın.
    public double apply(double x, double y) {
        switch (this) {
            case PLUS:
                return x + y;
            case MINUS:
                return x - y;
            case TIMES:
                return x * y;
            case DIVIDE:
                return x / y;
        }
        throw new AssertionError("Unknown op : " + this);
    }
}
```

Bu kod çalışır, ancak çok güzel değildir. throw statement olmadan compile olmaz çünkü method'un sonu teknik olarak
ulaşılabilir, ancak asla ulaşılmaz `[JLS, 14.21]`. Dahası, kod kırılgandır. Yeni bir enum constant eklerseniz ancak
switch'e karşılık gelen bir case eklemeyi unutursanız, enum yine compile olur ancak yeni işlemi uygulamaya
çalıştığınızda runtime'da hata verir.

Neyse ki, her enum constant ile farklı bir davranış ilişkilendirmenin daha iyi bir yolu vardır: enum type içinde
`abstract apply` method declare edin ve her constant için `constant-specific` class body içinde bunu concrete bir method
olarak override edin. Bu tür method'lara `constant-specific method implementation` denir:

```java
// Constant-specific method implementation'lara sahip enum type
enum Operation {
    PLUS {
        public double apply(double x, double y) {
            return x + y;
        }
    },
    MINUS {
        public double apply(double x, double y) {
            return x - y;
        }
    },
    TIMES {
        public double apply(double x, double y) {
            return x * y;
        }
    },
    DIVIDE {
        public double apply(double x, double y) {
            return x / y;
        }
    };

    abstract double apply(double x, double y);
}
```

Operation'ın ikinci versiyonuna yeni bir constant eklerseniz, apply method'u sağlamayı unutmanız pek olası değildir
çünkü method her constant declaration'ının hemen ardından gelir. Olası olmayan bir şekilde unutursanız, compiler sizi
uyarır çünkü bir enum type içindeki abstract method'lar, tüm constant'larda constract method'larla override edilmelidir.
Constant-specific method implementation'lar, constant-specific data ile combine edilebilir. Örneğin, Operation'ın,
işlemlerle yaygın olarak ilişkilendirilen sembolü döndürmek için toString method'unu override eden bir versiyonu:

```java
// Constant-specific class body ve data içeren enum type
enum Operation {
    PLUS("+") {
        public double apply(double x, double y) {
            return x + y;
        }
    },

    MINUS("-") {
        public double apply(double x, double y) {
            return x - y;
        }
    },
    TIMES("*") {
        public double apply(double x, double y) {
            return x * y;
        }
    },
    DIVIDE("/") {
        public double apply(double x, double y) {
            return x / y;
        }
    };

    private final String symbol;

    Operation(String symbol) {
        this.symbol = symbol;
    }

    @Override
    public String toString() {
        return symbol;
    }

    abstract double apply(double x, double y);
}
```

Gösterilen toString implementasyonu, bu küçük programda da gösterildiği gibi, aritmetik expression'ları yazdırmayı
kolaylaştırır:

```java
public static void main(String[] args) {
    double x = Double.parseDouble(args[0]);
    double y = Double.parseDouble(args[1]);

    for (Operation op : Operation.values()) {
        System.out.printf("%f %s %f = %f%n", x, op, y, op.apply(x, y));
    }
}
```

Bu program `2` ve `4` command line argümanlarıyla çalıştırıldığında aşağıdaki çıktıyı üretir:

```
2.000000 + 4.000000 = 6.000000
2.000000 - 4.000000 = -2.000000
2.000000 * 4.000000 = 8.000000
2.000000 / 4.000000 = 0.500000
```

Enum type'lar, bir constant'ın adını o constant'a çeviren otomatik olarak üretilmiş `valueOf(String)` method'una
sahiptir. Bir enum type'ta toString method'unu override ederseniz, custom string representation'ınını ilgili enum'a geri
çevirmek için bir `fromString` method'u yazmayı düşünün. Aşağıdaki kod (type adı uygun şekilde değiştirilerek), her
constant unique bir string representation taşıdığı sürece herhangi bir enum için işe yarar:

```java
// Constant-specific class body ve data içeren enum type
enum Operation {
    PLUS("+") {
        public double apply(double x, double y) {
            return x + y;
        }
    },

    MINUS("-") {
        public double apply(double x, double y) {
            return x - y;
        }
    },
    TIMES("*") {
        public double apply(double x, double y) {
            return x * y;
        }
    },
    DIVIDE("/") {
        public double apply(double x, double y) {
            return x / y;
        }
    };

    private final String symbol;

    Operation(String symbol) {
        this.symbol = symbol;
    }

    @Override
    public String toString() {
        return symbol;
    }

    abstract double apply(double x, double y);

    // Bir enum type üzerinde fromString method'unu implement etmek
    private static final Map<String, Operation> stringToEnum = Stream.of(values())
            .collect(toMap(Object::toString, e -> e));

    // String için Operation döner, varsa
    public static Optional<Operation> fromString(String symbol) {
        return Optional.ofNullable(stringToEnum.get(symbol));
    }
}
```

Operation constant'larının, enum constant'ları oluşturulduktan sonra çalışan static field initialization'da
`stringToEnum` map'ine eklendiğini unutmayın. Önceki kod, `values()` method'u tarafından döndürülen array üzerinde bir
`stream` kullanır; Java 8'den önce, boş bir `hash map` oluşturur ve `string-to-enum` mapping'lerini map'e ekleyerek
values array üzerinde iterate ederdik; eğer tercih ederseniz, hâlâ bu şekilde yapabilirsiniz. Ancak, her constant'ın
kendi constructor'ından kendisini bir map'e eklemeye çalışmasının işe yaramadığını unutmayın. Bu, bir compilation
error’a yol açar ki bu iyi bir şeydir; çünkü eğer legal olsaydı, runtime'da bir `NullPointerException`’a neden olurdu.
Enum constructor'larının, constant variable'lar hariç olmak üzere, enum'un static field'larına erişmesine izin verilmez.
Bu kısıtlama gereklidir çünkü enum constructor'ları çalıştığında static field'lar henüz initialize edilmemiştir. Bu
kısıtlamanın special bir case'i de, enum constant'larının constructor'larından birbirlerine erişememeleridir.

Ayrıca, `fromString` method'unun `Optional<String>` döndürdüğünü unutmayın. Bu, metoda geçirilen string'in geçerli bir
operation'ı represent etmediğini belirtmesini sağlar ve client'ı bu olasılıkla yüzleşmeye zorlar.

`Constant-specific` method implementation'ların bir dezavantajı, enum constant'ları arasında kod paylaşımını
zorlaştırmalarıdır. Örneğin, bir bordro `(payroll)` package'inde ki haftanın günlerini `(day of the week)` represent
eden bir enum'u düşünün. Bu enum, bir işçinin `(worker)` o gün için temel maaşı (saatlik) ve o gün çalışılan dakika
sayısı verildiğinde işçinin ücretini hesaplayan bir method içerir. Beş iş gününde, normal mesaiyi aşan her çalışma saati
fazla mesai ücreti doğurur; İki hafta sonu gününde ise, yapılan tüm çalışma fazla mesai ücreti sağlar. Switch
statement'ı ile, bu hesaplamayı iki kod bloğuna birden çok case label uygulayarak kolayca yapmak mümkündür:

```java
// Value'suna göre switch yapan ve kod paylaşımı sağlayan enum – şüpheli
enum PayrollDay {
    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY;

    private static final int MINS_PER_SHIFT = 8 * 60;

    int pay(int minutesWorked, int payRate) {
        int basePay = minutesWorked * payRate;

        int overTimePay;
        switch (this) {
            case SATURDAY: // Weekend
            case SUNDAY:
                overTimePay = basePay / 2;
                break;
            default: // Weekday
                overTimePay = minutesWorked <= MINS_PER_SHIFT ? 0 : (minutesWorked - MINS_PER_SHIFT) * payRate / 2;
        }
        return basePay + overTimePay;
    }
}
```

Bu kod, kesinlikle özlüdür ancak bakım açısından tehlikelidir. Enum'a örneğin bir tatil gününü represent eden special
bir value eklendiğini, ancak switch statement'da karşılık gelen bir case eklemeyi unuttuğunuzu varsayalım. Program yine
de compile olur, ancak `pay` method'u tatil günü için işçiye sessizce sıradan bir iş günüyle aynı ücreti öder.

`Constant-specific` method implementasyonlarıyla `pay` hesaplamasını safe bir şekilde gerçekleştirmek için, overtime pay
hesaplamasını her bir constant için kopyalaman gerekir ya da hesaplamayı biri `weekdays`, diğeri `weekend days` için
olmak üzere iki helper method içine taşıyıp, her constant'tan uygun helper method'u invoke etmen gerekir. Her iki
yaklaşım da önemli miktarda boilerplate code ile sonuçlanır, bu da okunabilirliği önemli ölçüde azaltır ve hata
olasılığını artırır.

Boilerplate, `PayrollDay` üzerindeki abstract `overtimePay` method'unun yerine `weekdays` için `overtime` hesaplamasını
gerçekleştiren concrete bir method konularak reduce edilebilir. Böylece yalnızca weekend days bu method'u override etmek
zorunda kalır. Ancak bu, switch statement ile aynı dezavantaja sahip olur: eğer `overtimePay` method'unu override
etmeden başka bir gün eklersen, `weekday` hesaplamasını sessizce inherit etmiş olursun.

Gerçekten istediğin şey, her enum constant eklediğinde bir `overtime pay` stratejisi seçmeye zorlanmaktır. Neyse ki,
bunu başarmanın güzel bir yolu vardır. Fikir, `overtime pay` hesaplamasını `private` bir `nested enum` içine taşımak ve
bu strategy enum'un bir instance'ını `PayrollDay` enum'undaki constructor'a geçmektir. Böylece `PayrollDay` enum,
`overtime pay` hesaplamasını `strategy enum`'a delegate eder ve `PayrollDay` içinde switch statement ya da
`constant-specific` method implementasyonuna ihtiyaç kalmaz. Bu pattern, switch statement'tan daha az özlü `(concise)`
olsa da, daha safe ve daha flexible'dır:

```java
// The strategy enum pattern
enum PayrollDay {
    MONDAY(PayType.WEEKDAY),
    TUESDAY(PayType.WEEKDAY),
    WEDNESDAY(PayType.WEEKDAY),
    THURSDAY(PayType.WEEKDAY),
    FRIDAY(PayType.WEEKDAY),

    SATURDAY(PayType.WEEKEND), SUNDAY(PayType.WEEKEND);

    private final PayType payType;

    PayrollDay(PayType payType) {
        this.payType = payType;
    }

    // The strategy enum type
    enum PayType {
        WEEKDAY {
            int overtimePay(int minsWorked, int payRate) {
                return minsWorked <= MINS_PER_SHIFT ? 0 : (minsWorked - MINS_PER_SHIFT) * payRate / 2;
            }
        },
        WEEKEND {
            int overtimePay(int minsWorked, int payRate) {
                return minsWorked * payRate / 2;
            }
        };

        abstract int overtimePay(int mins, int payRate);

        private static final int MINS_PER_SHIFT = 8 * 60;

        int pay(int minsWorked, int payRate) {
            int basePay = minsWorked * payRate;
            return basePay + overtimePay(minsWorked, payRate);
        }
    }
}
```

Derived.java;

```java
public static void main(String[] args) {
    for (PayrollDay day : values()) {
        System.out.printf("%-10s%d%n", day, day.payType.pay(8 * 60, 1));
    }
}
```

Eğer enum'lar üzerinde switch statement'lar, constant-specific behavior implementasyonu için iyi bir tercih değilse, ne
için iyidirler? Enum'lar üzerindeki switch statement'lar, enum type'larını constant-specific davranışlarla büyütmek için
iyidir. Örneğin, Operation enum senin kontrolünde değil ve her operation için tersini `(inverse)` döndüren bir instance
method'u olmasını istiyorsun. Aşağıdaki static method ile bu etkiyi simüle edebilirsin:

```java
// Eksik bir method'u simüle etmek için bir enum üzerinde switch kullan.
public static Operation inverse(Operation op) {
    switch (op) {
        case PLUS:
            return Operation.MINUS;
        case MINUS:
            return Operation.PLUS;
        case TIMES:
            return Operation.DIVIDE;
        case DIVIDE:
            return Operation.TIMES;
        default:
            throw new AssertionError("Unknown op " + op);
    }
}
```

Bir method enum type içinde yer almıyorsa, kontrolünde olan enum type'larda da bu tekniği kullanmalısın. Method, bazı
kullanımlar için gerekli olabilir ancak genel olarak enum type içine dahil edilmeyi hak edecek kadar faydalı değildir.
Genel olarak konuşmak gerekirse, enum'lar performans açısından int constant'larla karşılaştırılabilir. Enum'ların küçük
bir performans dezavantajı, enum type'ların yüklenmesi ve initialize edilmesinin belli bir zaman ve bellek maliyeti
olmasıdır, ancak bu pratikte fark edilecek düzeyde değildir.

Peki enum'ları ne zaman kullanmalısın? Member'ları compile time'da bilinen bir constant set'ine ihtiyaç duyduğun her
zaman enum kullan. Elbette, bu durum gezegenler, haftanın günleri ve satranç taşları gibi “natural enumerated type”ları
da kapsar. Ancak bu, compile time'da tüm olası değerlerini bildiğin diğer set'leri de kapsar; örneğin bir menüdeki
seçenekler, operation code'ları ve command line flag'leri gibi. Bir enum type içindeki constant set'inin her zaman fixed
kalması gerekmez. Enum feature'u, enum type'ların binary compatible şekilde evrimleşmesine olanak tanıyacak şekilde özel
olarak tasarlanmıştır.

Özetle, enum type'ların int constant'lara karşı avantajları son derece etkileyicidir. Enum'lar daha okunabilir, daha
safe ve daha powerful'dur. Pek çok enum explicit constructor ya da member gerektirmez, ancak bazıları her constant ile
data ilişkilendirilmesinden ve bu data'dan etkilenen davranışlara sahip method'lar sağlamaktan fayda görür. Daha az
sayıda enum, tek bir method ile birden fazla davranışı ilişkilendirmekten fayda görür. Bu nispeten nadir durumda, kendi
değerleri üzerinde switch yapan enum'lara kıyasla constant-specific method'ları tercih et. Bazı enum constant'larının
ortak davranışları paylaşması ancak hepsinin paylaşmaması durumunda `strateji enum` modelini göz önünde bulundurun.