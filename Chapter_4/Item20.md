# Prefer interfaces to abstract classes

Java'nın multiple implementation'a izin veren bir tür tanımlamak için iki mekanizması vardır: interface'ler ve abstract
class'lar. Java 8'de interface'ler için default method'ların `[JLS 9.4.3]` kullanıma sunulmasından bu yana, her iki
mekanizma da bazı instance method'ları için implementation'lar sağlamanıza olanak tanır.

> `[JLS 9.4.3]` Interface Method Body

Bir default method'un bir block body'si vardır. Bu kod bloğu, bir sınıf interface'i implement ettiğinde ancak method'un
kendi implementation'ını sağlamadığında, method'un bir implementation'ını sunar. Bir private veya static interface
method'u da bir block body'ye sahiptir, bu da method'un implementation'ını sağlar. Bir interface method declaration'ı
abstract (explicitly veya implicitly olarak) ve body'si bir block ise compile time hatasıdır. Bir interface method
declaration'ı `default`, `private` veya `static` ise ve body'si noktalı virgül ise compile time hatasıdır. Eğer bir
method'un bir return type'ı olduğu declare edilmişse, method'un body'si normal şekilde tamamlanabiliyorsa bir compile
time hatası oluşur.

> End of documentation

Temel bir fark, abstract class tarafından tanımlanan türü implement etmek için bir sınıfın abstract class'ın bir
subclass'ı olması gerektiğidir. Java yalnızca single inheritance'a izin verdiğinden, abstract class'lar üzerindeki bu
kısıtlama, onların type definition olarak kullanımını ciddi şekilde sınırlar. Gerekli tüm method'ları define eden ve
general contract'a uyan herhangi bir sınıf, sınıf hiyerarşisinde nerede yer alırsa alsın, bir interface'i implement
etmeye izinlidir.

Mevcut sınıflar, yeni bir interface'i implement etmek için kolayca uyarlanabilir. Yapmanız gereken tek şey, eğer henüz
mevcut değillerse, gerekli method'ları eklemek ve sınıf declaration'ına bir implements clause eklemektir. Örneğin,
platforma Comparable, Iterable ve Autocloseable interface'leri eklendiğinde birçok mevcut sınıf bu interface'leri
implement etmek üzere uyarlandı. Mevcut sınıflar genellikle yeni bir abstract class'ı extend etmek üzere uyarlanamaz.
Eğer iki sınıfın aynı abstract class'ı extend etmesini istiyorsanız, onu her iki sınıfın da atası `(ancestor)` olduğu
type hiyerarşisinde yukarıya yerleştirmeniz gerekir. Maalesef bu durum, type hierarchy'sinde büyük yan `(collateral)`
hasara neden olabilir; yeni abstract class'ın tüm subclass'larını, uygun olup olmamasına bakılmaksızın onu subclass
yapmaya zorlar.

Interface'ler, mixin'leri tanımlamak için idealdir. Kabaca söylemek gerekirse, bir mixin, bir sınıfın "primary type'ına"
ek olarak implement edebileceği, optional bir davranış sağladığını belirtmek için kullanılan bir type'tır. Örneğin,
Comparable bir mixin interface'idir ve bir sınıfın instance'larının karşılıklı olarak comparable olan diğer object'lere
göre sıralandığını `(ordered)` belirtmesini sağlar. Böyle bir interface'e mixin denir çünkü optional functionality'nin,
type'ın primary functionality'sine "mixed edilmesine" olanak tanır. Abstract class'lar, mevcut sınıflara retrofit
edilememelerinin aynı nedeni yüzünden mixin tanımlamak için kullanılamaz: bir sınıfın birden fazla parent'i olamaz ve
mixin'i yerleştirmek için sınıf hiyerarşisinde makul bir yer yoktur.

Interface'ler, nonhierarchical type framework'lerinin oluşturulmasına olanak tanır. Type hierarchy'leri bazı şeyleri
düzenlemek için harikadır, ancak diğer şeyler katı bir hiyerarşiye düzenli bir şekilde uymaz. Örneğin, bir `singer`
represent eden bir interface'imiz ve bir `song writer` represent eden başka bir interface'imiz olduğunu varsayalım:

```
public interface Singer {
    AudioClip sing(Song s);
}

public interface Songwriter {
    Song compose(int chartPosition);
}
```

