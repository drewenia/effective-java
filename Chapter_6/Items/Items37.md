# Use EnumMap instead of ordinal indexing

Bazen, bir array veya list içinde indekslemek için ordinal methodunu kullanan kodlarla karşılaşabilirsiniz. Örneğin, bir
bitkiyi represent etmek için tasarlanmış bu basit sınıfı ele alalım:

```java
class Plant {
    enum LifeCycle {ANNUAL, PERENNIAL, BIENNIAL}

    final String name;
    final LifeCycle lifeCycle;

    public Plant(String name, LifeCycle lifeCycle) {
        this.name = name;
        this.lifeCycle = lifeCycle;
    }

    @Override
    public String toString() {
        return name;
    }
}
```

Şimdi, bir bahçeyi represent eden bir plants array'iniz olduğunu ve bu plant'leri life cycle'a (annual, perennial veya
biennial) göre düzenlenmiş şekilde listelemek istediğinizi varsayalım. Bunu yapmak için, her bir life cycle için birer
tane olmak üzere üç set oluşturur ve bahçeyi dolaşarak her bitkiyi uygun sete yerleştirirsiniz. Bazı programcılar,
set'leri life cycle'ın `ordinal` değeriyle indekslenmiş bir array içine yerleştirerek bunu yapar:

```java
public static void main(String[] args) {
    Plant[] garden = {
            new Plant("Basil", Plant.LifeCycle.ANNUAL),
            new Plant("Carroway", Plant.LifeCycle.BIENNIAL),
            new Plant("Dill", Plant.LifeCycle.ANNUAL),
            new Plant("Lavendar", Plant.LifeCycle.PERENNIAL),
            new Plant("Parsley", Plant.LifeCycle.BIENNIAL),
            new Plant("Rosemary", Plant.LifeCycle.PERENNIAL)
    };

    // Bir array içinde indekslemek için ordinal() kullanmak - BUNU YAPMAYIN!
    Set<Plant>[] plantsByLifeCycle = (Set<Plant>[]) new Set[Plant.LifeCycle.values().length];
    for (int i = 0; i < plantsByLifeCycle.length; i++) {
        plantsByLifeCycle[i] = new HashSet<>();
    }

    for (Plant p : garden) {
        plantsByLifeCycle[p.lifeCycle.ordinal()].add(p);
    }

    // Print the results
    for (int i = 0; i < plantsByLifeCycle.length; i++) {
        System.out.printf("%s: %s%n",
                Plant.LifeCycle.values()[i], plantsByLifeCycle[i]);
    }
}
```

Bu teknik çalışır, ancak sorunlarla doludur. Array'ler generics ile uyumlu olmadığından (Item 28), program unchecked
cast gerektirir ve temiz bir şekilde compile olmaz. Array, indekslerinin neyi represent ettiğini bilmediği için çıktıyı
manuel olarak etiketlemek zorunda kalırsınız. Ancak bu teknikle ilgili en ciddi sorun, bir enum'un ordinal değeriyle
indekslenmiş bir array'e erişirken doğru int değerini kullanmanın sizin sorumluluğunuzda olmasıdır; int'ler, enum'ların
typesafe özelliğini sağlamaz. Yanlış bir değer kullanırsanız, program sessizce yanlış bir şey yapacak veya - eğer
şanslıysanız - bir `ArrayIndexOutOfBoundsException` fırlatacaktır.

Aynı etkiyi elde etmek için çok daha iyi bir yol vardır. Array, enum'dan bir değere eşleme yapan bir map gibi davrandığı
için bir Map kullanabilirsiniz. Daha spesifik olarak, enum key'lerle kullanılmak üzere tasarlanmış, `java.util.EnumMap`
olarak bilinen çok hızlı bir Map implementation vardır. İşte programın EnumMap kullanacak şekilde yeniden yazıldığında
nasıl görüneceği:

