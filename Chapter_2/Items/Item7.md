# Eliminate obsolete object references

Eğer manuel memory management olan `C` veya `C++` gibi bir dilden, garbage-collected bir dil olan Java’ya geçtiysen,
programcı olarak işin, object'lerin işin bittiğinde otomatik olarak geri kazanılması `(reclaimed)` sayesinde çok daha
kolaylaşmıştır. İlk deneyimlendiğinde neredeyse sihir gibi görünür. Memory management hakkında düşünmene gerek yok
izlenimi kolayca oluşabilir, ancak bu tamamen doğru değildir.

Aşağıdaki basit stack implementasyonunu düşünelim:

```
// Can you spot the "memory leak"?
class Stack {
    private Object[] elements;
    private int size = 0;
    private static final int DEFAULT_INITIAL_CAPACITY = 16;

    public Stack() {
        elements = new Object[DEFAULT_INITIAL_CAPACITY];
    }

    public void push(Object e) {
        ensureCapacity();
        elements[size++] = e;
    }

    public Object pop(){
        if (size == 0)
            throw new EmptyStackException();
        return elements[size--];
    }

    /* Array'in büyümesi (grow) gerektiğinde kapasiteyi yaklaşık iki katına çıkararak en az bir element için yer aç. */
    private void ensureCapacity() {
        if (elements.length == size)
            elements = Arrays.copyOf(elements, 2 * size + 1);
    }
}
```

Bu programda açıkça yanlış görünen bir şey yok. Programı kapsamlı şekilde test edebilirsin ve tüm testlerden başarıyla
geçer, ancak gizli bir problem vardır. Geniş anlamda, programda “memory leak” vardır; bu, artan garbage collector
aktivitesi veya büyüyen bellek kullanımı nedeniyle performans düşüşü olarak sessizce kendini gösterebilir. Extreme
case'lerde, bu tür memory leak’ler disk paging’e ve hatta `OutOfMemoryError` ile programın çökmesine yol açabilir; ancak
bu tür hatalar nispeten nadirdir.

Peki memory leak nerede? Bir stack büyüyüp sonra küçüldüğünde, stack’ten çıkarılan (`pop edilen`) object'ler, programda
artık onlara referans olmasa bile garbage collected tarafından toplanmazlar. Bunun sebebi, stack’in bu object'lere olan
geçersiz `(obsolete)` referansları tutmaya devam etmesidir. Obsolete reference, bir daha asla `dereference` edilmeyecek
olan referanstır. Bu case de, element array’inin “aktif kısmı `(active portion)`” dışındaki tüm referanslar
obsolete’dur. Aktif kısım `(active portion)`, index'i size’dan küçük olan element’lerden oluşur.

Garbage-collected dillerdeki memory leak’ler (daha doğru ifade ile istenmeyen object tutmaları `(unintentional object 
retentions)`) sinsidir. Eğer bir object referansı istemeden tutulursa `(unintentionally retained)`, sadece o object
garbage collection'dan muaf kalmaz, aynı zamanda o object’in referans verdiği diğer object’ler de muaf kalır ve bu böyle
devam eder. İstemeden sadece birkaç object referansı tutulsa bile, çok sayıda object’in garbage collected tarafından
toplanması engellenebilir ve bu da performans üzerinde potansiyel olarak büyük etkiler yaratır.

Bu tür bir problemin çözümü basittir: Referanslar `obsolete` olduğunda onları `null` yap. Stack sınıfımızda, bir item’e
olan referans, item stack’ten çıkarılır çıkarılmaz obsolete olur. `pop` metodunun düzeltilmiş versiyonu şöyle görünür:

```
public Object pop(){
    if (size == 0)
        throw new EmptyStackException();
    Object result = elements[--size];
    elements[size] = null; // Obsolete referansı kaldır.
    return result;
}
```

Obsolete referansları null yapmak, yanlışlıkla daha sonra `(subsequently)` `dereference` edilseler bile programın
sessizce yanlış davranmak yerine hemen `NullPointerException` ile hata vermesini sağlar. Programlama hatalarını mümkün
olduğunca hızlı tespit etmek her zaman faydalıdır.

