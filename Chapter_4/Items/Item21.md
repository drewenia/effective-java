# Design interfaces for posterity

# Gelecek için interface'ler tasarlayın.

Java 8'den önce, mevcut implementation'ları bozmadan interface'lere metot eklemek imkansızdı. Eğer bir interface'e yeni
bir metot eklerseniz, mevcut implementation'larda genellikle bu metot bulunmaz ve bu da compile time hatasına yol açar.
Java 8'de, mevcut interface'lere metot eklemeyi sağlamak amacıyla default method construct `[JLS 9.4]` eklendi. Ancak
mevcut interface'lere yeni metotlar eklemek risklerle doludur.

Bir default method'un declaration'ı, interface'i implement eden ancak default method'u implement etmeyen tüm sınıflar
tarafından kullanılan bir default implementation içerir. Java'ya default method'ların eklenmesi, mevcut bir interface'e
metot eklemeyi mümkün kılsada, bu metotların önceden var olan tüm implementation'larda çalışacağına dair bir garanti
yoktur. Default method'lar, implementor'ların bilgisi veya rızası olmadan mevcut implementation'a "inject" edilir.
Java 8'den önce, bu implementation'lar, interface'erin asla yeni metotlar edinmeyeceği (kapalı, zımnî, söylenmeden
anlaşılan/ifade edilen) `tacit`  anlayışıyla yazılıyordu.

Java 8'de, özellikle lambdaların kullanımını kolaylaştırmak amacıyla core collection interface'lerine birçok yeni
default method eklendi. Java library'lerinin default method'ları high-quality genel amaçlı implementation'lardır ve çoğu
durumda sorunsuz çalışır. Ancak her olası implementation'ın tüm değişmezlerini `(invariant)` koruyan bir default method
yazmak her zaman mümkün değildir. Örneğin, Java 8'de Collection interface'ine eklenen `removeIf` metodunu düşünün. Bu
metot, belirli bir boolean function'ının (veya predicate'in) `true` döndürdüğü tüm element'leri kaldırır. Default
implementation, collection'ı iterator'ını kullanarak dolaşacak, her element üzerinde predicate'i invoke edecek ve
predicate'in `true` döndürdüğü element'leri kaldırmak için iterator'ın `remove` metodunu kullanacak şekilde
belirtilmiştir. Tahminen declaration şöyle bir şeye benziyor:

```
// Default method added to the Collection interface in Java 8
default boolean removeIf(Predicate<? super E> filter) {
    Objects.requireNonNull(filter);
    boolean result = false;

    for (Iterator<E> it = iterator(); it.hasNext(); ) {
        if (filter.test(it.next())) {
            it.remove();
            result = true;
        }
    }
    return result;
}
```

Bu, `removeIf` metodu için yazılabilecek en iyi genel amaçlı implementation'dır, ancak ne yazık ki, bazı gerçek dünya
Collection implementation'larında başarısız olur.

Örneğin, `org.apache.commons.collections4.collection.SynchronizedCollection`'ı ele alalım. Apache Commons library'sinde
bu sınıf, `java.util` içindeki `Collections.synchronizedCollection` static factory metodu tarafından döndürülen sınıfa
benzer. Apache versiyonu ek olarak, collection'ın yerine, `locking` için client-supplied bir object kullanma yeteneği
sunar. Başka bir deyişle, o bir wrapper sınıfıdır (Item 18); tüm metotları, wrapped collection'a delegate etmeden önce
bir `locking` object'i üzerinde synchronize olur.

Apache SynchronizedCollection sınıfı hala aktif olarak sürdürülmektedir, ancak bu yazının yazıldığı an itibarıyla
`removeIf` metodunu override etmemiştir. Bu sınıf Java 8 ile birlikte kullanıldığında, bu nedenle `removeIf`'in default
implementation'ını inherit alacaktır; bu implementation, sınıfın temel vaadini, yani her metot invocation etrafında
otomatik olarak `synchronize` olmayı sağlamaz ve sağlayamaz da. Default implementation, synchronization hakkında hiçbir
şey bilmez ve `locking` object'ini içeren field'e erişimi yoktur. Eğer bir client, bir SynchronizedCollection instance'ı
üzerinde `removeIf` metodunu, collection'ın başka bir thread tarafından concurrent olarak değiştirildiği durumlarda
call ederse, bir `ConcurrentModificationException` veya başka belirtilmemiş davranışlar ortaya çıkabilir.

