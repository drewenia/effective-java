# Override clone judiciously

Cloneable interface'i, sınıfların cloning'e izin `(permit)` verdiğini duyurmak için bir mixin interface'i olarak
tasarlanmıştır. Maalesef bu amaca hizmet etmiyor. Birincil kusuru, bir clone metodu içermemesi ve Object'in clone
metodu'nun `protected` olmasıdır. Yalnızca Cloneable'ı implement ettiği için bir object üzerinde clone'u reflection'a
başvurmadan invoke edemezsiniz. Reflective bir invocation bile fail olabilir, çünkü object'in accessible bir clone
metodu olduğuna dair hiçbir garanti yoktur. Bu kusura ve diğer birçoklarına rağmen, bu özellik oldukça yaygın olarak
kullanılmaktadır, bu yüzden onu anlamak faydalıdır. Bu madde size iyi çalışan bir clone metodu'nun nasıl implement
edileceğini anlatır, ne zaman uygun olduğunu tartışır ve alternatiflerini sunar.

Peki Cloneable, herhangi bir metot içermediğine göre ne işe yarar? Object'in protected clone implementation'ınının
behavior'ını belirler: Eğer bir sınıf Cloneable'ı implement ediyorsa, Object'in clone metodu object'in `field-by-field`
bir kopyasını döndürür; aksi takdirde, `CloneNotSupportedException` fırlatır. Bu, interface'lerin oldukça atypical bir
kullanımıdır ve taklit `(emulated)` edilmemelidir. Normalde, bir interface'i implement etmek, bir sınıfın client'ları
için neler yapabileceği hakkında bir şeyler söyler. Bu case'de, bir superclass'da ki protected bir metodun behavior'unu
değiştirir.

Spesifikasyonda belirtilmese de, pratikte, Cloneable interface'ini implement eden bir sınıfın düzgün çalışan public
clone metodu sağlaması beklenir. Bunu başarmak için, sınıf ve tüm superclass'ları complex, uygulanamaz `(unenforceable)`
yetersiz belgelenmiş bir protokole uymalıdır. Ortaya çıkan mekanizma kırılgan, tehlikeli ve dil dışıdır: Object'leri bir
constructor call etmeden oluşturur.

clone metodunun general contract'ı zayıftır. İşte Object spesifikasyonundan kopyalanmış hali:

Bu object'in bir kopyasını oluşturur ve döndürür. "Kopya" kelimesinin kesin anlamı, object'in sınıfına depend olabilir.
Genel amaç, herhangi bir `x` object'i için, şu expression'ın geçerli olmasıdır:

```
x.clone() != x
```

true olacaktır, expression;

```
x.clone().getClass() == x.getClass()
```

true olacaktır, ancak bunlar mutlak gereksinimler değildir. Tipik olarak durum böyle olsa da,

```
x.clone().equals(x)
```

true olsa da, bu mutlak bir gereksinim değildir.

Convention'a göre, bu metot tarafından döndürülen object `super.clone` call edilerek elde edilmelidir. Eğer bir sınıf ve
tüm superclass'ları `(Object hariç)` bu convention'a uyarsa, durum şöyle olacaktır:

```
x.clone().getClass() == x.getClass().
```

Convention'a göre, döndürülen object klonlanan object'den bağımsız `(independent)` olmalıdır. Bu bağımsızlığı
`(independence)` sağlamak için, `super.clone` tarafından döndürülen object'in bir veya daha fazla field'ini, döndürmeden
önce değiştirmek gerekebilir.

Bu mekanizma, uygulanamaması dışında, constructor chaining'e belirsiz bir şekilde benzer: Eğer bir sınıfın clone metodu
`super.clone` calling ile değil de bir constructor calling ile elde edilen bir instance döndürürse, compiler şikayet
etmeyecektir. Ancak o sınıfın bir subclass'ı `super.clone` call ederse, ortaya çıkan object yanlış sınıfa sahip olacak
ve subclass'ın clone metodunun düzgün çalışmasını engelleyecektir. Eğer clone'u override eden bir sınıf `final` ise, bu
kural güvenle ignore edilebilir, çünkü endişelenilecek subclass yoktur. Ancak final bir sınıfın `super.clone`'u invoke
etmeyen bir clone metodu varsa, sınıfın Cloneable interface'ini implement etmesi için bir neden yoktur, çünkü Object'in
clone implementation'nının behavior'una dayanmaz.

