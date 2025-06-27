# Avoid finalizers and cleaners

Finalizers unpredictable'dır, çoğu zaman tehlikelidir ve genellikle gereksizdir. Kullanımları, düzensiz behavior'lara,
düşük performansa ve taşınabilirlik `(portability)` problemlerine yol açabilir. Finalizer’ların az sayıda geçerli
kullanım durumu vardır — bunlara bu bölümde daha sonra değineceğiz — ancak genel kural olarak, finalizer’lardan
kaçınmalısın. Java 9 itibarıyla finalizer’lar kullanımdan kaldırılmıştır `(deprecated)`, ancak Java library'leri
tarafından hâlâ kullanılmaktadır. Java 9’daki finalizer yerine önerilen yapı `cleaner`’lardır. Cleaner’lar,
finalizer’lara kıyasla daha az tehlikelidir; ancak hâlâ unpredictable'dır, yavaştır ve genellikle gereksizdir.

C++ programcılarına, finalizer veya cleaner’ları Java’daki C++ destructor’larının karşılığı olarak düşünmemeleri
konusunda uyarı yapılır. C++’ta destructor’lar, bir object ile ilişkili resource'ları geri kazanmanın `(reclaim)` normal
yoludur ve constructor’ların zorunlu bir karşılığıdır. Java’da, bir object erişilemez hale geldiğinde, ona ait bellek
alanı garbage collector tarafından otomatik olarak geri kazanılır `(reclaim)`; bu durum programcıdan özel bir çaba
gerektirmez. C++ destructor’ları, sadece belleği değil, diğer nonmemory resource'ları geri kazanmak için de kullanılır.
Java’da bu amaç için `try-with-resources` veya `try-finally` bloğu kullanılır.

Finalizer’lar ve cleaner’ların bir eksikliği, bunların zamanında çalıştırılacağının garanti edilmemesidir `[JLS, 12.6]`.
Bir object erişilemez hale geldikten sonra, onun finalizer’ının veya cleaner’ının çalışmasına kadar geçen süre keyfi
olarak uzun olabilir. Bu, finalizer veya cleaner içinde zaman açısından kritik hiçbir işlem yapmaman gerektiği anlamına
gelir. Örneğin, open file descriptor'ları sınırlı bir resource olduğundan, file'ları kapatmak için `finalizer` veya
`cleaner`’a güvenmek ciddi bir hatadır. Finalizer veya cleaner’ların gecikmeli `(tardiness)` çalışması nedeniyle birçok
file açık kalırsa, program file açamaz hale geldiği için başarısız olabilir.

Finalizer ve cleaner’ların ne kadar hızlı çalıştırıldığı öncelikle garbage collection algoritmasına bağlıdır ve bu
algoritma implementasyonlar arasında büyük farklılıklar gösterir. Finalizer veya cleaner’ın hızlı çalışmasına bağlı
programların behavior'u da buna bağlı olarak değişkenlik gösterebilir. Böyle bir program, üzerinde test ettiğin JVM’de
sorunsuz çalışırken, en önemli müşterinin tercih ettiği JVM’de büyük ihtimalle başarısız olabilir.

Gecikmeli `(tardy)` finalization sadece teorik bir problem değildir. Bir sınıfa finalizer sağlamak, o sınıfın
instance’larının geri kazanımını `(reclamation)` keyfi olarak geciktirebilir. Bir meslektaşım, gizemli bir şekilde
`OutOfMemoryError` ile çöken uzun süre çalışan bir `GUI` uygulamasını debug etti. Yapılan analizde, uygulamanın çökme
anında, finalize edilip geri kazanılmayı `(reclaim)` bekleyen binlerce grafik object'inin finalizer queue'sunda olduğu
ortaya çıktı. Ne yazık ki, finalizer thread’i başka bir uygulama thread’inden lower priority'de çalışıyordu, bu yüzden
object'ler finalization için uygun hale geldikleri hızda finalize edilmiyordu. Dil spesifikasyonu, finalizer’ların hangi
thread tarafından çalıştırılacağı konusunda hiçbir garanti vermez; bu yüzden bu tür problemleri önlemenin taşınabilir
`(portable)` tek yolu, finalizer kullanmaktan kaçınmaktır. Bu açıdan cleaner’lar finalizer’lardan biraz daha iyidir,
çünkü sınıf yazarları kendi cleaner thread’leri üzerinde kontrole sahiptir; ancak cleaner’lar yine de background'da,
garbage collector’ın kontrolü altında çalışır, dolayısıyla zamanında temizlik yapılacağına dair bir garanti verilemez.