```java
public static void main(String[] args) {
    Plant[] garden = {
            new Plant("Basil", Plant.LifeCycle.ANNUAL),
            new Plant("Carroway", Plant.LifeCycle.BIENNIAL),
            new Plant("Dill", Plant.LifeCycle.ANNUAL),
            new Plant("Lavendar", Plant.LifeCycle.PERENNIAL),
            new Plant("Parsley", Plant.LifeCycle.BIENNIAL),
            new Plant("Rosemary", Plant.LifeCycle.PERENNIAL)
    };

    //Bir enum ile data ilişkilendirmek için EnumMap kullanma
    Map<Plant.LifeCycle, Set<Plant>> plantsByLifeCycle =
            new EnumMap<>(Plant.LifeCycle.class);

    for (Plant.LifeCycle lc : Plant.LifeCycle.values())
        plantsByLifeCycle.put(lc, new HashSet<>());

    for (Plant p : garden)
        plantsByLifeCycle.get(p.lifeCycle).add(p);

    // Naive stream based yaklaşım - muhtemelen bir EnumMap üretmez!
    System.out.println(Arrays.stream(garden)
            .collect(groupingBy(p -> p.lifeCycle)));

    // Bir enum ile data ilişkilendirmek için stream ve EnumMap kullanma
    System.out.println(Arrays.stream(garden)
            .collect(groupingBy(p -> p.lifeCycle,
                    () -> new EnumMap<>(Plant.LifeCycle.class), toSet())));
}
```

Bu program, orijinal versiyona kıyasla daha kısa, daha net, daha safe ve hız açısından benzerdir. Unsafe cast yoktur;
map key'leri kendilerini yazdırılabilir string'lere çevirebilen enum'lar olduğu için çıktıyı manuel olarak etiketlemeye
gerek yoktur; ve array indekslerini hesaplarken hata yapma olasılığı yoktur. EnumMap'in hızının `ordinal-indexed array`
ile comparable olmasının nedeni, EnumMap'in internally olarak böyle bir array kullanmasıdır, ancak bu implementation
detail'i programcıdan gizler, bir Map'in zenginliğini ve type safety'ini bir array'in hızıyla combine eder. EnumMap
constructor'ının key type'ın `Class object`'ını aldığına dikkat edin: bu, runtime generic type bilgisi sağlayan bounded
type token'dır.

Önceki program, map'i yönetmek için bir stream kullanılarak daha da kısaltılabilir. İşte önceki örneğin davranışını
büyük ölçüde tekrarlayan en basit stream tabanlı kod:

```
// Naive stream based yaklaşım - muhtemelen bir EnumMap üretmez!
System.out.println(Arrays.stream(garden)
        .collect(groupingBy(p -> p.lifeCycle)));
```

Bu kodun sorunu, kendi map implementation'ını seçmesidir ve pratikte bu bir EnumMap olmayacaktır, bu nedenle explicit
bir EnumMap kullanan versiyonun space ve time performansına uymayacaktır. Bu sorunu düzeltmek için, caller'ın mapFactory
parametresini kullanarak map implementation'ını belirtmesine izin veren üç parametreli `Collectors.groupingBy` formunu
kullanın:

```
// Bir enum ile data ilişkilendirmek için stream ve EnumMap kullanma
System.out.println(Arrays.stream(garden)
    .collect(groupingBy(p -> p.lifeCycle,
    () -> new EnumMap<>(Plant.LifeCycle.class), toSet())));
```

Bu optimizasyon, bunun gibi küçük bir programda yapmaya değmez, ancak map'i yoğun şekilde kullanan bir program için
kritik olabilir.

Stream based versiyonların davranışı, EnumMap versiyonundan biraz farklıdır. EnumMap versiyonu her bir plant lifecycle
için her zaman nested bir map oluşturur, stream based versiyonlar ise yalnızca garden o lifecycle'a sahip bir veya daha
fazla bitki içeriyorsa nested bir map oluşturur. Örneğin, garden annuals ve perennials içeriyor ancak biennials
içermiyorsa, plantsByLifeCycle'ın boyutu EnumMap versiyonunda üç, stream based versiyonların her ikisinde de iki
olacaktır.

