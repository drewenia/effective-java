# Prefer dependency injection to hardwiring resources

# Resource'ları doğrudan bağlamak yerine dependency injection tercih edin.

Pek çok class, bir veya daha fazla temel `(underlying)` resource'a bağımlıdır `(depend)`. Örneğin, bir spell checker bir
dictionary’e bağımlıdır `(depend)`. Bu tür class’ların static utility class olarak implement edildiği durumlara sıkça
rastlanır.

```
// Inappropriate use of static utility - inflexible & untestable!
class SpellChecker{
    private static final Lexicon dictionary = ...;

    private SpellChecker(){} // Noninstantiable

    public static boolean isValid(String word){
        ...
    }

    public static List<String> suggestions(String type){
        ...
    }
}
```

Benzer şekilde, bunların singleton olarak implement edildiği durumlar da yaygındır.

```
// Inappropriate use of singleton - inflexible & untestable!
public class SpellChecker {
    private final Lexicon dictionary = ...;
    private SpellChecker(...) {}

    public static INSTANCE = new SpellChecker(...);
    
    public boolean isValid(String word) {
        ... 
    }
    
    public List<String> suggestions(String typo) {
        ... 
    }
}
```

Bu yaklaşımların hiçbiri tatmin edici değildir, çünkü yalnızca tek bir dictionary’nin kullanılmaya değer olduğunu
varsayarlar. Pratikte, her dilin kendi dictionary’si vardır ve special vocabulary’ler için special dictionary’ler
kullanılır. Ayrıca, test için special bir dictionary kullanmak da istenebilir. Tek bir dictionary’nin her zaman
yeteceğini varsaymak iyimser bir düşüncedir.

SpellChecker’ın birden fazla dictionary desteklemesini sağlamak için dictionary field’ını `nonfinal` yapıp mevcut bir
spell checker’ın dictionary’sini değiştiren bir method eklemeyi deneyebilirsiniz, ancak bu yaklaşım kullanışsız, hata
yapmaya açık ve `concurrent` bir ortamda işe yaramazdır. Behavior'u alttaki `(underlying)` bir resource tarafından
parameterize edilen class’lar için static utility class’lar ve singleton’lar uygun değildir.

Gereken, her biri client’ın istediği resource’u (örneğimizde dictionary) kullanan sınıfın (örneğimizde SpellChecker)
birden fazla instance’ını destekleyebilme yeteneğidir. Bu gereksinimi karşılayan basit bir pattern, yeni bir instance
oluşturulurken resource’u constructor’a parametre olarak geçmektir. Bu, dependency injection’ın bir formudur:
dictionary, spell checker’ın dependency'sidir ve spell checker create edilirken içine inject edilir.

```
// Dependency injection provides flexibility and testability
class SpellChecker{
    private final Lexicon dictionary;

    public SpellChecker(Lexicon dictionary){
        this.dictionary = Objects.requireNonNull(dictionary);
    }

    public boolean isValid(String word){
        ...
    }

    public List<String> suggestions (String typo){
        ...
    }
}
```

Dependency injection pattern o kadar basittir ki, birçok programcı adı olduğunu bilmeden yıllarca kullanır. Spell
checker örneğimizde yalnızca tek bir resource `(dictionary)` vardı, ancak dependency injection, herhangi sayıda resource
ve karmaşık dependency graph'ları ile çalışır. Immutability’yi korur, böylece multiple client dependent object’leri
paylaşabilir (client’lar aynı underlying resource’ları istiyorsa). Dependency injection, constructor’lara, static
factory’lere ve builder’lara eşit derecede uygulanabilir.

Pattern’in faydalı bir varyantı, constructor’a bir resource factory geçirmek. Factory, belirli bir type'da instance’lar
yaratmak için repeatedly call edilebilen bir object’tir. Bu tür factory’ler, Factory Method pattern’ini somutlaştırır
`(embody)`. Java 8 ile tanıtılan `Supplier<T>` interface’i, factory’leri represent etmek için idealdir. `Supplier<T>`
alan method’lar genellikle factory’nin type parametresini, client’in specified bir type'ın herhangi bir subtype'ını
yaratan factory’i geçmesine izin vermek için `bounded wildcard` type'ı ile kısıtlar.Örneğin, işte her karo’yu oluşturmak
için client tarafından sağlanan bir factory kullanan bir mozaik yapan method:

```
Mosaic create(Supplier<? extends Tile> tileFactory){
    
}
```

Dependency injection esnekliği ve test edilebilirliği büyük ölçüde artırsa da, genellikle binlerce dependency içeren
büyük projelerde karmaşıklığa yol açabilir. Bu karmaşıklık, Dagger, Guice veya Spring gibi dependency injection
framework’leri kullanılarak neredeyse tamamen ortadan kaldırılabilir.

Özetle, behavior'u etkileyen bir veya daha fazla underlying resource’a depends olan bir class’ı implement etmek için
singleton veya static utility class kullanmayın ve class’ın bu resource’ları directly oluşturmasına izin vermeyin. Bunun
yerine, resource’ları veya onları yaratacak factory’leri constructor’a (ya da static factory veya builder’a) parametre
olarak geçin. Dependency injection olarak bilinen bu practice, bir class’ın esnekliğini, yeniden kullanılabilirliğini ve
test edilebilirliğini büyük ölçüde artıracaktır.