Benzer Java platform library'lerinde ki implementation'da, örneğin `Collections.synchronizedCollection` tarafından
döndürülen package-private sınıfta bunun olmasını engellemek için, JDK maintainer'ları, default `removeIf`
implementation'ını ve onun gibi diğer metotları, default implementation'ı invoke etmeden önce gerekli synchronization'ı
gerçekleştirecek şekilde override etmek zorunda kaldılar. Java platformunun bir parçası olmayan önceden var olan
collection implementation'ları, interface değişikliğiyle eşzamanlı olarak benzer değişiklikleri yapma fırsatına sahip
olamadı ve bazılarının hala bunu yapması gerekiyor.

> Java Collections.synchronizedCollection

`Collections.synchronizedCollection` method'u, collection'lar etrafında `thread-safe` wrapper'lar oluşturur. Bu,
belirtilen collection'ın arkasında çalışan `synchronized (thread-safe)` bir collection döndürür. Bu, multi-threaded
ortamlar için hayati öneme sahiptir. Döndürülen collection'a tüm erişim `synchronized wrapper` üzerinden yapılmalıdır.
Wrapper, tüm method call'larının `atomic` olmasını garanti eder. Ancak, compound operation'lar için hâlâ manuel olarak
synchronization yapılması gerekir. `synchronizedCollection` method'u, Java'nın Collections utility class'ının bir
parçasıdır. Bu method, collection operation'ları için basic thread safety sağlar. Herhangi bir Collection
implementasyonunu synchronization ile wrap eder. Döndürülen collection, tüm method erişimlerini sıralı `(serialize)`
hale getirir. Bu, concurrent değişiklik sorunlarını önler. Ancak, `ConcurrentModificationException`'dan kaçınmak için
iteration sırasında açık `(explicit)` synchronization gereklidir. Bu örnek, basic bir synchronized collection
oluşturmayı göstermektedir. Bir ArrayList'i `Collections.synchronizedCollection` ile wrap ediyoruz. Örnek, synchronized
collection üzerindeki basic operation'ları göstermektedir.

```
Collection<String> baseCollection = new ArrayList<>();
Collection<String> syncCollection = Collections.synchronizedCollection(baseCollection);

// Elementleri thread-safe bir şekilde ekleyin.
syncCollection.add("Java");
syncCollection.add("Python");
syncCollection.add("C#");

System.out.println(syncCollection); // => [Java, Python, C#]

// Element remove
syncCollection.remove("Python");
System.out.println(syncCollection); // => [Java, C#]
System.out.println(syncCollection.size()); // => 2

System.out.println(baseCollection); // => [Java, C#]
System.out.println(baseCollection.size()); // => 2
```

Bu kod, bir ArrayList etrafında `synchronized wrapper` oluşturur. `syncCollection` üzerindeki tüm operation'lar
thread-safe'dir. Örnek, element ekleme, çıkarma ve boyut kontrolü operation'ları göstermektedir. Output, her
operation'ın ardından collection'ın state'ini gösterir. Synchronized wrapper, bu operation'ların birden fazla thread
tarafından erişildiğinde `atomic` olmasını garanti eder.

### Multi-threaded Access

Bu örnek, birden fazla thread'in synchronized collection'a güvenli şekilde erişimini göstermektedir. Collection'ı
concurrently olarak değiştiren birkaç thread oluşturuyoruz. Synchronized wrapper, data corruption'ı önler.

```
public static void main(String[] args) throws InterruptedException {
    Collection<Integer> numbers = Collections.synchronizedCollection(new ArrayList<>());

    // Birden fazla thread oluşturun ve başlatın.
    Thread[] threads = new Thread[5];
    for (int i = 0; i < threads.length; i++) {
        final int threadId = i;
        threads[i] = new Thread(() -> {
            for (int j = 0; j < 100; j++) {
                numbers.add(threadId * 100 + j);
            }
        });
        threads[i].start();
    }

    // Tüm thread'lerin bitmesini bekle
    for (Thread thread : threads){
        thread.join();
    }

    System.out.println("Total elements: " + numbers.size()); // => Total elements: 500
}
```

Bu örnek, thread-safe collection erişimini gösterir. Beş thread, collection'a concurrently olarak thread başı 100
element ekler. Synchronized wrapper, tüm eklemelerin doğru şekilde serialized edilmesini sağlar. Son toplam tam olarak
500 element olmalıdır (5 thread × her biri 100 element). Synchronization olmadan, race condition nedeniyle sayı tahmin
edilemez olurdu.

### Iterating with Synchronization

Synchronized collection üzerinde iterate etmek için açık `(explicit)` synchronization gereklidir. Bu örnek, thread
safety korunarak doğru iterasyon yöntemini gösterir. Iteration boyunca Collection `lock` edilir.