Spesifikasyon, finalizer veya cleaner’ların zamanında çalışacağına dair garanti vermediği gibi, hiç çalışacaklarına dair
de herhangi bir garanti sunmaz. Bir programın, artık erişilemeyen bazı object’ler üzerinde finalizer veya cleaner’ları
çalıştırmadan sonlanması tamamen mümkündür, hatta büyük olasılıkla gerçekleşebilir. Bu nedenle, persistent state'i
güncellemek için asla finalizer veya cleaner’a güvenmemelisin. Örneğin, bir database gibi paylaşılan bir resource
üzerindeki persistent lock'u serbest bırakmak için finalizer veya cleaner’a güvenmek, tüm distributed system'i durma
noktasına getirmek için “iyi” bir yoldur.

`System.gc` ve `System.runFinalization` metodlarının cazibesine kapılma. Bu metodlar, finalizer veya cleaner’ların
çalıştırılma olasılığını artırabilir; ancak bunu garanti etmezler. Bir zamanlar bu garantiyi sunduğu iddia edilen iki
metod vardı: `System.runFinalizersOnExit` ve onun “kötü ikizi” olan `Runtime.runFinalizersOnExit`. Bu metodlar ciddi
şekilde hatalıdır ve onlarca yıldır kullanımdan kaldırılmıştır (deprecated).

Finalizer’larla ilgili bir diğer problem, finalization sırasında uncaught bir exception fırlatılırsa bunun yok sayılması
ve o object'in finalization’ının sonlanmasıdır `[JLS, 12.6]` Yakalanmayan `(uncaught)` exception’lar, diğer object'lerin
corrupt state'de kalmasına yol açabilir. Başka bir thread böyle corrupt bir object'i kullanmaya çalışırsa, keyfi ve
belirlenemez `(nondeterministic)` behavior'lar ortaya çıkabilir. Normalde yakalanmayan `(uncaught)` bir exception
thread’i terminate eder ve `stack trace` yazdırır; ancak finalizer içinde oluşursa, uyarı bile vermez. Cleaner’lar bu
soruna sahip değildir çünkü cleaner kullanan kütüphane kendi thread’i üzerinde kontrole sahiptir.

Finalizer ve cleaner kullanmanın ciddi bir performans maliyeti vardır. Kendi makinemde, basit bir `AutoCloseable` object
oluşturmak, `try-with-resources` ile kapatmak ve garbage collector’ın onu geri kazanması `(reclaim)` yaklaşık `12 ns`
sürüyor. Bunun yerine bir finalizer kullanmak ise süreyi `550 ns`’ye çıkarıyor. Başka bir deyişle, finalizer’lı object
oluşturup yok etmek yaklaşık 50 kat daha yavaştır. Bunun başlıca sebebi, finalizer’ların etkili garbage collection’ı
engellemesidir. Cleaner’lar, sınıfın tüm instance’larını temizlemek için kullanıldığında finalizer’larla benzer
hızdadır (kendi makinemde instance başına yaklaşık 500 ns), ancak sadece bir güvenlik ağı `(safety net)` olarak
kullanıldıklarında çok daha hızlıdırlar, aşağıda tartışıldığı gibi. Bu durumda, kendi makinemde bir object oluşturmak,
temizlemek ve yok etmek yaklaşık `66 ns` sürüyor; yani kullanmasan bile bir güvenlik ağı `(safety net)` sigortası için
beş kat (elli değil) bir maliyet ödüyorsun.