Diyelim ki, superclass'ı iyi çalışan `(well-behaved)` bir clone metodu sağlayan bir sınıfta Cloneable interface'ini
implement etmek istiyorsunuz. İlk olarak `super.clone`'u çağırın. Geri aldığınız object, orijinalinin tamamen işlevsel
bir kopyası olacaktır. Sınıfınızda tanımlanan herhangi bir field, orijinalinkilerle aynı değerlere sahip olacaktır.
Eğer her field bir primitive value veya immutable bir object'e referans içeriyorsa, döndürülen object tam da ihtiyacınız
olan şey olabilir; bu durumda başka bir işlem yapmaya gerek yoktur. Bu case, örneğin PhoneNumber sınıfı için geçerlidir,
ancak immutable sınıfların asla bir clone metodu sağlamaması gerektiğini unutmayın, çünkü bu sadece savurgan
`(wasteful)` kopyalamayı teşvik edecektir. Bu uyarıyla birlikte, PhoneNumber sınıfı için bir clone metodu şöyle
görünürdü:

```
// Clone method for class with no references to mutable state
@Override 
public PhoneNumber clone() {
    try {
        return (PhoneNumber) super.clone();
    } catch (CloneNotSupportedException e) {
        throw new AssertionError(); // Can't happen
    }
}
```

Bu metodun çalışması için, PhoneNumber sınıfının declaration'ınının Cloneable interface'ini implement ettiğini
belirtecek şekilde değiştirilmesi gerekir. Object'in clone metodu Object döndürmesine rağmen, bu clone metodu
`PhoneNumber` döndürüyor. Bunu yapmak legal ve arzu edilen bir durumdur çünkü Java covariant return type'larını
destekler. Diğer bir deyişle, override edilen bir metodun return type'ı, override edilen metodun return type'ının bir
subclass'ı olabilir. Bu, client'da casting ihtiyacını ortadan kaldırır. `super.clone`'dan dönen sonucu döndürmeden önce
Object'ten PhoneNumber'a dönüştürmemiz gerekir, ancak bu cast'in başarılı olacağı garanti edilir.

`super.clone` çağrısı bir `try-catch` bloğunda yer alır. Bunun nedeni, Object'in clone metodunu
`CloneNotSupportedException` fırlatacak şekilde tanımlamasıdır ki bu da checked exception kategorisindedir.
PhoneNumber'ın Cloneable interface'ini implement etmesinden dolayı, `super.clone` call'unun başarılı olacağını
biliyoruz. Bu boilerplate kodu ihtiyacı, `CloneNotSupportedException`'ın unchecked olması gerektiğini gösteriyor.

Eğer bir object, mutable object'lere referans veren field'ler içeriyorsa, daha önce gösterilen basit clone
implementation'ı felaketle sonuçlanabilir. Örneğin, tekrar Stack sınıfını ele alalım:

```
class Stack{
    private Object[] elements;
    private int size = 0;
    private static final int DEFAULT_INITIAL_CAPACITY = 16;

    public Stack(){
        this.elements = new Object[DEFAULT_INITIAL_CAPACITY];
    }

    public void push(Object e){
        ensureCapacity();
        elements[size++] = e;
    }

    public Object pop(){
        if (size == 0)
            throw new EmptyStackException();
        Object result = elements[--size];
        elements[size] = null; // Eliminate obsolete reference
        return result;
    }

    // Ensure space for at least one more element.
    private void ensureCapacity() {
        if (elements.length == size)
            elements = Arrays.copyOf(elements, 2 * size + 1);
    }
}
```

Diyelim ki bu sınıfı cloneable yapmak istiyorsunuz. Eğer clone metodu `super.clone()` çağrısıyla dönerse, ortaya çıkan
Stack instance'ı `size` field'inde doğru değere sahip olacak, ancak elements field'i orijinalStack instance'ı ile aynı
array'e referans verecektir. Orijinali değiştirmek, kopyadaki sabitleri (invariants) bozacaktır ve bunun tersi de
geçerlidir. Programınızın anlamsız sonuçlar ürettiğini veya bir `NullPointerException` fırlattığını çabucak
göreceksiniz.

Bu durum, Stack sınıfındaki tek constructor'ı calling'in bir sonucu olarak asla meydana gelemezdi. Aslında, clone metodu
bir constructor gibi işlev görür; Orijinal object'e zarar vermediğinden ve clone üzerinde sabitleri (invariants) doğru
şekilde oluşturduğundan emin olmalısınız. Stack sınıfındaki clone metodunun düzgün çalışabilmesi için, stack'in internal
yapısını kopyalaması gerekir. Bunu yapmanın en kolay yolu, elements array'i üzerinde recursively olarak clone'u
call etmektir:

```
class Stack{
    private Object[] elements;
    private int size = 0;
    private static final int DEFAULT_INITIAL_CAPACITY = 16;

    public Stack(){
        this.elements = new Object[DEFAULT_INITIAL_CAPACITY];
    }

    public void push(Object e){
        ensureCapacity();
        elements[size++] = e;
    }

    public Object pop(){
        if (size == 0)
            throw new EmptyStackException();
        Object result = elements[--size];
        elements[size] = null; // Eliminate obsolete reference
        return result;
    }

    // Ensure space for at least one more element.
    private void ensureCapacity() {
        if (elements.length == size)
            elements = Arrays.copyOf(elements, 2 * size + 1);
    }

    @Override
    public Stack clone(){
        try{
            Stack result = (Stack) super.clone();
            result.elements = elements.clone();
            return result;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
```

`elements.clone()` sonucunu `Object[]`'e dönüştürmek zorunda değiliz. Bir array üzerinde clone'u çağırmak, runtime
ve compile time type'ları clone'layan array ile aynı olan bir array döndürür. Bu, bir array'i kopyalamak için tercih
edilen yöntemdir. Aslında, array'ler `clone` özelliğinin tek ikna edici kullanım alanıdır.

Şunu da belirtmek gerekir ki, `elements` field'i final olsaydı önceki çözüm işe yaramazdı, çünkü clone'un bu field'e
yeni bir değer ataması yasaklanmış olurdu. Bu temel bir problem: tıpkı serialization gibi, Cloneable architecture de
mutable object'lere referans veren final field'lerin normal kullanımıyla uyumsuzdur; ancak mutable object'lerin bir
object ile clone'u arasında güvenli bir şekilde paylaşılabileceği durumlar hariç. Bir sınıfı cloneable yapmak için, bazı
field'lerden final modifier'larını kaldırmak gerekebilir.

Yalnızca recursively olarak clone'u call etmek her zaman yeterli değildir. Örneğin, internals consist bir bucket
array'inden oluşan bir hash table için bir clone metodu yazdığınızı varsayın. Bu bucket'ların her biri, key-value
pair'lerinin linked list'inde ki ilk entry'i referans alıyor. Performans için, sınıf, internally olarak
`java.util.LinkedList` kullanmak yerine kendi light-weigth singly linked listesini implement eder:

```
public class HashTable implements Cloneable {
    private Entry[] buckets = ...;
    private static class Entry {
        final Object key;
        Object value;
        Entry next;
        
        Entry(Object key, Object value, Entry next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }
    ... // Remainder omitted
}
```

Diyelim ki, Stack sınıfında yaptığımız gibi sadece bucket array'ini recursively olarak clone'ladınız:

```
// Broken clone method - results in shared mutable state!
@Override 
public HashTable clone() {
    try {
        HashTable result = (HashTable) super.clone();
        result.buckets = buckets.clone();
        return result;
    } catch (CloneNotSupportedException e) {
        throw new AssertionError();
    }
}
```

Clone'un kendine ait bir bucket array'i olmasına rağmen, bu array orijinaliyle aynı linked list'lere referans verir, bu
da hem clone'da hem de orijinalde kolayca belirsiz `(non-determenistic)` behavior'a neden olabilir. Bu sorunu çözmek
için, her bir bucket'ı oluşturan linked list'i kopyalamanız gerekecek. İşte yaygın bir yaklaşım:

```
// Recursive clone method for class with complex mutable state
public class HashTable implements Cloneable {
    private Entry[] buckets = ...;
    
    private static class Entry {
        final Object key;
        Object value;
        Entry next;
    
        Entry(Object key, Object value, Entry next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }
        
        // Recursively copy the linked list headed by this Entry
        Entry deepCopy() {
            return new Entry(key, value, next == null ? null : next.deepCopy());
        }
    }

    @Override public HashTable clone() {
        try {
            HashTable result = (HashTable) super.clone();
            result.buckets = new Entry[buckets.length];
            for (int i = 0; i < buckets.length; i++)
                if (buckets[i] != null)
                result.buckets[i] = buckets[i].deepCopy();
                return result;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
    ... // Remainder omitted
}
```