Gerçek hayatta bazı şarkıcılar `(singer)` aynı zamanda şarkı yazarıdır `(songwriter)`. Bu type'ları tanımlamak için
abstract class'lar yerine interface'ler kullandığımız için, tek bir sınıfın hem Singer hem de Songwriter'ı implement
etmesi tamamen uygundur. Hatta hem Singer hem de Songwriter'ı extend eden ve bu kombinasyona uygun yeni method'lar
ekleyen üçüncü bir interface tanımlayabiliriz:

```
public interface SingerSongwriter extends Singer, Songwriter {
    AudioClip strum();
    void actSensitive();
}
```

Her zaman bu düzeyde esnekliğe ihtiyacınız olmaz, ancak olduğunda, interface'ler bir cankurtarandır. Alternatif olarak,
desteklenen her attributes kombinasyonu için ayrı bir sınıf içeren şişkin `(bloated)` bir sınıf hiyerarşisi bulunur.
Type sisteminde `n` attribute varsa, desteklemeniz gerekebilecek `2n` olası kombinasyon vardır. İşte buna combinatorial
explosion denir. Şişmiş `(bloated)` sınıf hiyerarşileri, argümanlarının type'ından başka bir farkı olmayan birçok method
içeren şişmiş `(bloated)` sınıflara yol açabilir, çünkü ortak davranışları yakalamak için sınıf hiyerarşisinde hiçbir
type yoktur.

Interface'ler, wrapper class idiom'u aracılığıyla güvenli, güçlü functionality geliştirmeleri sağlar. Eğer type'ları
define etmek için abstract class'lar kullanırsanız, functionality eklemek isteyen programcıyı inheritance'tan başka
alternatifsiz bırakırsınız. Ortaya çıkan sınıflar, wrapper class'lardan daha az güçlü ve daha kırılgandır. Başka
interface method'ları açısından bir interface method'unun bariz bir implementation'ı olduğunda, programcılara default
method şeklinde implementation yardımı sağlamayı düşünün. Bu tekniğe bir örnek için, sayfa 104'teki removeIf method'una
bakınız. Eğer default method'lar sağlıyorsanız, bunları inheritance için `@implSpec Javadoc tag'ini kullanarak
belgelediğinizden emin olun.

Default method'larla sağlayabileceğiniz implementation yardımı konusunda sınırlar vardır. Birçok interface equals ve
hashCode gibi Object method'larının davranışını belirtse de, bunlar için default method'lar sağlamanıza izin verilmez.
Ayrıca, interface'lerin instance field'lar veya nonpublic static member'lar (private static method'lar hariç) içermesine
izin verilmez. Son olarak, kontrol etmediğiniz bir interface'e default method'lar ekleyemezsiniz.

Ancak, bir interface ile birlikte abstract skeletal implementation class sağlayarak interface'lerin ve abstract
class'ların avantajlarını birleştirebilirsiniz. Interface, type'ı define eder, belki bazı default method'lar sağlar;
skeletal implementation class ise geri kalan `non-primitive` interface method'larını primitive interface method'larının
üzerine implement eder. Bir skeletal implementation'ı extend etmek, bir interface'i implement etmenin iş yükünün çoğunu
ortadan kaldırır. Bu, Template Method pattern'ıdır.

> Template Method Pattern Example

DataProcessor.java;

```
public interface DataProcessor {
    default void process() {
        readData();
        processData();
        writeData();
    }

    void readData();
    void processData();
    void writeData();
}
```

CSVDataProcessor.java;

```
class CSVDataProcessor implements DataProcessor{
    @Override
    public void readData() {
        System.out.println("Reading CSV data");
    }

    @Override
    public void processData() {
        System.out.println("Processing CSV data");
    }

    @Override
    public void writeData() {
        System.out.println("Writing CSV data");
    }
}
```

Derived class;

```
DataProcessor dp = new CSVDataProcessor();
dp.process();
```

> End of example

