# Non-Reifiable Types

Type Erasure bölümü, compiler'ın type parameter'lar ve type argument'larla ilgili bilgileri remove ettiği process'i
açıklar. Type erasure, `varargs` olarak da bilinen variable arguments method'larında, varargs formal parameter
non-reifiable bir type olduğunda bazı sonuçlara yol açar.

## Non-Reifiable Types

Reifiable type, type bilgisinin runtime sırasında tamamen erişilebilir olduğu bir type'tır. Buna primitive'ler,
non-generic type'lar, raw type'lar ve unbound wildcard invocation'ları dahildir. Non-reifiable type'lar, compile-time
sırasında type erasure tarafından bilgisi kaldırılmış olan type'lardır — unbounded wildcard olarak tanımlanmamış generic
type invocation'larıdır. Non-reifiable type, runtime sırasında tüm bilgilerine sahip değildir. Non-reifiable type
örnekleri `List<String>` ve `List<Number>`'dır. JVM, bu type'lar arasındaki farkı runtime sırasında ayırt edemez.
Bazı durumlarda non-reifiable type'lar kullanılamaz: örneğin, `instanceof` expression'nın da veya bir array içinde
element olarak.

## Heap Pollution

Heap pollution, parameterized type'a sahip bir variable'ın, o parameterized type olmayan bir object'e referans
verdiğinde oluşur. Bu durum, program compile-time'da unchecked uyarısı veren bir operation gerçekleştirdiğinde oluşur.
Unchecked uyarısı, compile-time'da (compile-time type checking kuralları sınırları içinde) veya runtime'da,
parameterized type içeren bir işlemin (örneğin cast veya method call) doğruluğu doğrulanamıyorsa oluşturulur. Örneğin,
heap pollution, raw type'lar ile parameterized type'ların karıştırılması veya unchecked cast yapılması durumunda oluşur.

Normal durumlarda, tüm kod aynı anda compile edildiğinde, compiler potansiyel heap pollution'a dikkat çekmek için
unchecked uyarısı verir. Kodunuzun bölümlerini ayrı ayrı compile ederseniz, potansiyel heap pollution riski tespit etmek
zorlaşır. Kodunuzun uyarısız compile olmasını sağlarsanız, heap pollution oluşmaz.

## Non-Reifiable Formal Parameter'lara Sahip Varargs Method'larının Potansiyel Güvenlik Açıkları

Vararg input parameter içeren generic method'lar heap pollution'a neden olabilir.

Aşağıdaki ArrayBuilder sınıfını dikkate alın:

```java
class ArrayBuilder {
    public static <T> void addToList(List<T> listArg, T... elements) {
        for (T x : elements) {
            listArg.add(x);
        }
    }

    public static void faultyMethod(List<String>... l) {
        Object[] objectArray = l; // Valid
        objectArray[0] = Arrays.asList(42);
        String s = l[0].get(0);
    }
}
```

Aşağıdaki örnek, HeapPollutionExample, ArrayBuilder sınıfını kullanır:

```java
class HeapPollutionExample {
    public static void main(String[] args) {
        List<String> stringListA = new ArrayList<>();
        List<String> stringListB = new ArrayList<>();

        ArrayBuilder.addToList(stringListA, "Seven", "Eight", "Nine");
        ArrayBuilder.addToList(stringListB, "Ten", "Eleven", "Twelve");

        List<List<String>> listOfStringLists = new ArrayList<>();
        ArrayBuilder.addToList(listOfStringLists, stringListA, stringListB);

        ArrayBuilder.faultyMethod(Arrays.asList("Hello"), Arrays.asList("World"));
    }
}
```

Compile edildiğinde, aşağıdaki uyarı `ArrayBuilder.addToList` methodunun definition'ı tarafından üretilir:

```
warning: [varargs] Possible heap pollution from parameterized vararg type T
```

Compiler bir vararg method ile karşılaştığında, vararg formal parameter’ı bir array’e translate eder. Ancak, Java
programlama dili parameterized type’lardan array oluşturulmasına izin vermez. `ArrayBuilder.addToList` methodunda,
compiler `varargs` formal parameter `T...` elements ifadesini, bir array olan formal parameter `T[] elements` ifadesine
translate eder. Ancak, type erasure nedeniyle, compiler `varargs` formal parameter’ı `Object[] elements` olarak convert
eder. Sonuç olarak, heap pollution olasılığı vardır.

Aşağıdaki statement, `varargs` formal parameter `l` ifadesini Object array olan `objectArgs` değişkenine assign eder:

```
Object[] objectArray = l;
```

Bu statement, potansiyel olarak heap pollution’a neden olabilir. Varargs formal parameter `l`’in parameterized type’ı
ile eşleşmeyen bir value, objectArray değişkenine assign edilebilir ve dolayısıyla `l` değişkenine de assign edilebilir.
Ancak, compiler bu statement için unchecked warning üretmez. Compiler, varargs formal parameter `List<String>... l`
ifadesini formal parameter `List[] l` ifadesine translate ederken zaten bir warning üretmiştir. Bu statement geçerlidir;
`l` değişkeninin tipi `List[]` olup, bu da `Object[]`’in bir subtype’ıdır.

Sonuç olarak, compiler aşağıdaki statement’ta gösterildiği gibi, herhangi bir türdeki List object’ini objectArray
array’inin herhangi bir array component’ine assign etmeniz durumunda warning veya error üretmez:

```
objectArray[0] = Arrays.asList(42);
```

Bu statement, objectArray array’inin ilk array component’ine Integer türünden bir object içeren bir List object’ini
assign eder.

`ArrayBuilder.faultyMethod` methodunu aşağıdaki statement ile invoke ediyorsunuz:

```
ArrayBuilder.faultyMethod(Arrays.asList("Hello!"), Arrays.asList("World!"));
```

Runtime sırasında, JVM aşağıdaki statement’ta ClassCastException fırlatır:

```
// ClassCastException thrown here
String s = l[0].get(0);
```

`l` değişkeninin ilk array component’inde depolanan object’in tipi `List<Integer>` iken, bu statement `List<String>`
tipinde bir object beklemektedir.

## Non-Reifiable Formal Parameter’lara sahip Varargs Method’larından Kaynaklanan Uyarıları Önleme

Eğer parameterized type parametreleri olan bir varargs method declare eder ve methodun body'sinin varargs formal
parameter’ının yanlış kullanımı nedeniyle ClassCastException veya benzeri bir exception fırlatmadığından emin olursanız,
compiler’ın bu tür varargs methodlar için ürettiği warning’i aşağıdaki annotation’ı static ve non-constructor method
declaration’larına ekleyerek önleyebilirsiniz:

```
@SafeVarargs
```

`@SafeVarargs` annotation’ı, methodun contract'ının belgelenmiş bir parçasıdır; bu annotation, methodun
implementasyonunun varargs formal parameter’ını yanlış kullanmayacağını garanti eder.

Ayrıca, daha az tercih edilen bir yöntem olarak, method deklarasyonuna aşağıdakini ekleyerek bu tür warning’leri
bastırmak da mümkündür:

```
@SuppressWarnings({"unchecked", "varargs"})
```

Ancak, bu yaklaşım method’un call site’ından kaynaklanan warning’leri bastırmaz.