Private class `HashTable.Entry`, "deep copy" metodunu desteklemek için geliştirildi. HashTable'daki clone metodu uygun
boyutta yeni bir buckets array'i tahsis eder ve orijinal buckets array'i üzerinde iterate ederek, nonempty her
bir bucket'ı deep-copying kopyalar. Entry sınıfındaki `deepCopy` metodu, girdinin `(entire)` başını oluşturan linked
list'in tamamını kopyalamak için kendini recursively olarak invoke eder. Bu teknik hoş ve bucket'lar çok uzun
olmadığında iyi çalışsa da, linked bir list'i clone'lamak için iyi bir yöntem değildir çünkü listedeki her element için
bir stack frame consume eder. Eğer liste uzunsa, bu kolayca bir stack overflow'a neden olabilir. Bunun olmasını önlemek
için, `deepCopy`'deki recursion'ı iteration ile değiştirebilirsiniz:

```
public class HashTable implements Cloneable {
    private Entry[] buckets = ...;
    
    private static class Entry {
        final Object key;
        Object value;
        Entry next;
    
        Entry(Object key, Object value, Entry next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }
        
        // Iteratively copy the linked list headed by this Entry
        Entry deepCopy() {
            Entry result = new Entry(key, value, next);
            for (Entry p = result; p.next != null; p = p.next)
                p.next = new Entry(p.next.key, p.next.value, p.next.next);
            return result;
        }
    }

    @Override public HashTable clone() {
        try {
            HashTable result = (HashTable) super.clone();
            result.buckets = new Entry[buckets.length];
            for (int i = 0; i < buckets.length; i++)
                if (buckets[i] != null)
                result.buckets[i] = buckets[i].deepCopy();
                return result;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
    ... // Remainder omitted
}
```

Complex mutable object'leri clone'lamak için son bir yaklaşım, `super.clone`'u call etmek, ortaya çıkan object'de ki tüm
field'leri initial state'lerine set etmek ve ardından orijinal object'in state'ini regenerate için higher-level
metodları çağırmaktır. HashTable örneğimizde, buckets field'i yeni bir bucket array'ine initialize edilir ve
`put(key, value)` metodu (gösterilmemiştir), clone'lanan hash table'ında ki her key-value mapping için çağrılır. Bu
yaklaşım, clone'un iç kısımlarını `(innards)` directly manipüle eden bir metoda göre daha yavaş çalışsa da, genellikle
basit ve oldukça zarif bir clone metodu ortaya çıkarır. Bu yaklaşım temiz olsa da, Cloneable mimarisinin temelini
oluşturan `field-by-field` object kopyalamayı körü körüne overwrite ettiği için tüm bu mimariye aykırıdır.

Bir constructor gibi, bir clone metodu da yapım aşamasındaki (under construction) clone üzerinde overridable bir metodu
asla invoke etmemelidir. Eğer clone, bir subclass'da override edilmiş bir metodu çağırırsa, bu metod, subclass'ın
clone'da ki state'ini düzeltme şansı bulamadan önce çalışacaktır; bu da büyük olasılıkla clone'da ve orijinalde
bozulmalara yol açabilir. Bu nedenle, önceki paragrafta bahsedilen `put(key, value)` metodu ya final ya da private
olmalıdır. (Eğer private ise, muhtemelen final olmayan public bir metodun "helper metodu"dur.)

Ortaya çıkan hash function daha hızlı çalışabilir; ancak kalitesiz olması, hash table'larının performansını kullanılmaz
hale getirecek düzeyde düşürebilir. public clone metodları `throws` clause içermemelidir, çünkü checked exception’ları
fırlatmayan metodlar kullanımı daha kolaydır.

Bir sınıfı inheritance için tasarlarken iki seçeneğiniz vardır, ancak hangisini seçerseniz seçin, sınıf Cloneable
interface'ini implement etmemelidir. Object'in behavior'unu taklit `(mimic)` ederek, `CloneNotSupportedException`
fırlattığı belirtilen, düzgün çalışan protected bir clone metodu implement edebilirsiniz. Bu, subclass'lara, sanki
doğrudan Object'i extend etmiş gibi, Cloneable'ı implement etme veya etmeme özgürlüğü verir. Alternatif olarak, çalışan
bir clone metodu implement etmemeyi ve subclass'ların bir tane implement etmesini engellemeyi seçebilirsiniz, aşağıdaki
degenerate clone implementasyonunu sağlayarak:

```
// clone method for extendable class not supporting Cloneable
@Override
protected final Object clone() throws CloneNotSupportedException {
    throw new CloneNotSupportedException();
}
```

