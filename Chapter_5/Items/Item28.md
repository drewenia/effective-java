# Prefer lists to arrays

Arrays, generic types'dan iki önemli şekilde farklıdır. İlk olarak, arrays covariant'dır (teoriyi ifade eden, sergileyen
veya bununla ilgili olan). Bu korkutucu görünen kelime basitçe şunu ifade eder: Eğer Sub, Super'ın bir subtype'ı ise, o
zaman array type'ı `Sub[]` array type'ı `Super[]`'in bir subtype'ıdır. Generics ise, buna karşılık, invariant'tır:
Herhangi iki farklı type olan `Type1` ve `Type2` için, `List<Type1>` ne `List<Type2>`'nin bir subtype'ıdır ne de super
type'ıdır `[JLS, 4.10]`. Generics'in yetersiz olduğunu düşünebilirsiniz, ancak tartışmalı olarak asıl yetersiz olan
arrays'tir. Bu code fragment geçerlidir:

```
// Fails at runtime!
Object[] objectArray = new Long[1];
objectArray[0] = "I don't fit in"; // Throws ArrayStoreException
```

ama bu geçerli değildir:

```
// Won't compile!
List<Object> ol = new ArrayList<Long>(); // Incompatible types
ol.add("I don't fit in");
```

Her iki durumda da bir String'i bir Long container'a koyamazsınız, ancak bir array ile hata yaptığınızı runtime'da
öğrenirsiniz; Bir list ile ise bunu compile time'da öğrenirsiniz. Elbette, bunu compile time'da öğrenmeyi tercih
edersiniz. Arrays ve generics arasındaki ikinci büyük fark, arrays'in reified olmasıdır `[JLS, 4.7]`. Bu, arrays'in
element type'ını runtime'da bildiği ve uyguladığı anlamına gelir. Daha önce belirtildiği gibi, bir String'i bir array of
Long içine koymaya çalışırsanız, bir `ArrayStoreException` alırsınız. Buna karşılık, generics erasure ile implement
edilir `[JLS, 4.6]`. Bu, type constraint'larını yalnızca compile time'da uyguladıkları ve element type bilgilerini
runtime'da discard ettikleri (ya da erase ettikleri) anlamına gelir. Erasure, generic types'in generics kullanmayan
legacy code ile serbestçe birlikte çalışmasına olanak tanıdı ve Java 5'te generics'e sorunsuz bir geçiş sağladı.

Bu temel farklar nedeniyle, arrays ve generics iyi bir şekilde bir arada çalışmaz. Örneğin, bir generic type, bir
parameterized type veya bir type parameter'dan bir array oluşturmak geçersizdir. Bu nedenle, bu array creation
expression'larının hiçbiri geçerli değildir: `new List<E>[]`, `new List<String>[]`, `new E[]`. Hepsi compile time'da
generic array creation hatalarıyla sonuçlanacaktır.

Neden bir generic array oluşturmak illegal'dir? Çünkü typesafe değildir. Eğer legal olsaydı, aksi hâlde doğru olan bir
programda compiler tarafından üretilen cast'ler runtime'da ClassCastException ile başarısız olabilirdi. Bu, generic type
system tarafından sağlanan temel `(fundamental)` garantiyi ihlal ederdi.

Bunu daha somut hale getirmek için, aşağıdaki code fragment'a bakın:

```
// Neden generic array creation illegal'dir – compile edilmez!
List<String>[] stringLists = new List<String>[1]; // (1)
List<Integer> intList = List.of(42); // (2)
Object[] objects = stringLists; // (3)
objects[0] = intList; // (4)
String s = stringLists[0].get(0); // (5)
```

1 - generic bir array oluşturmasının geçerli olduğunu varsayalım.

2 - Single bir element içeren bir `List<Integer>` oluşturur ve initialize eder.

3 - `List<String>` array'ini bir Object array variable'ına atar; bu geçerlidir çünkü arrays covariant'tır.

4 - `List<Integer>`'ı Object array'in tek elementine atar; bu başarılıdır çünkü generics erasure ile implement
edilmiştir: Bir `List<Integer>` instance'ının runtime type'ı basitçe List'tir ve bir `List<String>[]` instance'ının
runtime type'ı `List[]`'tir, bu yüzden bu atama bir `ArrayStoreException` oluşturmaz. Şimdi işimiz zor. Yalnızca
`List<String>` instance'larını tutacak şekilde tanımlanmış bir array'e bir `List<Integer>` örneği koyduk.

5 - bu array'deki tek listenin tek elementini alıyoruz. Compiler, alınan elementi otomatik olarak String'e cast eder,
ancak o bir Integer olduğu için runtime'da `ClassCastException` alırız. Bunun olmasını önlemek için, generic array
oluşturan birinci satır compile-time hatası üretmelidir.

`E`, `List<E>` ve `List<String>` gibi type'lar teknik olarak `nonreifiable` types olarak bilinir `[JLS, 4.7]`. Sezgisel
olarak ifade etmek gerekirse, `non-reifiable` bir type, runtime representation'ınının compile-time
representation'ınından daha az bilgi içerdiği bir typedır. Erasure nedeniyle, `reifiable` olan tek parameterized
type'lar `List<?>` ve `Map<?,?>` gibi `unbounded wildcard type`'lardır. Unbounded wildcard type'larından array
oluşturmak legal'dir, ancak nadiren kullanışlıdır.