İki enum değerinden bir mapping represent etmek için ordinal'lerle indekslenmiş (iki kez!) bir array of arrays
görebilirsiniz. Örneğin, bu program iki phase'i bir phase geçişine map etmek için böyle bir array kullanır (liquid'den
solid'e freezing, liquid'den gas'e boiling vb.):

```java
// Array of arrays'ı indekslemek için ordinal() kullanmak - BUNU YAPMAYIN!
enum Phase {
    SOLID, LIQUID, GAS;

    enum Transition {
        MELT, FREEZE, BOIL, CONDENSE, SUBLIME, DEPOSIT;

        // row'lar from-ordinal, column'lar to-ordinal ile indekslenir
        private static final Transition[][] TRANSITIONS = {
                {null, MELT, SUBLIME},
                {FREEZE, null, BOIL},
                {DEPOSIT, CONDENSE, null}
        };

        // Bir phase'den diğerine phase geçişini döndürür
        public static Transition from(Phase from, Phase to) {
            return TRANSITIONS[from.ordinal()][to.ordinal()];
        }
    }
}
```

Bu program çalışır ve hatta zarif görünebilir, ancak görünüşler aldatıcı olabilir. Daha önce gösterilen daha basit
garden örneğinde olduğu gibi, compiler'ın `ordinal`'ler ve `array indices` arasındaki ilişkiyi bilmesinin bir yolu
yoktur. Transition tablosunda bir hata yaparsan ya da `Phase` veya `Phase.Transition` enum type'ını değiştirdiğinde
tabloyu güncellemeyi unutursan, programın runtime'da fail eder. Hata bir `ArrayIndexOutOfBoundsException`, bir
`NullPointerException` veya (daha kötüsü) sessiz hatalı davranış olabilir. Ve tablonun boyutu, null olmayan entry sayısı
daha az olsa bile, phase sayısının karesiyle orantılıdır.

Yine, EnumMap ile çok daha iyi bir çözüm elde edebilirsin. Her phase transition bir phase enum pair'i ile
indekslendiğinden, bu ilişkiyi bir enum'dan `(“from” phase)` diğer enum'a `(“to” phase)` ve sonuca `(phase transition)`
giden bir map olarak represent etmek en iyisidir. Bir phase transition ile ilişkili iki phase, en iyi şekilde bu
phase'leri phase transition enum'u ile ilişkilendirerek yakalanır; bu enum da ardından nested EnumMap'i initialize etmek
için kullanılabilir.

```java
// Enum pair'ları ile data ilişkilendirmek için nested EnumMap kullanımı
enum Phase {
    SOLID, LIQUID, GAS;

    enum Transition {
        MELT(SOLID, LIQUID), FREEZE(LIQUID, SOLID),
        BOIL(LIQUID, GAS), CONDENSE(GAS, LIQUID),
        SUBLIME(SOLID, GAS), DEPOSIT(GAS, SOLID);

        private final Phase from;
        private final Phase to;

        Transition(Phase from, Phase to) {
            this.from = from;
            this.to = to;
        }

        // Phase transition map'ini initialize et
        private static final Map<Phase, Map<Phase, Transition>>
                m = Stream.of(values()).collect(groupingBy(t -> t.from,
                () -> new EnumMap<>(Phase.class),
                toMap(t -> t.to, t -> t,
                        (x, y) -> y, () -> new EnumMap<>(Phase.class))));

        public static Transition from(Phase from, Phase to) {
            return m.get(from).get(to);
        }
    }
}
```

Derived.java;

```java
public static void main(String[] args) {
    for (Phase src : Phase.values()) {
        for (Phase dst : Phase.values()) {
            Phase.Transition transition = Phase.Transition.from(src, dst);
            if (transition != null)
                System.out.printf("%s to %s : %s %n", src, dst, transition);
        }
    }
}
```