```
Collection<String> syncCollection = Collections.synchronizedCollection(new ArrayList<>());

syncCollection.add("Apple");
syncCollection.add("Banana");
syncCollection.add("Cherry");

// Doğru şekilde synchronized edilmiş iteration
synchronized (syncCollection) {
    for (String item : syncCollection){
        System.out.println(item);
    }
}

// Alternative with forEach (Java 8+)
syncCollection.forEach(System.out::println);
```

Bu örnek, iteration'a iki farklı yaklaşımı gösterir. İlki, iteration sırasında concurrent değişiklikleri önlemek için
açık `(explicit)` `synchronization` kullanır. İkincisi, internally olarak `synchronized` olan forEach method'unu
kullanır. Her iki yaklaşım da thread-safe'dir, ancak `synchronized block` daha fazla kontrol sağlar. `forEach` method'u
daha kısa yazılır ancak complex operation'ları için daha az esnek olabilir.

### Compound Operations

Synchronized collection'lar üzerinde compound operation'lar ek `synchronization` gerektirir. Bu örnek, bir elementin
`atomic` olarak kontrol edilip eklenmesini gösterir. Tüm operation `thread-safe` olması için synchronized olmalıdır.

```
Collection<String> syncCollection = Collections.synchronizedCollection(new ArrayList<>());

syncCollection.add("Red");
syncCollection.add("Green");

// Thread-safe compound operation
synchronized (syncCollection) {
    if (!syncCollection.contains("Blue")){
        syncCollection.add("Blue");
    }
}

System.out.println("Collection: " + syncCollection); // => Collection: [Red, Green, Blue]
```

Bu örnek, yaygın bir pattern olan `check-then-act`'i gösterir. `contains` kontrolü ve sonrasında yapılan `add`
operation'ı, race condition'larını önlemek için atomic olmalıdır. Synchronized block, bu operation'lar arasında başka
hiçbir thread'in collection'ı değiştiremeyeceğini garanti eder. Bu synchronization olmazsa, başka bir thread bizim
kontrolümüz ile ekleme işlemi arasında "Blue" element'ini ekleyebilir. Bu durum, duplicate elementlere veya diğer
tutarsızlıklara yol açabilir.

### Synchronized Collection vs Concurrent Collections

Bu örnek, synchronized collection'ları `CopyOnWriteArrayList` gibi concurrent collection'larla karşılaştırır. Her
yaklaşım için performans farklarını ve kullanım senaryolarını gösterir. CopyOnWriteArrayList class'ı, List interface'ini
implement eden ve JDK 1.5'te tanıtılan bir class'tır. Bu, tüm modification'ların (add, set, remove, vb.) fresh bir kopya
oluşturularak gerçekleştirildiği, ArrayList'in enchanced bir versiyonudur. Adından da anlaşılacağı gibi,
`CopyOnWriteArrayList` her update operation'ı için altta yatan `(underlying)` ArrayList'in klonlanmış bir kopyasını
oluşturur; belirli bir noktada her ikisi de otomatik olarak senkronize edilir, bu JVM tarafından handle edilir. Bu
nedenle, read operation gerçekleştiren thread'ler için herhangi bir etkisi yoktur. Kullanımı maliyetlidir çünkü her
update operation için klonlanmış bir kopya oluşturulur. Bu nedenle, sık yapılan operation read operation ise
CopyOnWriteArrayList en iyi tercihtir. Altta yatan `(underlying)` data structure grow-able bir array'dir. Bu,
ArrayList'in `thread-safe` versiyonudur. Ekleme sırası korunur, duplicates, `null` ve `heterogeneous` Object'lere izin
verilir. `CopyOnWriteArrayList` ile ilgili en önemli nokta, `CopyOnWriteArrayList` Iterator'ının remove operation'ı
gerçekleştiremeyeceğidir; aksi takdirde `UnsupportedOperationException` hatası alınır. CopyOnWriteArrayList
iterator'ındaki `add()` ve `set()` method'ları da `UnsupportedOperationException` fırlatır. Ayrıca, CopyOnWriteArrayList
iterator'ı asla `ConcurrentModificationException` fırlatmaz.

```
// Synchronized collection
Collection<Integer> syncCollection = Collections.synchronizedCollection(new ArrayList<>());

// Concurrent collection
Collection<Integer> concurrentCollection = new CopyOnWriteArrayList<>();

long start,end;

// Test synchronized collection
start = System.currentTimeMillis();
for (int i = 0; i < 1_000_000; i++) {
    syncCollection.add(i);
}
end = System.currentTimeMillis();
System.out.println("Synchronized collection time: " + (end - start) + "ms"); // => 10 ms

// Test concurrent collection
start = System.currentTimeMillis();
for (int i = 0; i < 100_000; i++) {
    concurrentCollection.add(i);
}
end = System.currentTimeMillis();
System.out.println("Concurrent collection time: " + (end - start) + "ms");
```