Geleneksel olarak, skeletal implementation class'lara `AbstractInterface` adı verilir; burada Interface, implement
ettikleri interface'in adıdır. Örneğin, Collections Framework her main collection interface'iyle birlikte bir skeletal
implementation sağlar: `AbstractCollection, AbstractSet, AbstractList, AbstractMap`. Tartışmalı bir şekilde, onlara
SkeletalCollection, SkeletalSet, SkeletalList ve SkeletalMap demek mantıklı olabilirdi, ancak Abstract geleneği artık
sağlam bir şekilde yerleşmiştir. Doğru tasarlandığında, skeletal implementation'lar (ister ayrı bir abstract class
olsun, ister yalnızca bir interface üzerindeki default method'lardan oluşsun) programcılar için bir interface'in kendi
implementation'larını sağlamayı çok kolaylaştırabilir. Örneğin, işte AbstractList üzerine kurulu, eksiksiz, fully
functional bir List implementation'ı içeren bir static factory method:

```
// Skeletal implementation üzerine inşa edilmiş concrete implementation.
static List<Integer> intArrayAsList(int[] a) {
    Objects.requireNonNull(a);

    // Diamond operator yalnızca Java 9 ve sonraki sürümlerde yasaldır.
    // Eğer daha önceki bir sürümü kullanıyorsanız, <Integer> belirtin.
    return new AbstractList<>() {
        @Override
        public Integer get(int index) {
            return a[index]; // Autoboxing
        }

        @Override
        public Integer set(int index, Integer val) {
            int oldVal = a[index];
            a[index] = val; // Auto-unboxing
            return oldVal; // Autoboxing
        }

        @Override
        public int size() {
            return a.length;
        }
    };
}
```

Derived class;

```
public static void main(String[] args) {
    int[] a = new int[10];
    for (int i = 0; i < a.length; i++) {
        a[i] = i;
    }
    List<Integer> integers = intArrayAsList(a);
    Collections.shuffle(integers);
    System.out.println(integers); // => [4, 7, 6, 3, 5, 1, 8, 9, 0, 2]
}
```

Bir List implementation'ının sizin için yaptığı tüm işleri göz önüne aldığınızda, bu örnek skeletal implementation'ların
gücünün etkileyici bir göstergesidir. Bu örnek aynı zamanda, bir int array'inin Integer instance'larından oluşan bir
list olarak görülmesini `(viewed)` sağlayan bir Adapter'dır. int value'ları ile Integer instance'ları arasındaki tüm bu
back forward translation'lar `(boxing ve unboxing)` nedeniyle performansı pek iyi değildir. Unutulmamalıdır ki,
implementation bir `anonymous class` form'undadır.

Skeletal implementation class'ların güzelliği, abstract class'ların type definition'ları olarak hizmet ederken
implement ettiği katı kısıtlamaları dayatmadan, abstract class'ların tüm implementation yardımını sağlamalarıdır. Eğer
bir sınıf skeletal implementation'ı extend edemiyorsa, sınıf her zaman interface'i doğrudan implement edebilir. Sınıf,
interface'in kendisinde bulunan tüm default method'lardan yine de faydalanır. Dahası, skeletal implementation yine de
implementor'ın görevine yardımcı olabilir. Interface'i implement eden sınıf, interface method invocation'larını,
skeletal implementation'ı extend eden private inner class'ın içerdiği bir instance'a yönlendirebilir. Simulated multiple
inheritance olarak bilinen bu teknik, Madde 18'de tartışılan wrapper class idiom'uyla yakından ilişkilidir. Multiple
inheritance'ın faydalarının çoğunu sağlarken, tuzaklarından kaçınır.

> Simulated multiple inheritance example

Diyelim ki, geometrik şekilleri represent eden ve alan ile çevre hesaplama method'ları olan Shape adında bir
interface'imiz var. Bu method'lar için default implementation'lar sağlayan bir skeletal implementasyon class'ı olan
`AbstractShape` oluşturacağız ve ardından `Shape` interface'ini directly implement eden ancak method call'larını inner
class'a forwarding ile `AbstractShape`'teki kodu yeniden kullanan bir Circle class'ımız olacak.

Shape.java;

```
// Interface representing a geometric shape
interface Shape{
    double getArea();
    double getPerimeter();
}
```

AbstractShape.java;

```
// Default implementation'lar sağlayan skeletal implementation class'ı
abstract class AbstractShape implements Shape{

    // Tüm Shape'ler için common metot implementation'ları
    @Override
    public double getArea() {
        return 0.0D;
    }

    @Override
    public double getPerimeter() {
        return 0.0D;
    }
}
```

Circle.java;

