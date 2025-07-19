# Favor static member classes over nonstatic

# Nonstatic yerine static member class'ları tercih edin.

Nested class, bir başka sınıfın içinde tanımlanan sınıftır. Nested class, yalnızca kapsayan `(enclosing)` sınıfına
hizmet etmek için var olmalıdır. Eğer bir nested class başka bir context'de faydalı olacaksa, o zaman `top-level` sınıf
olmalıdır. Dört tür nested class vardır: `static member class`’lar, `nonstatic member class`’lar, `anonymous class`’lar
ve `local class`’lar. `static member class`'lar dışındaki tüm nested class’lar inner class olarak bilinir. Bu bölüm size
hangi tür nested class’ın ne zaman ve neden kullanılacağını anlatır.

Static member class, en basit nested class türüdür. Bu sınıf, başka bir sınıfın içinde declare edilmiş ve kapsayan
`(enclosing)` sınıfın `private` olanlar da dahil tüm member'larına erişebilen sıradan `(ordinary)` bir sınıf olarak
düşünülmelidir. Static member class, kapsayan `(enclosing)` sınıfının static bir member'ıdır ve diğer static member'lar
ile aynı accessibility kurallarına tabidir. Eğer `private` olarak tanımlanmışsa, yalnızca kapsayan `(enclosing)` sınıf
içinde erişilebilir, vb. şekilde devam eder.

Static member class’ların yaygın kullanım alanlarından biri, yalnızca outer sınıfıyla birlikte kullanıldığında faydalı
olan public helper class olarak görev yapmasıdır. Örneğin, bir hesap makinesinin desteklediği operation’ları tanımlayan
bir enum’u ele alalım: Operation enum’u, Calculator sınıfının public static member class’ı olmalıdır. Calculator’ın
client’ları daha sonra operation’lara `Calculator.Operation.PLUS` ve `Calculator.Operation.MINUS` gibi isimlerle
referans verebilir.

Sözdizimsel olarak, `static` ve `nonstatic` member class’lar arasındaki tek fark, static member class’ların
declaration'larında `static` keyword'unun bulunmasıdır. Sözdizimsel benzerliğe rağmen, bu iki tür nested class oldukça
farklıdır. `Nonstatic member class`’ın her instance'ı, dolaylı olarak `(implicitly)` kapsayan `(enclosing)` sınıfın bir
instance'ı ile ilişkilidir. `Nonstatic member class`’ın instance method’ları içinde, kapsayan `(enclosing)` instance
üzerindeki method’ları çağırabilir veya `enclosingInstance.this` yapısını kullanarak kapsayan `(enclosing)` instance'a
referans alabilirsiniz `[JLS, 15.8.4]`. Eğer bir nested class instance'ı, kapsayan `(enclosing)` sınıfın instance'ından
bağımsız olarak var olabiliyorsa, nested class `static member class` olmalıdır: kapsayan `(enclosing)` bir instance
olmadan nonstatic member class’ın instance'ını oluşturmak imkansızdır.

Nonstatic member class instance'i oluşturulduğunda, kapsayan `(enclosing)` instance'la olan ilişki kurulur ve sonrasında
değiştirilemez. Normalde, ilişki kapsayan `(enclosing)` sınıfın bir instance methodu içinden nonstatic member class
constructor’ı invoke edilerek otomatik olarak kurulur. Nadir olmakla birlikte, ilişki manuel olarak
`enclosingInstance.new MemberClass(args)` expression'ı kullanılarak kurulabilir. Beklendiği gibi, bu ilişki nonstatic
member class instance'ında yer kaplar ve construction süresine ek yük getirir.

Nonstatic member class’ın yaygın kullanımlarından biri, outer sınıfın bir instance'ının alakasız başka bir sınıfın
instance'ı olarak görülmesini sağlayan `Adapter` tanımlamaktır. Örneğin, Map interface’inin implementasyonları
genellikle collection view'lerini implement etmek için nonstatic member class’lar kullanır; bunlar Map’in `keySet`,
`entrySet` ve `values` method’ları tarafından döndürülür. Benzer şekilde, Set ve List gibi collection interface’lerinin
implementasyonları, iterator’larını implement etmek için genellikle nonstatic member class’lar kullanır:

```
// Typical use of a nonstatic member class
public class MySet<E> extends AbstractSet<E> {
    ... // Bulk of the class omitted
    
    @Override 
    public Iterator<E> iterator() {
        return new MyIterator();
    }

    private class MyIterator implements Iterator<E> {
        ...
    }
}
```

Eğer kapsayan `(enclosing)` instance'a erişim gerektirmeyen bir member class tanımlarsanız, her zaman deklarasyonunda
`static` modifier’ını kullanarak onu static member class yapın. Bu modifier’ı atlarsanız, her instance kapsayan
`(enclosing)` instance'a gizli ve gereksiz bir referans içerir. Daha önce belirtildiği gibi, bu referansı saklamak zaman
ve alan harcar. Daha da önemlisi, bu durum kapsayan `(enclosing)` instance'ın aslında garbage collection için uygun
olduğu halde tutulmasına neden olabilir. Ortaya çıkan memory leak felaket sonuçlara yol açabilir. Bu referans görünmez
olduğundan, genellikle tespit edilmesi zordur.