syncCollection'a `1_000_000` element ekleniyor, concurrentCollection'a `100_000` element ekleniyor.

Output;

```
Synchronized collection time: 11ms
Concurrent collection time: 914ms
```

Bu örnek, synchronized collection'lar ile concurrent collection'ların karşılaştırmalı performans ölçümünü yapar.
Synchronized collection'lar kaba taneli `(coarse-grained) locking` kullanırken, concurrent collection'lar daha gelişmiş
teknikler kullanır. Performans characteristic'leri use case'lere göre farklılık gösterir. Synchronized collection'lar,
genellikle write-heavy ve simple operation'lar içeren workload'ları için daha iyidir. Concurrent collection'lar,
genellikle read-heavy workload'ları veya complex operation'lar için daha iyi performans gösterir.

CopyOnWriteArrayList example;

```
public static void main(String[] args) throws InterruptedException {
    Integer[] integerArray = new Integer[]{1,3,5,8};
    CopyOnWriteArrayList<Integer> numbers = new CopyOnWriteArrayList<>(integerArray);
    
    /* CopyOnWriteArrayList için bir iterator oluşturduğumuzda, iterator() call edildiği anda listedeki 
    data'ların immutable bir snapshot'ını elde ettiğimizi unutmayın. */
    
    Iterator<Integer> iterator = numbers.iterator();
    numbers.add(10);

    List<Integer> result = new LinkedList<>();
    /* Bu nedenle, üzerinde iteration yaparken, iteration'da 10 sayısını görmeyeceğiz: */
    iterator.forEachRemaining(result::add);
    System.out.println(result); // => [1, 3, 5, 8]

    /* Yeni oluşturulan Iterator kullanılarak yapılan sonraki iterating'ler de eklenen 10 sayısını 
    döndürür: */
    Iterator<Integer> iterator2 = numbers.iterator();
    List<Integer> result2 = new LinkedList<>();
    iterator2.forEachRemaining(result2::add);
    System.out.println(result2); // => [1, 3, 5, 8, 10]
}
```

### Synchronized Collection with Custom Objects

Bu örnek, synchronized collection'ların custom object'lerle kullanımını göstermektedir. Bu, collection element'lerinin
kendilerine birden fazla thread tarafından erişilebileceği durumlarda thread safety'nin nasıl sağlanacağını gösterir.

Product.java;

```
class Product {
    private String name;
    private double price;

    public Product(String name, double price) {
        this.name = name;
        this.price = price;
    }

    public synchronized void updatePrice(double newPrice) {
        this.price = newPrice;
    }

    @Override
    public synchronized String toString() {
        return "Product{" +
                "name='" + name + '\'' +
                ", price=" + price +
                '}';
    }
}
```

Derived class;

```
Collection<Product> products = Collections.synchronizedCollection(new ArrayList<>());

// add products
products.add(new Product("Laptop", 999.99));
products.add(new Product("Phone", 699.99));

// price'ları birden fazla thread'den güncelle.
Thread t1 = new Thread(() -> {
    for (Product p : products) {
        p.updatePrice(p.toString().contains("Laptop") ? 899.99 : 599.99);
    }
});

Thread t2 = new Thread(() -> {
    synchronized (products) {
        for (Product p : products) {
            System.out.println(p);
        }
    }
});

t1.start();
t2.start();

try {
    t1.join();
    t2.join();
} catch (InterruptedException e) {
    e.printStackTrace();
}
```

Bu örnek, custom Product object'lerinden oluşan bir collection'ı gösterir. Product class'ı, fiyat güncellemeleri için
kendi synchronization'ına sahiptir. Collection wrapper, collection structure'ına thread-safe erişimi sağlar.
Collection'ı synchronize etmek, element'lere erişimi otomatik olarak synchronize etmez. Product class'ı, thread-safe
element erişimi için kendi synchronization'ını sağlamalıdır.

### Synchronized Collection Performance Considerations

Bu örnek, synchronized collection'ların performans etkilerini göstermektedir. Bu, synchronization yükünün
single-thread'li ve multi-thread'li senaryolardaki operation'ları nasıl etkilediğini gösterir.