Belirtilmesi gereken bir ayrıntı daha var. Eğer Cloneable'ı implement eden thread-safe bir sınıf yazıyorsanız, clone
metodunuzun tıpkı diğer metodlar gibi uygun şekilde synchronized edilmesi gerektiğini unutmayın. Object'in clone metodu
synchronized değildir, bu yüzden implementation'ı başka açılardan tatmin edici olsa bile, `super.clone()` döndüren
synchronized bir clone metodu yazmanız gerekebilir.

Özetlemek gerekirse, Cloneable'ı implement eden tüm sınıflar, clone metodunu return type'ı sınıfın kendisi olan public
bir metotla override etmelidir. Bu metot önce `super.clone`'u call etmeli, ardından düzeltilmesi gereken tüm field'leri
düzeltmelidir. Tipik olarak bu, object'in internal "deep structure" oluşturan herhangi bir mutable object'i kopyalamak
ve clone'un bu object'lere olan referanslarını, kopyalarına olan referanslarla değiştirmek anlamına gelir. Bu internal
kopyalar genellikle recursively olarak clone calling ile yapılabilse de, bu her zaman en iyi yaklaşım değildir. Eğer
sınıf yalnızca primivite field'ler veya immutable object'lere referanslar içeriyorsa, muhtemelen hiçbir field'in
düzeltilmesi gerekmez. Bu kuralın istisnaları vardır. Örneğin, bir seri numarası veya başka bir unique ID represent
eden bir field, primitive veya immutable olsa bile düzeltilmesi gerekecektir.

Tüm bu complexity gerçekten gerekli mi? Nadiren. Eğer halihazırda Cloneable'ı implement eden bir sınıfı extend
ediyorsanız, iyi çalışan bir clone metodu implement etmekten başka seçeneğiniz pek kalmaz. Aksi takdirde, genellikle
object copying için alternatif bir yöntem sağlamak daha iyidir. Object copying için daha iyi bir yaklaşım, bir copy
constructor veya copy factory sağlamaktır. Bir copy constructor, basitçe, constructor'ı içeren sınıfın tipinde tek bir
argüman alan bir constructor'dır, örneğin:

```
// Copy constructor
public Yum(Yum yum) { 
    ... 
};
```

Bir copy factory, bir copy constructor'ın static factory karşılığıdır:

```
// Copy factory
public static Yum newInstance(Yum yum) { 
    ... 
};
```

Copy constructor yaklaşımı ve onun static factory varyantı, `Cloneable/clone`'a göre birçok avantaja sahiptir: Bunlar,
riske açık dil dışı bir object oluşturma mekanizmasına dayanmaz; bunlar, zayıf belgelenmiş kurallara uygulanamaz bir
bağlılık talep etmez; Bunlar, final field'lerin doğru kullanımıyla çelişmez; bunlar gereksiz checked exception'lar
fırlatmaz; ve cast gerektirmezler.

Dahası, bir copy constructor veya factory, type'ı sınıf tarafından implement edilen bir interface olan bir argüman
alabilir. Örneğin, konvansiyonel olarak tüm genel amaçlı collection implementation'ları, argümanı Collection veya Map
tipinde olan bir constructor sağlarlar. Interface based copy constructor'lar ve factory'ler – daha doğru bir ifadeyle
conversion constructor'ları ve conversion factory'leri olarak bilinirler – client'in, kopyanın implementasyon type'ını
orijinalin implementasyon type'ını kabul etmek zorunda kalmadan seçmesine olanak tanır. Örneğin, elinizde bir HashSet
olan `s` olduğunu ve bunu bir TreeSet olarak kopyalamak istediğinizi varsayın. clone metodu bu işlevselliği sunamaz,
ancak bir conversion constructor'ı ile çok kolaydır: `new TreeSet<>(s)`.

Cloneable ile ilişkili tüm sorunlar göz önüne alındığında, yeni interface'ler onu extend etmemeli ve yeni extend
edilebilir sınıflar da onu implement etmemelidir. final sınıfların Cloneable'ı implement etmesi daha az zararlı olsa da,
bu durum bir performans optimizasyonu olarak görülmeli ve sadece haklı olduğu nadir durumlar için saklanmalıdır. Kural
olarak, kopyalama işlevi en iyi constructor'lar veya factory'ler aracılığıyla sağlanır. Bu kuralın dikkat çekici bir
istisnası, en iyi clone metodu ile kopyalanan array'lerdir.