Output;

```
SOLID to LIQUID : MELT 
SOLID to GAS : SUBLIME 
LIQUID to SOLID : FREEZE 
LIQUID to GAS : BOIL 
GAS to SOLID : DEPOSIT 
GAS to LIQUID : CONDENSE
```

Phase transition map'ini initialize eden kod biraz karmaşıktır. Map'in type'ı `Map<Phase, Map<Phase, Transition>>`
şeklindedir, bu da `(source) phase'den (destination) phase'e olan transition'a giden map` anlamına gelir. Bu
`map-of-maps`, sequence iki collector kullanılarak initialize edilir. İlk collector transition'ları source phase'e göre
gruplar, ikinci collector ise destination phase'den transition'a olan mapping'leri içeren bir EnumMap oluşturur. İkinci
collector'daki merge function `((x, y) -> y)` kullanılmaz; EnumMap elde etmek için bir map factory belirtmemiz
gerektiğinden ve Collectors'ın bu tipte factory'ler sağladığından dolayı gereklidir.

Şimdi sisteme yeni bir phase eklemek istediğini varsayalım: plasma veya ionized gas. Bu phase ile ilişkili yalnızca iki
transition vardır: gazı plazmaya dönüştüren ionization ve plazmayı gaza dönüştüren deionization. Array based programı
güncellemek için, Phase'a bir yeni constant ve `Phase.Transition`'a iki yeni constant eklemeli, ayrıca orijinal dokuz
elemanlı array of array'i yeni on altı elemanlı versiyonla değiştirmelisin. Array'e çok fazla veya çok az eleman ekler
veya bir elemanı yanlış sıraya koyarsan, şansın kalmaz: program compile olur ama runtime'da fail eder. EnumMap based
versiyonu güncellemek için yapman gereken tek şey, phases listesine PLASMA'yı ve phase transition listesine IONIZE(GAS,
PLASMA) ile DEIONIZE(PLASMA, GAS)'ı eklemektir:

```java
// Nested EnumMap implementasyonu kullanarak yeni bir phase eklemek
public enum Phase {
    SOLID, LIQUID, GAS, PLASMA;

    public enum Transition {
        MELT(SOLID, LIQUID), FREEZE(LIQUID, SOLID),
        BOIL(LIQUID, GAS), CONDENSE(GAS, LIQUID),
        SUBLIME(SOLID, GAS), DEPOSIT(GAS, SOLID),
        IONIZE(GAS, PLASMA), DEIONIZE(PLASMA, GAS);
        // Remainder unchanged
    }
}
```

Program geri kalan her şeyi halleder ve sana neredeyse hiç hata yapma fırsatı bırakmaz. Internally olarak, map of maps
bir array of array olarak implement edilmiştir; bu nedenle, eklenen açıklık, güvenlik ve bakım kolaylığı için çok az
alan veya zaman maliyeti ödersin. Kısalık adına, yukarıdaki örneklerde state değişikliğinin olmaması (to ve from'un aynı
olduğu durum) null ile gösterilmiştir. Bu iyi bir practice değildir ve büyük olasılıkla runtime'da NullPointerException
ile sonuçlanır. Bu probleme temiz, zarif bir çözüm tasarlamak şaşırtıcı derecede zordur ve ortaya çıkan programlar
yeterince uzun olduğundan bu item'daki ana konudan sapmaya neden olur.

Özetle, ordinal'ları array'lerde indeks olarak kullanmak nadiren uygundur: onun yerine EnumMap kullan. Represent ettiğin
ilişki çok boyutluysa, `EnumMap<..., EnumMap<...>>` kullan. Bu, uygulama programmer'larının neredeyse hiç `Enum.ordinal`
kullanmaması gerektiği genel ilkesinin özel bir durumudur.