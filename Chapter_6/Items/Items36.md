# Use EnumSet instead of bit fields

Eğer bir enumerated type'ın element'leri öncelikle set'ler içinde kullanılıyorsa, her constant'a farklı bir 2'nin
kuvveti atayarak int enum pattern'ını (Item 34) kullanmak gelenekseldir:

```java
// Bit field enumeration constant'ları – ESKİMİŞ!
class Text {
    public static final int STYLE_BOLD = 1 << 0; // 1
    public static final int STYLE_ITALIC = 1 << 1; // 2
    public static final int STYLE_UNDERLINE = 1 << 2; // 4
    public static final int STYLE_STRIKETHROUGH = 1 << 3; // 8

    // Parameter, sıfır veya daha fazla STYLE_ constant'ının bitwise OR işlemidir.
    public void applyStyles(int styles) {
    }
}
```

Bu representation, birkaç constant'ı `bitwise OR` operation ile combine ederek `bit field` olarak bilinen bir set
oluşturmanı sağlar:

```
text.applyStyles(STYLE_BOLD | STYLE_ITALIC);
```

Bit field representation'ı ayrıca bitwise aritmetik kullanarak union ve intersection gibi set operation'larını da
verimli bir şekilde gerçekleştirmeni sağlar. Ancak bit field'lar, int enum constant'ların tüm dezavantajlarına ve daha
fazlasına sahiptir. Bir bit field, sayı olarak yazdırıldığında, basit bir int enum constant'tan bile daha zor anlaşılır.
Bir bit field tarafından represent edilen tüm element'ler üzerinde iterate etmenin kolay bir yolu yoktur. Son olarak,
API'yi yazarken ihtiyaç duyacağın maksimum bit sayısını önceden tahmin etmen ve buna göre bit field için bir type
(genellikle int veya long) seçmen gerekir. Bir type seçtikten sonra, API'yi değiştirmeden bu type'ın genişliğini (32
veya 64 bit) aşamazsın.

Bazı programmer'lar, constant set'lerini dolaştırmaları gerektiğinde, enum'ları int constant'lara tercih etseler bile
bit field kullanmaya devam eder. Bunu yapmaya gerek yoktur, çünkü daha iyi bir alternatif vardır. `java.util` package'i,
tek bir enum type'tan alınan value set'lerini verimli şekilde represent etmek için EnumSet sınıfını sağlar. Bu class,
Set interface'ini implement eder ve diğer tüm Set implementasyonlarında elde ettiğin zenginliği, type safety'yi ve
birlikte çalışabilirliği sağlar. Ancak internally olarak, her `EnumSet` bir bit vector'u olarak represent edilir. Eğer
temel `(underlying)` enum type'ı altmış dört veya daha az elemente sahipse — ki çoğu öyledir — tüm EnumSet tek bir long
ile represent edilir, bu yüzden performansı bit field ile karşılaştırılabilir düzeydedir. `removeAll` ve `retainAll`
gibi toplu operation'lar, bit field'lar için manuel olarak yaptığın gibi bitwise aritmetik kullanılarak implement
edilmiştir. Ama manuel bit manipülasyonunun çirkinliğinden ve hata yapma olasılığından korunursun: EnumSet zor işi sizin
için yapar. İşte önceki örneğin, bit field'leri yerine enum ve enum set kullanacak şekilde değiştirilmiş hali. Daha
kısa, daha net ve daha safe:

```java
// EnumSet - bit field için modern bir alternatif
class Text {
    public enum Style {BOLD, ITALIC, UNDERLINE, STRIKETHROUGH}

    // Herhangi bir Set geçilebilir, ancak EnumSet açıkça en iyisidir.
    public void applyStyles(Set<Style> styles) {
    }
}
```

İşte `applyStyles` metoduna bir EnumSet instance'ını geçen client kodu. EnumSet sınıfı, kolay set oluşturma için zengin
bir static factories koleksiyonu sunar ve bu kodda bunlardan biri gösterilmiştir:

```
text.applyStyles(EnumSet.of(Style.BOLD, Style.ITALIC));
```

`applyStyles` metodunun bir `EnumSet<Style>` yerine `Set<Style>` aldığına dikkat edin. Metoda tüm client'ların bir
EnumSet geçireceği muhtemel görünse de, genel olarak `implementation type` yerine `interface type`'ı kabul etmek iyi bir
practice olarak kabul görür. Bu, alışılmadık bir client'ın başka bir `Set` implementation geçirme olasılığına olanak
tanır.

Özetle, bir enum type'ın set'lerde kullanılacak olması, onu bit fields ile represent etmek için bir neden oluşturmaz.
EnumSet sınıfı, bit fields'in kısalığını ve performansını, Item 34'te açıklanan enum types'ın tüm avantajlarıyla
combine eder. EnumSet'in tek gerçek dezavantajı, Java 9 itibarıyla immutable bir EnumSet oluşturmanın mümkün
olmamasıdır, ancak bu muhtemelen yakın bir sürümde düzeltilecektir. Bu arada, bir EnumSet'i
`Collections.unmodifiableSet` ile wrap edebilirsiniz, ancak kısalık ve performans bundan olumsuz etkilenecektir.