Private static member class’ların yaygın bir kullanım alanı, kapsayan `(enclosing)` sınıf tarafından represent edilen
object'in component'lerini represent etmektir. Örneğin, key'leri value'lar ile eşleyen `(associates)` bir Map
instance'ını düşünün. Birçok Map implementasyonu, map içindeki her `key-value` pair'i için internal bir Entry object'ine
sahiptir. Her entry bir map ile ilişkili olsa da, entry üzerindeki method’lar `(getKey, getValue ve setValue)` map’e
erişim gerektirmez. Bu nedenle, entry’leri represent etmek için `nonstatic member class` kullanmak israf olurdu: en
iyisi, `private static member class` kullanmaktır. Eğer entry tanımında static modifier’ını kazara atlaranız, map yine
çalışır; ancak her entry, map’e gereksiz bir referans içerir ve bu hem alan hem de zaman israfına yol açar.

Eğer söz konusu class, exported bir sınıfın public veya protected bir member’ıysa, static ile nonstatic arasında doğru
seçim yapmak iki kat daha önemlidir. Bu case de, member class exported bir API element'idir ve sonraki bir sürümde
nonstatic’tan static member class’a dönüştürülemez; aksi takdirde backward compatibility ihlal edilir.

Beklendiği gibi, anonymous sınıfın ismi yoktur. Kapsayan `(enclosing)` sınıfın bir member'ı değildir. Diğer member'lar
ile birlikte declare edilmek yerine, kullanım noktasında simultaneously olarak declare edilir ve instantiate edilir.
Anonymous sınıflar, bir expression'ın legal olduğu herhangi bir kod noktasında kullanılabilir. Anonymous sınıflar, ancak
nonstatic bir context'de ortaya çıktıklarında kapsayan `(enclosing)` instance'lara sahiptir. Ama static bir context'de
ortaya çıksalar bile, final primitive veya constant expression'lar ile initialize edilen string field’lar gibi constant
variable'lar dışında hiçbir static member'a sahip olamazlar `[JLS, 4.12.4]`.

Anonymous sınıfların kullanılabilirliği konusunda birçok sınırlama vardır. Onları yalnızca declare edildikleri noktada
instantiate edebilirsiniz. Sınıfı adlandırmanız gereken `instanceof` testleri veya başka herhangi bir işlemi
gerçekleştiremezsiniz. Anonymous bir sınıfı birden fazla interface’i implement edecek şekilde veya aynı anda hem bir
sınıfı extend edip hem bir interface’i implement edecek şekilde declare edemezsiniz. Anonymous bir sınıfın client’ları,
yalnızca super type'ından inherit aldığı member'ları çağırabilir; başka member'ları çağırmaları mümkün değildir.
Anonymous sınıflar expression'ların ortasında yer aldıkları için kısa tutulmaları gerekir — yaklaşık on satır veya daha
az—aksi takdirde okunabilirlik zarar görür.

Java’ya lambda eklenmeden önce, anonymous sınıflar küçük function object’leri ve process object’leri anında oluşturmanın
tercih edilen yoluydu; ancak artık lambda expression'ları tercih edilmektedir. Anonymous sınıfların bir diğer yaygın
kullanım alanı da `static factory method`’ların implementasyonudur (Bkz. Item 20’deki `intArrayAsList`).

Local class’lar, dört tür nested class arasında en az kullanılanlardır. Bir local class, neredeyse bir local variable’ın
declare edildiği her yerde declare edilebilir ve aynı scope kurallarına tabidir. Local class’lar, diğer nested class
türlerinin her biriyle ortak bazı attribute'lere sahiptir. Member class’lar gibi, isimleri vardır ve tekrar tekrar
kullanılabilirler. Anonymous sınıflar gibi, yalnızca `nonstatic` bir context'de define edildiklerinde kapsayan
`(enclosing)` instance'lara sahiptirler ve static member içeremezler. Ve anonymous sınıflar gibi, okunabilirliği
bozmamak için kısa tutulmalıdırlar.

Özetle, dört farklı nested class türü vardır ve her birinin kendine özgü kullanım alanı vardır. Eğer bir nested class,
single bir methodun dışından görünür olmalıysa veya bir method içinde rahatça sığmayacak kadar uzunsa, member class
kullanın. Eğer her member class instance'ının kapsayan `(enclosing)` instance'a referans ihtiyacı varsa, onu nonstatic
yapın; aksi takdirde, static yapın. Sınıfın bir methodun içinde olduğunu varsayarsak, sadece tek bir yerden instance
oluşturmanız gerekiyorsa ve sınıfı tanımlayan önceden var olan bir tür varsa, anonymous sınıf yapın; aksi takdirde,
local class yapın.