Programcılar bu problemle ilk karşılaştıklarında, program işini bitirir bitirmez her object referansını null yaparak
aşırı tepki verebilirler. Bu ne gerekli ne de arzu edilen bir durumdur; programı gereksiz yere karmaşıklaştırır.
Object referanslarını null yapmak, kuraldan çok exception olmalıdır. Obsolete referansı ortadan kaldırmanın en iyi yolu,
referansı içeren variable'ın `out of scope`'a çıkmasını sağlamaktır. Bu, her variable'ı mümkün olan en dar `(narrowest)`
scope'da define edersen doğal olarak gerçekleşir.

Peki, ne zaman referansı null yapmalısın? Stack sınıfının hangi yönü memory leak’e karşı savunmasız olmasını sağlar?
Basitçe söylemek gerekirse, kendi memory'sini manage ediyor olmasıdır. Storage pool, element array’inin element’lerinden
oluşur (object referans cell'leri, object'lerin kendisi değil). Array'in aktif kısmındaki `(active portion)`
element’ler (daha önce define edildiği gibi) tahsis edilmiş, array'in kalan kısmındaki element’ler ise boş `(free)`
durumdadır. Garbage collector bunun farkında değildir; garbage collector’a göre, elements array’indeki tüm object
referansları eşit derecede geçerlidir. Yalnızca programcı, array'in aktif olmayan `(inactive portion)` kısmının önemsiz
olduğunu bilir. Programcı, array element’leri aktif olmayan `(inactive portion)` kısma geçtiği anda onları manuel olarak
null yaparak bu durumu garbage collector’a etkili bir şekilde bildirir.

Genel olarak, bir class kendi memory'sini yönettiğinde, programcı memory leak’lere karşı dikkatli olmalıdır. Bir element
serbest bırakıldığında, o element’in içerdiği tüm object referansları null yapılmalıdır.

Memory leak’lerin bir diğer yaygın kaynağı ise cache’lerdir. Bir object referansını cache’e koyduğunda, onun orada
olduğunu unutmak ve artık önemsiz hale geldikten sonra bile cache’te bırakmak kolaydır. Bu problemin birkaç çözümü
vardır. Eğer bir cache implement ederken şanslıysan ve bir entry yalnızca key’ine cache dışından referanslar olduğu
sürece geçerliyse, cache’i bir `WeakHashMap` olarak represent et; entry’ler obsolete hale geldikten sonra otomatik
olarak kaldırılır. `WeakHashMap`’in faydası, cache entry’lerinin ömrünün, `value`'dan değil de `key`'e yapılan external
referanslar tarafından belirlendiği durumlarla sınırlıdır.

> WeakHashMap `https://www.baeldung.com/java-weakhashmap`

Data structure’ı anlamak için burada onu kullanarak basit bir cache implementation'ı yapacağız. Ancak unutma ki, burada
amaç map’in nasıl çalıştığını anlamaktır ve kendi cache implementasyonunu yazmak neredeyse her zaman kötü bir fikirdir.
Basitçe söylemek gerekirse, `WeakHashMap`, Map interface’inin hashtable based bir implementasyonudur ve key’ler
`WeakReference` type'ındadır. `WeakHashMap` içindeki bir `entry`, `key`’i artık normal `(ordinary)` kullanımda değilse —
yani o `key`’e işaret eden tek bir Reference bile yoksa — otomatik olarak kaldırılır. Garbage collection (GC) process'i
bir `key`’i kaldırdığında, ona ait `entry` map’ten fiilen silinir; bu yüzden bu class, diğer Map implementasyonlarından
biraz farklı davranır.

### Strong, Soft, and Weak References

`WeakHashMap`’in nasıl çalıştığını anlamak için `WeakReference sınıfına bakmamız gerekir. Bu sınıf, `WeakHashMap`
implementasyonundaki `key’ler için temel construct'dır. Java’da üç main reference type'ı vardır ve bunları sonraki
bölümlerde açıklayacağız.

1 - `Strong References` - Strong reference, günlük programlama sırasında en sık kullandığımız referans türüdür:

```
Integer prime = 1;
```

`prime` variable'i, value'su `1` olan bir `Integer` object’ine `strong` referans tutuyor.

2 - `Soft References` - Basitçe söylemek gerekirse, kendisine `SoftReference` ile referans verilen bir object, JVM
kesinlikle memory'e ihtiyaç duyana kadar garbage collection’a uğramaz. Java’da bir `SoftReference`’i nasıl
oluşturabileceğimize bakalım:

```
Integer prime = 1;
SoftReference<Integer> softReference = new SoftReference<>(prime);
prime = null;
```

`prime` object’e `strong` bir referans pointing ediyor. Sonra, prime strong reference’ı bir `soft reference` içine
wrap ediyoruz. O strong reference'ı `null` yaptıktan sonra, prime object GC için uygun hale gelir ancak JVM kesinlikle
belleğe ihtiyaç duyduğunda collect edilir.

3 - `Weak References` - Sadece `weak reference`’lar tarafından referans verilen object’ler, garbage collected işlemi
için hemen `(eagerly)` collect edilir; GC bu case'de memory'e ihtiyaç duyana kadar beklemez. Java’da bir WeakReference’ı
şu şekilde oluşturabiliriz:

```
Integer prime = 1;
WeakReference<Integer> weakReference = new WeakReference<>(prime);
prime = null;
```

`prime` referansını `null` yaptığımızda, prime object’e point eden başka `strong` bir referans olmadığından, sonraki GC
cycle'ında garbage collected olur. WeakReference type'ındaki referanslar, `WeakHashMap`’te `key` olarak kullanılır.

### WeakHashMap as an Efficient Memory Cache

Diyelim ki, big image object’lerini `value` olarak ve image name'lerini `key` olarak tutan bir `cache` oluşturmak
istiyoruz. Bu problem için uygun bir map implementasyonu seçmek istiyoruz.

Basit bir `HashMap` kullanmak iyi bir seçim olmaz çünkü value object’ler çok fazla memory kaplayabilir. Üstelik,
uygulamamızda artık kullanılmıyor olsalar bile, `GC` process'i tarafından cache’den asla geri kazanılmazlar. İdeal
olarak, kullanılmayan `(unused)` object’leri GC’nin otomatik olarak silebilmesini sağlayan bir Map implementasyonu
istiyoruz. Big bir image object’inin `key`’i uygulamamızda herhangi bir yerde kullanılmadığında, o `entry` memory'de
silinecektir. Neyse ki, `WeakHashMap` tam olarak bu özelliklere sahiptir. Şimdi WeakHashMap’imizi test edip nasıl
davrandığına bakalım:

`await` library'si yardımı ile 10 saniye içerisinde mapIsEmpty haline gelmiş ise alttaki statement çalıştırılacaktır.
await library'si için : `jayway.awaitility` maven dependency'si gerekiyor;

```
public static void main(String[] args) {
    WeakHashMap<UniqueImageName, BigImage> map = new WeakHashMap<>();
    UniqueImageName uniqueImageName = new UniqueImageName("image_id");
    BigImage bigImage = new BigImage("big image");

    map.put(uniqueImageName,bigImage);
    boolean isContains = map.containsKey(uniqueImageName); // => true
    uniqueImageName = null;
    System.gc();
    await().atMost(10,TimeUnit.SECONDS).until(map::isEmpty);
    System.out.println(map.isEmpty()); // => true
}

record UniqueImageName(String id) {
}

record BigImage(String name) {
}
```

`BigImage` object’lerimizi saklayacak bir `WeakHashMap` instance'ı oluşturuyoruz. Bir `BigImage` object’ini `value`
olarak ve bir `uniqueImageName` object referansını `key` olarak koyuyoruz. `uniqueImageName`, map içinde `WeakReference`
türü olarak saklanacaktır. Sonra, `uniqueImageName` referansını null yapıyoruz; böylece bigImage object’ine işaret eden
başka referans kalmıyor. WeakHashMap’in default behavior'u, kendisine referans olmayan bir `entry`’yi sonraki GC’de geri
kazanmak `(reclaim)` olduğundan, bu `entry` bir sonraki `GC` process'i ile memory'den silinecektir. JVM’nin GC
process'ini trigger etmesi için `System.gc()` call'unu yapıyoruz. `GC` cycle'ından sonra WeakHashMap’imiz empty
olacaktır:

`assertTrue()` methodunu kullanabilmek için JUnit Dependency'lerini ekliyorum: `junit.jupiter.api`;