```
public static void main(String[] args) throws InterruptedException {
    final int ELEMENTS = 1_000_000;
    long start, end;

    // Unsynchronized collection
    Collection<Integer> normalCollection = new ArrayList<>();

    // Synchronized collection
    Collection<Integer> syncCollection = Collections.synchronizedCollection(new ArrayList<>());

    // Test unsynchronized add
    start = System.currentTimeMillis();
    for (int i = 0; i < ELEMENTS; i++) {
        normalCollection.add(i);
    }
    end = System.currentTimeMillis();
    System.out.println("Normal collection add time: " + (end - start) + "ms");

    // Test synchronized add
    start = System.currentTimeMillis();
    for (int i = 0; i < ELEMENTS; i++) {
        syncCollection.add(i);
    }
    end = System.currentTimeMillis();
    System.out.println("Synchronized collection add time: " + (end - start) + "ms");

    // Test multi-threaded synchronized add
    syncCollection.clear();
    start = System.currentTimeMillis();
    Thread[] threads = new Thread[4];
    for (int i = 0; i < threads.length; i++) {
        threads[i] = new Thread(() -> {
            for (int j = 0; j < ELEMENTS / threads.length; j++) {
                syncCollection.add(j);
            }
        });
        threads[i].start();
    }

    for (Thread t : threads) {
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    end = System.currentTimeMillis();
    System.out.println("Multi-threaded sync collection add time: " + (end - start) + "ms");
}
```

Output;

```
Normal collection add time: 9ms
Synchronized collection add time: 9ms
Multi-threaded sync collection add time: 42ms
```

Bu örnek, synchronized collection performansını ölçer. Bu, synchronized ve unsynchronized collection'lar arasında
single-thread'li operation'ları karşılaştırır. Ayrıca synchronized collection ile multi-thread'li performansı da ölçer.
Sonuçlar, single thread'li senaryolarda synchronization'ın ek yük getirdiğini gösteriyor. Ancak, multi thread
ortamlarında synchronization, data corruption'ı önler ve thread safety sağlar; bu da bir miktar performans maliyetiyle
gerçekleşir.

> End of documentation

Default method’ların varlığında, bir interface’in mevcut implementasyonları compile sırasında hata veya uyarı
vermeyebilir ancak runtime'da başarısız olabilir. Çok yaygın olmamakla birlikte, bu problem izole bir olay da değildir.
Java 8’de collections interface’lerine eklenen birkaç method’un bu soruna açık olduğu ve bazı mevcut implementasyonların
bundan etkilendiği bilinmektedir.

Yeni method’ları mevcut interface’lere eklemek için `default method` kullanmaktan, ihtiyaç kritik olmadıkça
kaçınılmalıdır; kritikse, default method implementasyonunuzun mevcut interface implementasyonlarını bozup bozmayacağını
iyi düşünmelisiniz. Ancak default method’lar, bir interface oluşturulduğunda standart method implementasyonları sağlamak
ve interface implementasyonunu kolaylaştırmak için son derece faydalıdır. Ayrıca şunu da belirtmek gerekir ki, default
method’lar interface’lerden method silmeyi veya mevcut method’ların imzalarını değiştirmeyi desteklemek amacıyla
tasarlanmamıştır. Bu interface değişikliklerinin hiçbiri, mevcut client’ları bozmadan mümkün değildir.

Çıkarılacak ders açıktır. Default method’lar artık Java platformunun bir parçası olsa da, interface’leri büyük bir
özenle tasarlamak hâlâ son derece önemlidir. Default method’lar mevcut interface’lere yeni method’lar eklemeyi mümkün
kılsa da, bunu yapmak büyük bir risk taşır. Bir interface küçük bir kusur içeriyorsa, bu kusur kullanıcılarını sonsuza
dek rahatsız edebilir; bir interface ciddi şekilde yetersizse, içinde bulunduğu API’yi başarısızlığa uğratabilir.

Bu nedenle, her yeni interface’i yayımlamadan önce test etmek son derece önemlidir. Birden fazla programcı, her
interface’i farklı şekillerde implement etmelidir. En azından, üç farklı çeşitlilikte implementasyon hedeflemelisiniz.
Aynı derecede önemli olan, her yeni interface’in instance'larını kullanarak çeşitli task'ları yerine getiren birden
fazla client program yazmaktır. Bu, her interface’in amaçlanan tüm kullanımları karşılamasını sağlama yolunda büyük bir
katkı sağlar. Bu adımlar, interface’ler yayımlanmadan önce kusurları keşfetmenizi sağlar ve henüz kolayca
düzeltebilirsiniz. Bir interface yayımlandıktan sonra bazı kusurları düzeltmek mümkün olsa da buna güvenemezsiniz.