```
// Shape'i directly implement eden, bir Circle'ı represent eden class
class Circle implements Shape {

    private final double radius;

    public Circle(double radius) {
        this.radius = radius;
    }

    // Skeletal implementation'ı extends eden inner class
    private class CircleImpl extends AbstractShape {
        @Override
        public double getArea() {
            return Math.PI * radius * radius;
        }

        @Override
        public double getPerimeter() {
            return 2 * Math.PI * radius;
        }
    }

    // Metot call'larını inner CircleImpl instance'ina forward et
    private final CircleImpl circleImpl = new CircleImpl();

    @Override
    public double getArea() {
        return circleImpl.getArea();
    }

    @Override
    public double getPerimeter() {
        return circleImpl.getPerimeter();
    }
}
```

Derived class;

```
public static void main(String[] args) {
    Shape circle = new Circle(5.0D);
    System.out.println(circle.getArea()); // => 78.53981633974483
    System.out.println(circle.getPerimeter()); // => 31.41592653589793
}
```

Bu örnekte, AbstractShape, Shape interface'inin bir skeletal implementation'ı olarak hizmet verir ve `getArea()` ile
`getPerimeter()` için default implementation'lar sağlar. Circle sınıfı Shape interface'ini directly implement eder,
ancak aynı zamanda `AbstractShape`'i extend eden ve Circle'lara özel `getArea()` ve `getPerimeter()`
implementation'larını sağlayan bir inner class'ı olan `CircleImpl`'e sahiptir. Metot call'larını inner `CircleImpl`
instance'ına forwarding ile, `Circle` sınıfı `AbstractShape`'i directly extend etmek zorunda kalmadan kodunu etkili bir
şekilde yeniden kullanır. Bu, Circle sınıfının skeletal implementation tarafından sağlanan implementation yardımından
faydalanırken, gerektiğinde diğer interface'leri implement etmekte veya farklı sınıfları extend etmekte serbest olmasını
sağlar. Buna Java'da simulated multiple inheritance denir ve görebileceğiniz gibi, yalnızca polimorfizm ve inheritance
kullanmaktan çok daha esnektir.

> End of example

Bir skeletal implementation yazmak, biraz sıkıcı olsa da nispeten basit bir süreçtir. Öncelikle, interface'i inceleyin
ve diğerlerinin hangi primitive metotlar cinsinden implement edilebileceğine karar verin. Bu primitive'ler, skeletal
implementation'ınızdaki abstract metotlar olacaktır. Daha sonra, doğrudan primitive'ler üzerine implement edilebilecek
tüm metotlar için interface'de default metotlar sağlayın. Ancak unutmayın ki `equals` ve `hashCode` gibi Object
metotları için default metotlar sağlayamazsınız. Primitive'ler ve default metotlar interface'i kapsıyorsa, işiniz bitti
ve bir skeletal implementation sınıfına ihtiyacınız kalmaz. Aksi takdirde, interface'i implement etmek üzere declare
edilmiş, kalan tüm interface metotlarının implementation'ları ile bir sınıf yazın. Sınıf, task'e uygun tüm `nonpublic`
field'leri ve metotları içerebilir.

Basit bir örnek olarak, `Map.Entry` interface'ini düşünün. Aşikar primitive'ler `getKey`, `getValue` ve (optionally)
`setValue`'dur. Interface, `equals` ve `hashCode`'un davranışını belirtir ve primitive'ler açısından toString'in aşikar
bir implementation'ı vardır. Object metotları için default implementation'lar sağlamanıza izin verilmediğinden, tüm
implementation'lar skeletal implementation sınıfına yerleştirilir:

```
// skeletal implementation class
abstract class AbstractMapEntry<K, V> implements Map.Entry<K, V> {

    // Modifiable bir map'de ki Entry'ler bu metodu override etmelidir.
    @Override
    public V setValue(V value) {
        throw new UnsupportedOperationException();
    }

    // Map.Entry.equals'ın general contract'ını implement eder.
    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Map.Entry))
            return false;
        Map.Entry<?, ?> e = (Map.Entry) o;
        return Objects.equals(e.getKey(), getKey())
                && Objects.equals(e.getValue(), getValue());
    }

    // Map.Entry.hashCode'un general contract'ını implement eder.
    @Override
    public int hashCode() {
        return Objects.hashCode(getKey()) ^ Objects.hashCode(getValue());
    }

    @Override
    public String toString() {
        return getKey() + "=" + getValue();
    }
}
```