```
public static void main(String[] args) {
    WeakHashMap<UniqueImageName, BigImage> map = new WeakHashMap<>();

    UniqueImageName imageNameFirst = new UniqueImageName("name_of_big_image");
    BigImage bigImageFirst = new BigImage("foo");

    UniqueImageName imageNameSecond = new UniqueImageName("name_of_big_image_2");
    BigImage bigImageSecond = new BigImage("foo_2");

    map.put(imageNameFirst, bigImageFirst);
    map.put(imageNameSecond, bigImageSecond);

    assertTrue(map.containsKey(imageNameFirst));
    assertTrue(map.containsKey(imageNameSecond));

    imageNameFirst = null;
    System.gc();

    await().atMost(10,TimeUnit.SECONDS).until(()-> map.size() == 1);
    await().atMost(10,TimeUnit.SECONDS).until(()-> map.containsKey(imageNameSecond));
    System.out.println("Done"); // => Done
}

record UniqueImageName(String id) {
}

record BigImage(String name) {
}
```

Dikkat edersen, yalnızca `imageNameFirst` referansı null yapılmıştır. `imageNameSecond` referansı ise değişmeden kalır.
GC trigger edildikten sonra, `map` yalnızca bir `entry` içerecektir – `imageNameSecond`.

> End of magazine

Daha yaygın olarak, bir cache entry’sinin useful lifetime'ı net bir şekilde tanımlanmaz; entry’ler zamanla value'sunu
yitirir. Bu gibi durumlarda, cache zaman zaman artık kullanılmayan entry’lerden arındırılmalıdır. Bu işlem bir
background thread’i (örneğin bir `ScheduledThreadPoolExecutor`) tarafından ya da cache’e yeni entry eklenmesi sırasında
side effect olarak yapılabilir. `LinkedHashMap` sınıfı, bu ikinci yaklaşımı `removeEldestEntry` metodu ile
kolaylaştırır. Daha gelişmiş cache’ler için, doğrudan `java.lang.ref` kullanman gerekebilir.

> LinkedHashMap removeEldestEntry() Method in Java

`https://www.geeksforgeeks.org/java/linkedhashmap-removeeldestentry-method-in-java/`

Java’daki `java.util.LinkedHashMap.removeEldestEntry()` metodu, map’in en eski `entry`’sini silip silmediğini takip
etmek için kullanılır. Bu nedenle, LinkedHashMap’e her yeni element eklendiğinde, en eski entry map’ten silinir. Bu
metod genellikle `put()` ve `putAll()` metodları ile map’e element eklendikten sonra çağrılır. Bu metod, döndürdüğü
değere göre map’in kendisini değiştirmesine izin verir. Metodun doğrudan map’i değiştirmesine izin verilse de, bunu
yaparsa `false` döndürmelidir; bu, map’in belirsizliğe yol açacak daha fazla değişiklik yapmaması gerektiğini belirtir.
Bu metodun içinden map’i değiştirip `true` döndürmenin etkileri belirtilmemiştir. Bu, map bir cache’i represent
ettiğinde çok faydalıdır; çünkü map’in eski entry’lerini tek tek silerek bellek kullanımını azaltmasına olanak tanır.
Metod, map’te en uzun süredir bulunan (en eski) entry’i temsil eden `eldest` parametresini alır. Eğer map erişim
sırasına göre ise, `eldest` en son erişilen entry’yi represent eder ve bu metod `true` dönerse silinir. Eğer map, `put`
veya `putAll` invocation'ınından önce boşsa, `eldest` parametresi yeni eklenen entry’yi represent eder; yani, map tek
bir entry içeriyorsa, `eldest` aynı zamanda en yeni entry’dir. Map, `eldest` entry’nin silinmesi gerekiyorsa `true`,
silinmemesi veya tutulması gerekiyorsa `false` döner.

```
// Map’in maksimum boyutuna işaret eder; bu boyut aşıldığında en eski entry silinir.
private static final int MAX = 6;
public static void main(String[] args) {
    // LinkedHashMap’i oluşturup removeEldestEntry() metodunu MAX boyutuna göre implement etmek:
    LinkedHashMap<Integer,String> lhm = new LinkedHashMap<>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Integer, String> eldest) {
            return size() > MAX;
        }
    };

    lhm.put(0, "Welcome");
    lhm.put(1, "To");
    lhm.put(2, "The");
    lhm.put(3, "World");
    lhm.put(4, "Of");
    lhm.put(5, "geeks");

    System.out.println(lhm); // => {0=Welcome, 1=To, 2=The, 3=World, 4=Of, 5=geeks}

    lhm.put(6, "GeeksforGeeks");

    System.out.println(lhm); // => {1=To, 2=The, 3=World, 4=Of, 5=geeks, 6=GeeksforGeeks}

    lhm.put(7, "Hello");

    System.out.println(lhm); // => {2=The, 3=World, 4=Of, 5=geeks, 6=GeeksforGeeks, 7=Hello}
}
```

