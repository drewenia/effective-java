# Enforce noninstantiability with a private constructor

# Noninstantiability’i private constructor ile zorunlu kılın.

Bazen yalnızca static method’lar ve static field’ların bir arada bulunduğu bir class yazmak isteyebilirsiniz. Bu tür
class’lar, bazı kişilerin object kavramını düşünmeden kullanmak için onları kötüye kullanmasından dolayı kötü bir üne
sahip olmuştur; ancak geçerli kullanım alanları da vardır. Bunlar, `java.lang.Math` veya `java.util.Arrays`’de olduğu
gibi, primitive değerler veya array’ler üzerinde çalışan ilgili method’ları gruplamak için kullanılabilir.

Ayrıca, `java.util.Collections` örneğinde olduğu gibi, bazı interface’leri implement eden object’ler için factory’ler
dahil static method’ları gruplamak amacıyla da kullanılabilirler. (Java 8 itibarıyla, eğer üzerinde değişiklik
yapma hakkınız varsa, bu tür method’ları doğrudan interface içine de koyabilirsiniz.) Son olarak, bu tür class’lar,
final bir class üzerindeki method’ları gruplamak için kullanılabilir; çünkü bu method’lar bir subclass’a konulamaz.

Bu tür utility class’lar instantiate edilmek üzere tasarlanmamıştır; bir instance anlamsız olurdu. Ancak explicit
tanımlanmış bir constructor olmadığında, compiler default olarak public ve parameterless bir constructor sağlar. Bir
kullanıcı için bu constructor, diğerlerinden ayırt edilemez. Published API’lerde yanlışlıkla instantiate edilebilir
class’lara rastlamak yaygındır.

Bir class’ı abstract yaparak noninstantiability sağlamaya çalışmak işe yaramaz. Class, subclass’lanabilir ve bu subclass
instantiate edilebilir. Ayrıca, bu yaklaşım kullanıcıyı class’ın inheritance için tasarlandığı yönünde yanıltır. Ancak,
noninstantiability’yi garanti altına almak için basit bir idiom vardır. Default constructor yalnızca bir class explicit
tanımlanmış bir constructor içermiyorsa oluşturulur, bu nedenle bir class, private bir constructor eklenerek
`noninstantiable` hâle getirilebilir:

```
// Noninstantiable utility class
class UtilityClass{

    // Suppress default constructor for noninstantiability
    private UtilityClass(){
        throw new AssertionError();
    }
    // Remainder omitted
}
```

Explicit constructor private olduğu için class dışından erişilemez. `AssertionError` zorunlu değildir, ancak
constructor’ın class içinden yanlışlıkla çağrılması durumuna karşı ek bir güvence sağlar. Bu, class’ın hiçbir koşulda
instantiate edilmeyeceğini garanti eder. Bu idiom biraz sezgilere aykırıdır çünkü constructor, özellikle çağrılamasın
diye tanımlanır. Bu nedenle, daha önce gösterildiği gibi açıklayıcı bir comment satırı eklemek akıllıca olur.

Side-effect olarak, bu idiom aynı zamanda class’ın subclass’lanmasını da engeller. Tüm constructor’lar, explicityly veya
implicitly olarak bir superclass constructor’ını çağırmak zorundadır; ancak subclass’ın erişebileceği bir superclass
constructor’ı olmayacağı için subclass oluşturulamaz.