Bu skeletal implementation'ın `Map.Entry` interface'inde veya bir subinterface olarak implement edilemeyeceğini
unutmayın, çünkü default metotların equals, hashCode ve toString gibi Object metotlarını override etmesine izin
verilmez.

Skeletal implementation'lar inheritance için tasarlandığından, Item 19'daki tüm tasarım ve dokümantasyon yönergelerine
uymalısınız. Kısalık adına, önceki örnekten dokümantasyon comment'leri çıkarılmıştır, ancak ister bir interface'de ki
default metotlardan ister ayrı bir abstract sınıftan oluşsun, iyi dokümantasyon bir skeletal implementation'da
kesinlikle esastır. Skeletal implementation'ın küçük bir varyantı, `AbstractMap.SimpleEntry`'nin örneklendirdiği simple
implementation'dır. Bir simple implementation, bir interface'i implement etmesi ve inheritance için tasarlanmış olması
bakımından bir skeletal implementation gibidir, ancak abstract olmamasıyla farklılık gösterir: Bu, mümkün olan en basit
çalışan implementation'dır. Olduğu gibi kullanabilir veya koşullara göre subclass'landırabilirsiniz.

> AbstractMap.SimpleEntry Explanation

`AbstractMap.SimpleEntry` (tam adıyla `java.util.AbstractMap.SimpleEntry`) Java Collections Framework içinde yer alan,
`Map.Entry` interface'inin basit bir implementation'ıdır. `Map.Entry`'nin bir key-value pair'ini represent eden bir
object olduğu yerlerde kullanılır. `AbstractMap.SimpleEntry`, özellikle `Map.Entry` object'leri ile çalışırken kolaylık
sağlamak için tasarlanmış bir sınıftır. Genellikle `Map` dışında `Map.Entry` instance'ları oluşturmak istendiğinde veya
bir Map'in `entrySet()` metodundan dönen entry'ler üzerinde işlem yapılırken kullanılır.

Mutable: key sabittir ancak value değiştirilebilir `(setValue() ile)`.

```
Map.Entry<String, Integer> entry = new AbstractMap.SimpleEntry<>("apple", 10);

System.out.println(entry.getKey()); // => apple
System.out.println(entry.getValue()); // => 10

entry.setValue(20);
System.out.println(entry.getValue()); // => 20
```

SimpleEntry sınıfı mutable olduğu için dikkatli kullanılmalıdır. Örneğin, `HashMap.entrySet()` ile alınan entry'ler
değiştirildiğinde Map etkilenmeyebilir.

> End of explanation

> AbstractMap.SimpleImmutableEntry Explanation

Bu sınıf, Map.Entry interface'inin immutable bir implementasyonudur. SimpleImmutableEntry, bir Map.Entry instance'ını
oluşturup daha sonra immutable hale getirmek için kullanılır. `setValue()` metodu desteklenmez ve çağrıldığında
`UnsupportedOperationException` fırlatır.

```
public class Main {
    public static void main(String[] args) {
        Map.Entry<String, Integer> entry =
            new AbstractMap.SimpleImmutableEntry<>("banana", 15);

        System.out.println(entry.getKey());   // banana
        System.out.println(entry.getValue()); // 15

        entry.setValue(30);  // Hata fırlatır: UnsupportedOperationException
    }
}
```

Key-value pair'lerinin değiştirilmesini istemediğiniz durumlarda. Value'ların yanlışlıkla veya bilinçli olarak
değiştirilmesini önlemek istiyorsanız. Immutable Map.Entry object'leri ile çalışmanız gereken API'lerde (örneğin bir
cache veya history yapısında) kullanılırlar.

> End of explanation

Özetlemek gerekirse, bir interface genellikle birden fazla implementation'a izin veren bir type'ı define etmenin en iyi
yoludur. Eğer önemsiz olmayan bir interface'i export ediyorsanız, onunla birlikte bir skeletal implementation sağlamayı
şiddetle düşünmelisiniz. Mümkün olduğu ölçüde, skeletal implementation'ı interface'de ki default metotlar aracılığıyla
sağlamalısınız ki interface'i implement eden herkes onu kullanabilsin. Bununla birlikte, interface'lerde ki kısıtlamalar
tipik olarak bir skeletal implementation'ın abstract bir sınıf şeklinde olmasını zorunlu kılar.