> End of magazine

> Guava Cache `https://www.baeldung.com/guava-cache`

Guava Maven Dependecies; `com.google.guava`

String instance’larının uppercase halini cache’leyen basit bir örnekle başlayalım.

```
CacheLoader<String, String> loader = new CacheLoader<String, String>() {
    @Override
    public String load(String key) throws Exception {
        return key.toUpperCase();
    }
};

LoadingCache<String,String> cache;
cache = CacheBuilder.newBuilder().build(loader);
cache.getUnchecked("Ocean");

assertEquals(1,cache.size());
assertEquals("HELLO",cache.getUnchecked("hello"));
assertEquals(2,cache.size());
```

Dikkat edersen, `hello` key’i için cache’te bir value yok; bu yüzden value compute edilip cache’e ekleniyor. Ayrıca,
`getUnchecked()` operasyonunu kullandığımıza dikkat et; bu operasyon, eğer value zaten yoksa onu compute edip cache’e
yükler.

### Eviction Policies

Her cache, bir noktada value’ları silmek zorundadır. Şimdi, cache’ten value’ları silmek `(evict etmek)` için kullanılan
farklı kriterleri inceleyelim.

1 - `Eviction by Size` - Cache’imizi `maximumSize()` ile sınırlayabiliriz. Eğer cache bu sınıra ulaşırsa, en eski
kayıtları siler. Aşağıdaki kodda, cache boyutunu üç record ile sınırlayacağız:

```
CacheLoader<String, String> loader = new CacheLoader<String, String>() {
    @Override
    public String load(String key) throws Exception {
        return key.toUpperCase();
    }
};

LoadingCache<String,String> cache;
cache = CacheBuilder.newBuilder().maximumSize(3).build(loader);
cache.getUnchecked("first");
cache.getUnchecked("second");
cache.getUnchecked("third");
cache.getUnchecked("forth");

assertEquals(3,cache.size());
assertNull(cache.getIfPresent("first"));
assertEquals("FORTH",cache.getIfPresent("forth"));
ConcurrentMap<String, String> map = cache.asMap();
System.out.println(map); // => {second=SECOND, third=THIRD, forth=FORTH}
```

2 - `Eviction by Weight` - Cache boyutunu custom bir ağırlık `(weight)` fonksiyonu kullanarak da limitleyebiliriz.
Aşağıdaki kodda, uzunluğu `(length)` custom `weight` fonksiyonu olarak kullanacağız:

```
CacheLoader<String, String> loader = new CacheLoader<String, String>() {
    @Override
    public String load(String key) throws Exception {
        return key.toUpperCase();
    }
};

Weigher<String,String> weighByLength;
weighByLength = new Weigher<String, String>() {
    @Override
    public int weigh(String key, String value) {
        return value.length();
    }
};

LoadingCache<String,String> cache;
cache = CacheBuilder.newBuilder()
        .maximumWeight(16)
        .weigher(weighByLength)
        .build(loader);

cache.getUnchecked("first");
cache.getUnchecked("second");
cache.getUnchecked("third");
cache.getUnchecked("last");

assertEquals(3, cache.size());
assertNull(cache.getIfPresent("first"));
assertEquals("LAST",cache.getIfPresent("last"));

ConcurrentMap<String, String> map = cache.asMap();
System.out.println(map); // => {second=SECOND, third=THIRD, last=LAST}
```

Not: cache, büyük bir record için yer açmak amacıyla birden fazla kaydı silebilir.

3 - `Eviction by Time` - Eski record'ları silmek için size'ın yanı sıra zamanı da kullanabiliriz. Aşağıdaki örnekte,
2 ms boyunca kullanılmamış record'ları silmek üzere cache’imizi customize edeceğiz:

```
CacheLoader<String, String> loader = new CacheLoader<String, String>() {
    @Override
    public String load(String key) throws Exception {
        return key.toUpperCase();
    }
};

LoadingCache<String, String> cache;
cache = CacheBuilder.newBuilder()
        .expireAfterAccess(2, TimeUnit.MILLISECONDS)
        .build(loader);

cache.getUnchecked("hello");
assertEquals(1,cache.size());

cache.getUnchecked("hello");
Thread.sleep(300);

cache.getUnchecked("test");
assertEquals(1,cache.size());

ConcurrentMap<String, String> map = cache.asMap();
System.out.println(map); // => {test=TEST}
```

Record'ları, toplam live time'larına göre de silebiliriz. Aşağıdaki örnekte, cache record'ları 2 ms boyunca saklandıktan
sonra silecektir:

```
CacheLoader<String, String> loader = new CacheLoader<String, String>() {
    @Override
    public String load(String key) throws Exception {
        return key.toUpperCase();
    }
};

LoadingCache<String, String> cache;
cache = CacheBuilder.newBuilder()
        .expireAfterWrite(2,TimeUnit.MILLISECONDS)
        .build(loader);

cache.getUnchecked("hello");
assertEquals(1,cache.size());

Thread.sleep(300);

cache.getUnchecked("test");
assertEquals(1,cache.size());

ConcurrentMap<String, String> map = cache.asMap();
System.out.println(map); // => {test=TEST}
```

### Weak Keys

Sonraki adımda, cache key’lerimizi weak reference yaparak, garbage collector’ın başka yerde referans verilmeyen cache
key’leri toplayabilmesini nasıl sağlayacağımızı göstereceğiz. Default olarak hem cache `key`’ler hem de `value`’lar
güçlü `(strong)` referanslara sahiptir, ancak `weakKeys()` kullanarak cache’in `key`’leri weak reference olarak
saklamasını sağlayabiliriz:

```
CacheLoader<String, String> loader = new CacheLoader<String, String>() {
    @Override
    public String load(String key) throws Exception {
        return key.toUpperCase();
    }
};

LoadingCache<String, String> cache;
cache = CacheBuilder.newBuilder()
        .weakKeys()
        .build(loader);

cache.getUnchecked("first");
```

### Soft Values

Garbage collector’ın cache’lenmiş value’ları collect etmesine izin vermek için `softValues()` kullanabiliriz:

```
CacheLoader<String, String> loader = new CacheLoader<String, String>() {
    @Override
    public String load(String key) throws Exception {
        return key.toUpperCase();
    }
};

LoadingCache<String, String> cache;
cache = CacheBuilder.newBuilder()
        .softValues()
        .build(loader);
```

Not: Çok fazla `soft reference` sistem performansını etkileyebilir, bu yüzden tercih edilen seçenek `maximumSize()`
kullanmaktır.

### Handle null Values

Şimdi cache’teki null value’ların nasıl handle edileceğine bakalım. Default olarak, `Guava Cache` null bir value
load etmeye çalışırsak exception fırlatır; çünkü bir null’ı cache’lemek mantıklı değildir. Ancak null value kodumuzda
bir anlam ifade ediyorsa, bu durumda `Optional` sınıfını etkili bir şekilde kullanabiliriz:

```
public static void main(String[] args) throws InterruptedException {
    CacheLoader<String, Optional<String>> loader = new CacheLoader<String, Optional<String>>() {
        @Override
        public Optional<String> load(String key) throws Exception {
            return Optional.ofNullable(getSuffix(key));
        }
    };

    LoadingCache<String,Optional<String>> cache;
    cache = CacheBuilder.newBuilder()
            .build(loader);

    assertEquals("txt",cache.getUnchecked("test.txt").get());
    assertFalse(cache.getUnchecked("hello").isPresent());
}
private static String getSuffix(final String str){
    int lastIndex = str.lastIndexOf(".");
    if (lastIndex == -1)
        return null;
    return str.substring(lastIndex + 1);
}
```

### Refresh the Cache

Şimdi, cache value’larımızı nasıl refresh edeceğimizi öğreneceğiz.

1 - `Manual Refresh` - Bir key’i manuel olarak refresh etmek için `LoadingCache.refresh(key)` metodundan
faydalanabiliriz:

```
String value = loadingCache.get("key");
loadingCache.refresh("key");
```

