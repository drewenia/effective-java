# Prefer try-with-resources to try-finally

Java library'leri, çoğu zaman bir `close` method'u invoke edilerek manuel olarak kapatılması gereken birçok resource
içerir. Örnekler arasında `InputStream`, `OutputStream` ve `java.sql.Connection` yer alır. Resource'ları kapatmak çoğu
zaman client'lar tarafından göz ardı edilir ve bu da öngörülebilir şekilde ciddi performans sonuçlarına yol açar.
Bu resource'ların birçoğu güvenlik ağı `(safety net)` olarak finalizer'ları kullansa da, finalizer'lar pek iyi çalışmaz.

Geçmişte, bir resource'un bir exception veya return durumunda bile düzgün şekilde kapatılmasını garanti altına almanın
en iyi yolu `try-finally` statement'ını kullanmaktı:

```
// try-finally – Artık resource'ları kapatmanın en iyi yolu değil!
static String firstLineOfFile(String path) throws IOException {
    BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
    try {
        return bufferedReader.readLine();
    } finally {
        bufferedReader.close();
    }
}
```

Bu kötü görünmeyebilir, ancak ikinci bir resource eklediğinizde işler daha da kötüleşir:

```
private static final int BUFFER_SIZE = 8 * 1024;

// try-finally, birden fazla resource ile kullanıldığında çirkin (karmaşık) hale gelir.!
static void copy(String src, String dst) throws IOException {
    InputStream inputStream = new FileInputStream(src);
    try {
        OutputStream outputStream = new FileOutputStream(dst);
        try {
            byte[] buf = new byte[BUFFER_SIZE];
            int n;
            while ((n = inputStream.read(buf)) >= 0) {
                outputStream.write(buf, 0, n);
            }
        } finally {
            outputStream.close();
        }
    } finally {
        inputStream.close();
    }
}
```

İnanması zor olabilir, ama iyi programcılar bile bunu çoğu zaman yanlış yapıyordu. Önceki iki kod örneğinde gösterildiği
gibi `try-finally` statement'larıyla resource kapatma işlemi doğru yapılsa bile, bunun ince bir eksikliği vardır. Hem
`try` bloğundaki hem de `finally` bloğundaki kod, `exception` fırlatabilir. Örneğin, `firstLineOfFile` metodunda,
`readLine` call'u, altta yatan `(underlying)` fiziksel cihazdaki bir arıza nedeniyle bir exception fırlatabilir ve
ardından `close` çağrısı da aynı sebepten dolayı fail olabilir. Bu durumda, ikinci exception birincisini tamamen yok
eder. Exception stack trace'inde ilk exception'ın record'u bulunmaz; bu da gerçek sistemlerde debugging'i oldukça
zorlaştırabilir — genellikle problemi teşhis etmek için görmek istediğiniz ilk exception'dır. İkinci exception'ı birinci
lehine baskılamak `(suppress)` için kod yazmak mümkün olsa da, neredeyse hiç kimse bunu yapmadı çünkü bu çok fazla
ayrıntı ve karmaşıklık yaratıyordu.

Tüm bu problemler, Java 7’nin `try-with-resources` statement'ını `[JLS, 14.20.3]` tanıtmasıyla bir hamlede çözüldü. Bu
construct'la kullanılabilir olması için, bir resource `AutoCloseable` interface'ini implement etmelidir; bu interface
tek bir void döndüren `close` metodundan oluşur. Java library'lerinde ki ve 3rd party library'lerde ki birçok class ve
interface artık `AutoCloseable` interface'ini implement eder veya onu `extends` eder. Eğer kapatılması gereken bir
resource'u represent eden bir class yazıyorsanız, bu class da `AutoCloseable` interface'ini implement etmelidir.

İlk örneğimizin `try-with-resources` kullanılarak yazılmış hali şu şekildedir:

```
// try-with-resources – resource’ları kapatmanın en iyi yolu!
static String firstLineOfFile(String path) throws IOException {
    try(BufferedReader br = new BufferedReader(new FileReader(path))){
        return br.readLine();
    }
}
```

Ve işte ikinci örneğimizin `try-with-resources` kullanılarak nasıl göründüğü:

```
private static final int BUFFER_SIZE = 8 * 1024;

// try-with-resources ile birden fazla resource kullanımı – kısa ve öz
static void copy(String src, String dst) throws IOException {
    try (
            InputStream in = new FileInputStream(src);
            OutputStream out = new FileOutputStream(dst);
    ) {
        byte[] buffer = new byte[BUFFER_SIZE];
        int n;
        while ((n = in.read(buffer)) >= 0) {
            out.write(buffer,0,n);
        }
    }
}
```

`try-with-resources` sürümleri yalnızca daha kısa ve okunabilir olmakla kalmaz, aynı zamanda çok daha iyi diagnostic
sağlar. `firstLineOfFile` metodunu ele alalım. Eğer hem `readLine` call'u hem de `(invisible)` `close` bir exception
fırlatırsa, ikinci exception (yani `close` sırasında oluşan) birinci lehine bastırılır `(suppressed)`. Aslında, görmek
istediğiniz exception’ı koruyabilmek için birden fazla exception bastırılabilir `(suppressed)`. Bastırılan
`(suppressed)` exception’lar sadece discard edilmez; Onlar, bastırıldıklarını belirtilerek `stack trace` içinde
yazdırılırlar. Ayrıca, Java 7’de Throwable sınıfına eklenen `getSuppressed` metodu ile bu bastırılmış exception'lara
programmatically olarak da erişebilirsiniz.

`try-with-resources` statement'larına, normal `try-finally` statement'larında olduğu gibi catch blokları
ekleyebilirsiniz. Bu, kodunuzu ekstra bir nesting yapı ile karmaşıklaştırmadan exception'ları handle etmenizi sağlar.
Biraz yapay bir örnek olarak, işte dosyayı açamaz veya okuyamazsa default bir değer döndüren, exception fırlatmayan
`firstLineOfFile` metodumuzun bir versiyonu:

```
// catch bloğuyla birlikte try-with-resources
static String firstLineOfFile(String path, String defaultValue) {
    try (BufferedReader bufferedReader = new BufferedReader(new FileReader(path))) {
        return bufferedReader.readLine();
    } catch (IOException e) {
        return defaultValue;
    }
}
```

Ders açık: Kapatılması gereken resource’larla çalışırken her zaman `try-finally` yerine `try-with-resources` kullanın.
Ortaya çıkan kod daha kısa ve anlaşılır olur, ayrıca oluşturduğu exception’lar daha faydalıdır. `try-with-resources`
statement'i, kapatılması gereken resource'ları kullanan doğru kodlar yazmayı kolaylaştırır; bu, `try-finally` ile
neredeyse imkânsızdı.