Finalizer’ların ciddi bir güvenlik sorunu vardır: sınıfını finalizer attack'larına açarlar. Finalizer saldırısının
ardındaki fikir basittir: Eğer bir exception constructor’dan veya onun serialization equivalent'ları olan `readObject`
ve `readResolve` metotlarından fırlatılırsa, kötü niyetli bir subclass’ın finalizer’ı, “doğmadan ölmüş” olması gereken
kısmen oluşturulmuş object üzerinde çalışabilir. Bu finalizer, object'e ait referansı static bir field'de tutabilir ve
böylece object'in garbage collected olmasını engeller. Kötü biçimlendirilmiş `(malformed)` object record edildikten
sonra, var olmaması gereken bu object üzerinde keyfi metotları invoke etmek oldukça kolay hale gelir. Bir
constructor’dan exception fırlatmak, normalde bir object'in oluşturulmasını engellemek için yeterlidir; ancak
finalizer’lar olduğunda bu durum geçerli değildir. Bu tür saldırılar ciddi sonuçlar doğurabilir. Final sınıflar,
finalizer saldırılarına karşı immune'dur çünkü hiç kimse final bir sınıfın kötü niyetli bir subclass’ını yazamaz.
Finalizer saldırılarına karşı `non-final` sınıfları korumak için, hiçbir şey yapmayan `final` bir `finalize` metodu yaz.

Dosyalar veya thread’ler gibi termination'ı gereken resource'ları encapsulate eden object’leri olan bir sınıf için
finalizer veya cleaner yazmak yerine ne yapmalısın? Sınıfın `AutoCloseable` interface'ini implemente etsin ve
client’larından, her instance artık gerekmediğinde `close` metodunu çağırmalarını zorunlu kıl; genellikle istisnalarla
karşılaşılsa bile termination'ı garanti altına almak için `try-with-resources` kullanılır. Bahsetmeye değer bir detay,
instance’ın kendisinin kapatılıp kapatılmadığını takip etmesi gerektiğidir: `close` metodu, object’in artık geçerli
olmadığını bir field'de record altına almalı ve diğer metotlar, object kapatıldıktan sonra çağrılırsa bu alanı kontrol
ederek `IllegalStateException` fırlatmalıdır.

Peki, cleaner’lar ve finalizer’lar ne işe yarar, ya da işe yararlar mı? Belki de iki meşru `(legitate)` kullanımları
vardır. Birincisi, bir resource'un sahibi `close` metodunu call etmeyi unuttuğunda devreye giren bir güvenlik ağı
`(safety net)` olarak görev yapmalarıdır. Cleaner veya finalizer’ın zamanında (ya da hiç) çalışacağı garantisi olmasa
da, client fail ise resource'u geç de olsa serbest bırakmak hiç bırakmamaktan iyidir. Böyle bir güvenlik ağı
`(safety net)` finalizer’ı yazmayı düşünüyorsan, korumanın maliyetine değip değmediğini iyice düşün. Bazı Java library
sınıfları, örneğin `FileInputStream`, `FileOutputStream`, `ThreadPoolExecutor` ve `java.sql.Connection`, güvenlik ağı
`(safety net)` olarak çalışan finalizer’lara sahiptir.

Cleaner’ların ikinci meşru `(legitate)` kullanımı, native peer’lere sahip object’lerle ilgilidir. Native peer, normal
bir object’in native metotlar aracılığıyla delegate ettiği native `(non-java)` bir object'dir. Native peer normal bir
object olmadığından, garbage collector onu bilmez ve Java peer’ı geri kazanıldığında `(reclaim)` onu serbest bırakamaz.
Performans kabul edilebilir seviyedeyse ve native peer kritik resource'lar tutmuyorsa, bu task için cleaner veya
finalizer uygun bir yöntem olabilir. Performans unacceptable ya da native peer hemen geri kazanılması `(reclaim)`
gereken resource'lar tutuyorsa, daha önce bahsedildiği gibi sınıfın bir `close` metoduna sahip olması gerekir.

Cleaner’ların kullanımı biraz karmaşıktır. Aşağıda bu özelliği gösteren basit bir Room sınıfı bulunmaktadır. Room geri
kazanılmadan `(reclaimed)` önce temizlenmeli olduğunu varsayalım. Room sınıfı `AutoCloseable` interface'ini implemente
eder; Automatic cleaning güvenlik ağı `(safety net)` olarak bir cleaner kullanması ise sadece bir implementasyon
detayıdır. Finalizer’ların aksine, cleaner’lar bir sınıfın public API’sini kirletmez:

```
// Cleaner’ı safety net olarak kullanan bir AutoCloseable sınıfı
class Room implements AutoCloseable {
    private static final Cleaner cleaner = Cleaner.create();

    // Temizlenmesi gereken resource. Room’a referans vermemeli!
    private static class State implements Runnable{
        int numJunkPiles; // Bu odadaki atık yığınlarının sayısı

        public State(int numJunkPiles){
            this.numJunkPiles = numJunkPiles;
        }

        // close metodu veya cleaner tarafından invoke edilir
        @Override
        public void run() {
            System.out.println("Cleaning room");
            numJunkPiles = 0;
        }
    }

    // Bu room'un state'i, cleanable ile paylaşılan
    private final State state;

    // Bizim cleanable’ımız. Room GC’ye uygun olduğunda temizler.
    private final Cleaner.Cleanable cleanable;

    public Room(int numJunkPiles){
        state = new State(numJunkPiles);
        cleanable = cleaner.register(this,state);
    }

    @Override
    public void close() throws Exception {
        cleanable.clean();
    }
}
```

Static nested `State` sınıfı, cleaner’ın Room'u temizlemek için ihtiyaç duyduğu resource'ları tutar. Bu case'de, Room'da
ki dağınıklık miktarını represent eden `numJunkPiles` field'idir. Daha gerçekçi olarak, native peer’a pointer eden bir
`final long` olabilir. State, Runnable interface'ini implemente eder ve run metodu en fazla bir kez call edilir; bu,
Room constructor'ında State instance'ımızı cleaner’a register ettiğimiz de aldığımız Cleanable tarafından yapılır. `run`
metodunun call edilmesi iki şeyden biri tarafından trigger edilir: Genellikle, Room’un `close` metodunun Cleanable’ın
`clean` metodunu çağırmasıyla trigger edilir. Eğer client, bir Room instance'ını garbage collection için uygun hale
geldiğinde `close` metodunu çağırmayı unutursa, `cleaner` (umarız ki) State’in `run` metodunu çağıracaktır.

Bir State instance'ının kendi Room instance'ına referans vermemesi kritik önemdedir. Eğer referans verseydi, bu durum
Room instance'ının garbage collection için uygun hale gelmesini (ve otomatik temizlenmesini) engelleyecek bir döngü
oluştururdu. Bu yüzden, State static nested sınıf olmalıdır; çünkü nonstatic nested sınıflar, bulundukları enclosing
instance'larına referans içerirler. Benzer şekilde, lambda kullanmak da tavsiye edilmez çünkü lambda expression'ları
kolayca bulundukları object'lere referans capture edebilir.

Daha önce söylediğimiz gibi, Room’un cleaner’ı sadece bir güvenlik ağı `(safety net)` olarak kullanılır. Eğer client’lar
tüm Room instantiation'larını `try-with-resources` blockları içinde yaparsa, otomatik temizlemeye hiç ihtiyaç kalmaz. Bu
düzgün davranan `(well-behaved)` client, bu behavior'u gösterir:

```
class Adult{
    public static void main(String[] args) {
        try(Room myRoom = new Room(7)){
            System.out.println("Goodbye");
        }
    }
}
```

Beklediğiniz gibi, `Adult` programını çalıştırmak "Goodbye" yazdırır, ardından "Cleaning room" mesajı gelir. Peki,
Room'unu hiç temizlemeyen bu kötü davranan `(ill-behaved)` program ne olacak?

```
public class Teenager {
    public static void main(String[] args) {
        new Room(99);
        System.out.println("Peace out");
    }
}
```

Bunun "Peace out" yazdırmasını, ardından "Cleaning room" mesajını bekleyebilirsiniz, ama benim makinemde asla "Cleaning
room" yazdırmıyor; sadece çıkıyor. İşte daha önce bahsettiğimiz o unpredictability. Cleaner spesifikasyonu şöyle der:
“System.exit sırasında cleaner’ların behavior'u implementation specific'dir.” Cleaning action'larının invoke edilip
edilmeyeceğine dair herhangi bir garanti verilmez. Spec bunu açıkça belirtmese de, normal program çıkışı için de aynı
durum geçerlidir. Kendi makinemde, Teenager’ın main metoduna `System.gc()` satırını eklemek, çıkıştan önce "Cleaning
room" yazdırması için yeterli oluyor; ancak bu behavior'un senin makinenizde de aynı şekilde gerçekleşeceğine dair
hiçbir garanti yok.

Özetle, cleaner'ları veya Java 9 öncesindeki sürümlerde finalizer’ları, sadece bir güvenlik ağı `(safety net)` olarak
veya kritik olmayan native resource'ları terminate etmek için kullanın; başka durumlarda kullanmaktan kaçının. Yine de,
belirsizliklere ve performans etkilerine karşı dikkatli olun.