Bu, CacheLoader’ın ilgili key için yeni value’yu load etmesini zorunlu kılar. Yeni value başarıyla yüklenene kadar,
`get(key)` call'u ilgili key’in önceki value’sunu döndürür.

2 - `Automatic Refresh` - Cache’lenmiş value’ları otomatik olarak refresh etmek için
`CacheBuilder.refreshAfterWrite(duration)` metodunu da kullanabiliriz:

```
CacheLoader<String, String> loader = new CacheLoader<String, String>() {
    @Override
    public String load(String key) throws Exception {
        return key.toUpperCase();
    }
};

LoadingCache<String, String> cache = CacheBuilder.newBuilder()
        .refreshAfterWrite(1, TimeUnit.MINUTES)
        .build(loader);
```

`refreshAfterWrite(duration)` metodunun, belirtilen sürenin ardından sadece bir key’i refresh için uygun hale
getirdiğini anlamak önemlidir. Value, aslında yalnızca ilgili entry `get(key)` ile sorgulandığında `(queried)` refresh
edilir.

### Preload the Cache

Cache’imize birden fazla record eklemek için `putAll()` metodunu kullanabiliriz. Aşağıdaki örnekte, bir Map kullanarak
cache’imize birden fazla record ekleyeceğiz:

```
CacheLoader<String, String> loader = new CacheLoader<String, String>() {
    @Override
    public String load(String key) throws Exception {
        return key.toUpperCase();
    }
};

LoadingCache<String, String> cache = CacheBuilder.newBuilder()
        .build(loader);

Map<String,String> map = new HashMap<>();
map.put("first","FIRST");
map.put("second","SECOND");

cache.putAll(map);
assertEquals(2,cache.size());
```

### RemovalNotification

Bazen bir record cache’den silindiğinde işlem yapmak gerekebilir; bu yüzden `RemovalNotification`’ı inceleyeceğiz.
Record'ların removed notification'larını almak için `RemovalListener` register edebiliriz. Ayrıca, removal sebebine
`getCause()` metodu ile erişebiliriz. Aşağıdaki örnekte, cache boyutu nedeniyle dördüncü element silindiğinde bir
`RemovalNotification` alınır:

```
CacheLoader<String, String> loader = new CacheLoader<String, String>() {
    @Override
    public String load(String key) throws Exception {
        return key.toUpperCase();
    }
};

RemovalListener<String, String> listener = notification -> {
    if (notification.wasEvicted()) {
        String cause = notification.getCause().name();
        assertEquals(RemovalCause.SIZE.toString(), cause);
        System.out.println(cause); // => SIZE
    }
};

LoadingCache<String,String> cache = CacheBuilder.newBuilder()
        .maximumSize(3)
        .removalListener(listener)
        .build(loader);

cache.getUnchecked("first");
cache.getUnchecked("second");
cache.getUnchecked("third");
cache.getUnchecked("last");

ConcurrentMap<String, String> map = cache.asMap();
System.out.println(map); // => {second=SECOND, third=THIRD, last=LAST}

assertEquals(3, cache.size());
```

Son olarak, Guava cache implementasyonu hakkında birkaç hızlı not:

* Thread-safe’dir.

* Cache’e manuel olarak `put(key, value)` ile değer ekleyebiliriz.

* Cache performansımızı `CacheStats` (hitRate(), missRate(), vb.) kullanarak ölçebiliriz.

> End of magazine

Memory leak’lerin üçüncü yaygın kaynağı, listener’lar ve diğer callback’lerdir. Eğer client’ların callback register
ettiği ama explicitly `deregister` edilmeyen bir API implement edersen, müdahale etmediğin sürece callback’ler
birikecektir `(accumulate)`. Callback’lerin hızlıca garbage collected olmasını sağlamak için, onlara sadece `weak
reference` tutmak bir yöntemdir; örneğin, callback’leri sadece `WeakHashMap`’in key’i olarak saklamak gibi. Memory
leak’ler genellikle belirgin hatalar olarak ortaya çıkmadığı için, sistemde yıllarca var olmaya devam edebilirler.
Genellikle yalnızca dikkatli code inspection veya `heap profiler` adı verilen debugging tool'u yardımıyla keşfedilirler.
Bu nedenle, bu tür problemleri ortaya çıkmadan önce tahmin etmeyi ve oluşmalarını engellemeyi öğrenmek çok önemlidir.