Generic array creation yasağı can sıkıcı olabilir. Bu, örneğin, bir generic collection'ın element type'ından bir array
döndürmesinin genellikle mümkün olmadığı anlamına gelir. Ayrıca bu, generic type'larla birlikte varargs method'ları
kullandığınızda kafa karıştırıcı uyarılar almanız anlamına da gelir. Bunun nedeni, her varargs method invocation'ında
varargs parameter'ları tutmak için bir array oluşturulmasıdır. Bu array'in element type'ı reifiable değilse, bir uyarı
alırsınız. Bu sorunu ele almak için SafeVarargs annotation kullanılabilir.

Bir generic array creation hatası veya array type'a yapılan bir cast'te unchecked cast uyarısı aldığınızda, en iyi çözüm
genellikle array type `E[]` yerine collection type `List<E>`'i tercih etmektir. Biraz kısalık ya da performanstan
feragat edebilirsiniz, ancak karşılığında daha iyi `type safety` ve `interoperability` (birlikte çalışabilirlik) elde
edersiniz.

Örneğin, bir collection alan bir constructor'a ve collection'dan random seçilmiş bir elementi döndüren tek bir method'a
sahip bir `Chooser` sınıfı yazmak istediğinizi varsayalım. Constructor'a hangi collection'ı geçerseniz, chooser'ı oyun
zarı, sihirli 8 topu veya Monte Carlo simülasyonu için bir data source olarak kullanabilirsiniz. Generics olmadan basit
bir implementasyon:

```
// generics'e ciddi şekilde ihtiyaç duyan bir sınıf!
public class Chooser {
    private final Object[] choiceArray;

    public Chooser(Collection choices) {
        choiceArray = choices.toArray();
    }

    public Object choose() {
        Random rnd = ThreadLocalRandom.current();
        return choiceArray[rnd.nextInt(choiceArray.length)];
    }
}
```

Bu sınıfı kullanmak için, choose method'unun return value'sunu her çağırdığınızda Object'ten istediğiniz type'a cast
etmeniz gerekir ve eğer type'ı yanlış yaparsanız cast runtime'da başarısız olur. Chooser'ı generic yapmak için
değişiklik yapmaya çalışıyoruz.

```
// Chooser'ı generic yapmak için ilk deneme – compile edilmez.
public class Chooser<T> {
    private final T[] choiceArray;

    public Chooser(Collection<T> choices){
        choiceArray = choices.toArray();
    }

    // choose method unchanged
}
```

Bu sınıfı compile etmeye çalışırsanız, şu hata mesajını alırsınız:

```
Chooser.java:9: error: incompatible types: Object[] cannot be
converted to T[]
choiceArray = choices.toArray();
^
where T is a type-variable:
T extends Object declared in class Chooser
```

Sorun değil, diyorsunuz, Object array'i `T array`'e cast edeceğim:

```
public Chooser(Collection<T> choices){
    choiceArray = (T[]) choices.toArray();
}
```

Bu hatayı ortadan kaldırır, ancak bunun yerine bir uyarı alırsınız:

```
Chooser.java:9: warning: [unchecked] unchecked cast
choiceArray = (T[]) choices.toArray();
^
required: T[], found: Object[]
where T is a type-variable:
T extends Object declared in class Chooser
```

Compiler, programın runtime'da `T`'nin ne tür bir tip olduğunu bilemeyeceği için cast'in güvenliğini garanti edemediğini
söylüyor — hatırlayın, element type bilgisi generics'ten runtime'da silinir (erased). Program çalışacak mı? Evet, ama
compiler bunu kanıtlayamaz. Bunu kendinize kanıtlayabilir, kanıtı bir comment'de belirtebilir ve uyarıyı bir annotation
ile bastırabilirsiniz, ancak uyarının nedenini ortadan kaldırmak daha iyidir. Unchecked cast uyarısını ortadan kaldırmak
için array yerine list kullanın. İşte hata veya uyarı olmadan compile edilen Chooser sınıfının bir versiyonu:

```
// List-based Chooser - typesafe
public class Chooser<T> {
    private final List<T> choiceList;

    public Chooser(Collection<T> choices) {
        choiceList = new ArrayList<>(choices);
    }

    public T choose() {
        Random rnd = ThreadLocalRandom.current();
        return choiceList.get(rnd.nextInt(choiceList.size()));
    }

    public static void main(String[] args) {
        List<Integer> intList = List.of(1, 2, 3, 4, 5, 6);
        Chooser<Integer> chooser = new Chooser<>(intList);
        for (int i = 0; i < 10; i++) {
            Number choice = chooser.choose();
            System.out.println(choice);
        }
    }
}
```

Bu versiyon biraz daha uzun ve belki biraz daha yavaş, ama runtime'da ClassCastException almayacağınızın garantisi için
buna değer.

Özetle, arrays ve generics çok farklı type kurallarına sahiptir. Arrays covariant ve reified'dır; generics ise invariant
ve erased'dir. Bunun sonucu olarak, arrays runtime type safety sağlar ama compile-time type safety sağlamaz; generics
için ise tam tersi geçerlidir. Genel olarak, arrays ve generics iyi mix olmazlar. Eğer arrays ve generics'i mix edip
compile-time hataları veya uyarılar alıyorsanız, ilk tepkiniz arrays'i listelerle değiştirmek olmalıdır.