# Consider a builder when faced with many constructor parameters

# Çok sayıda constructor parametresiyle karşılaşıldığında builder kullanmayı düşünün.

Static factory’ler ve constructor’lar ortak bir sınırlamaya sahiptir: çok sayıda optional parametre olduğunda iyi
ölçeklenmezler. Paketlenmiş gıdalarda yer alan `Nutrition Facts` etiketi temsil eden bir class durumunu düşünün. Bu
etiketlerde birkaç zorunlu alan vardır — porsiyon boyutu, kap başına porsiyon sayısı ve porsiyon başına kalori — ve
yirmiden fazla optional field bulunur — toplam yağ, doymuş yağ, trans yağ, kolesterol, sodyum ve benzeri. Çoğu ürünün bu
optional field'lerin sadece birkaçında sıfırdan farklı değerleri vardır.

Böyle bir class için nasıl constructor veya static factory method’lar yazmalısınız? Traditional olarak, programcılar
`telescoping constructor` pattern’ını kullanırlar; burada yalnızca zorunlu parametreleri alan bir constructor, tek bir
optional parametre alan başka bir constructor, iki optional parametre alan üçüncü bir constructor ve bu şekilde tüm
optional parametreleri alan son bir constructor sağlanır. Pratikte nasıl göründüğü aşağıdadır. Kısalık açısından sadece
dört optional field gösterilmiştir:

```
// Teleskopik constructor pattern — iyi ölçeklenmez!
public class NutritionFacts {
    private final int servingSize; // (mL) required
    private final int servings; // (per container) required
    private final int calories; // (per serving) optional
    private final int fat; // (g/serving) optional
    private final int sodium; // (mg/serving) optional
    private final int carbohydrate; // (g/serving) optional
    
    public NutritionFacts(int servingSize, int servings) {
        this(servingSize, servings, 0);
    }
    
    public NutritionFacts(int servingSize, int servings, int calories) {
        this(servingSize, servings, calories, 0);
    }
    
    public NutritionFacts(int servingSize, int servings, int calories, int fat) {
        this(servingSize, servings, calories, fat, 0);
    }
    
    public NutritionFacts(int servingSize, int servings, int calories, int fat, int sodium) {
        this(servingSize, servings, calories, fat, sodium, 0);
    }
    
    public NutritionFacts(int servingSize, int servings, int calories, int fat, int sodium, int carbohydrate) {
        this.servingSize = servingSize;
        this.servings = servings;
        this.calories = calories;
        this.fat = fat;
        this.sodium = sodium;
        this.carbohydrate = carbohydrate;
    }
}
```

Bir instance oluşturmak istediğinizde, set etmek istediğiniz tüm parametreleri içeren en kısa parametre listesine sahip
constructor’ı kullanırsınız:

```
NutritionFacts cocaCola = new NutritionFacts(240, 8, 100, 0, 35, 27);
```

Genellikle bu constructor çağrısı, ayarlamak istemediğiniz birçok parametre gerektirir; ancak yine de bunlar için bir
değer geçmek zorunda kalırsınız.Bu durumda, `fat` için `0` değeri geçtik. “Yalnızca” altı parametreyle bu o kadar da
kötü görünmeyebilir, ancak parametre sayısı arttıkça durum hızla kontrolden çıkar.

Kısacası, teleskopik constructor pattern işe yarar; ancak çok sayıda parametre olduğunda client kodunu yazmak zordur,
okumak ise daha da zordur. Okuyucu, tüm bu değerlerin ne anlama geldiğini merak eder ve bunu anlamak için parametreleri
dikkatlice saymak zorundadır. Aynı tipteki parametrelerin Long sequence'leri, fark edilmesi zor hatalara yol açabilir.
Eğer client bu tür iki parametreyi yanlışlıkla ters sırayla verirse, compiler şikayet etmez; ancak program runtime
sırasında hatalı davranır.

Constructor’da çok sayıda optional parametreyle karşılaştığınızda ikinci bir alternatif, parameterless bir constructor
ile object'i oluşturup ardından her bir gerekli parametreyi ve ilgilendiğiniz her bir optional parametreyi ayarlamak
için setter method’larını çağırdığınız JavaBeans pattern’ıdır:

```
// JavaBeans Pattern — tutarsızlığa izin verir, mutability’yi zorunlu kılar.
class NutritionFacts {
    // Parametreler default value'lar ile (varsa) initalize edilir
    private int servingSize = -1; // Required; no default value
    private int servings = -1; // Required; no default value
    private int calories = 0;
    private int fat = 0;
    private int sodium = 0;
    private int carbohydrate = 0;

    public NutritionFacts() {
    }

    public void setServingSize(int val) {
        this.servingSize = val;
    }

    public void setServings(int val) {
        this.servings = val;
    }

    public void setCalories(int val) {
        this.calories = val;
    }

    public void setFat(int val) {
        this.fat = val;
    }

    public void setSodium(int val) {
        this.sodium = val;
    }

    public void setCarbohydrate(int val) {
        this.carbohydrate = val;
    }
}
```

Bu pattern, teleskopik constructor pattern’ın hiçbir dezavantajına sahip değildir. Biraz uzun olsa da, instance
oluşturmak kolaydır ve ortaya çıkan kodu okumak da rahattır:

```
NutritionFacts cocaCola = new NutritionFacts();
cocaCola.setServingSize(240);
cocaCola.setServings(8);
cocaCola.setCalories(100);
cocaCola.setSodium(35);
cocaCola.setCarbohydrate(27);
```

Ne yazık ki, JavaBeans pattern’ının kendi içinde ciddi dezavantajları vardır. Construction işlemi birden fazla call'a
bölündüğü için, bir JavaBean oluşturulma sürecinin ortasında tutarsız `(inconsistent)` bir state'de olabilir. Class,
yalnızca constructor parametrelerinin geçerliliğini kontrol ederek tutarlılığı sağlama seçeneğine sahip değildir. Bir
object tutarsız `(inconsistent)` bir state'de iken kullanmaya çalışmak, bug'ın bulunduğu koddan çok uzakta hatalara
neden olabilir ve bu da debug'ı zorlaştırır. İlgili bir dezavantaj da, JavaBeans pattern’ın bir class’ı immutable yapma
olasılığını ortadan kaldırması ve programcıdan thread safety’yi sağlamak için ekstra çaba gerektirmesidir.

Object'in construction'ı tamamlandığında manually dondurarak `(freezing)` bu dezavantajlar azaltılabilir ve dondurulana
kadar object'in kullanılmasına izin verilmez; ancak bu yöntem kullanışsızdır ve pratikte nadiren tercih edilir. Ayrıca,
compiler programcının bir object'i kullanmadan önce freeze method’unu çağırdığını garanti edemediği için runtime’da
hatalara yol açabilir.

Neyse ki, teleskopik constructor pattern’ın güvenliğini JavaBeans pattern’ın okunabilirliğiyle birleştiren üçüncü bir
alternatif vardır. Bu, Builder pattern’ın bir çeşididir. İstenen object'i directly oluşturmak yerine, client tüm
required parametrelerle bir constructor (veya static factory) call eder ve bir builder object'i alır. Ardından client,
builder object'i üzerinde her bir ilgilenilen optional parametreyi set etmek için setter benzeri method’ları çağırır.
Son olarak, client parameterless build method’unu çağırarak object'i generate eder; bu object genellikle immutable olur.
Builder genellikle build ettiği class’ın static member class’ıdır. Pratikte şöyle görünür:

```
// Builder Pattern
class NutritionFacts{
    private final int servingSize;
    private final int servings;
    private final int calories;
    private final int fat;
    private final int sodium;
    private final int carbohydrate;

    public static class Builder{
        // Required parameters
        private final int servingSize;
        private final int servings;

        // Optional parameters - initialized to default values
        private int calories = 0;
        private int fat = 0;
        private int sodium = 0;
        private int carbohydrate = 0;

        public Builder(int servingSize, int servings){
            this.servingSize = servingSize;
            this.servings = servings;
        }

        public Builder calories(int val){
            this.calories = val;
            return this;
        }

        public Builder fat(int val){
            this.fat = val;
            return this;
        }

        public Builder sodium(int val){
            this.sodium = val;
            return this;
        }

        public Builder carbohydrate(int val){
            this.carbohydrate = val;
            return this;
        }

        public NutritionFacts build(){
            return new NutritionFacts(this);
        }
    }

    private NutritionFacts(Builder builder){
        servingSize = builder.servingSize;
        servings = builder.servings;
        calories = builder.calories;
        fat = builder.calories;
        sodium = builder.sodium;
        carbohydrate = builder.carbohydrate;
    }
}
```

NutritionFacts class’ı immutable’dır ve tüm parametre default value'ları tek bir yerde toplanmıştır. Builder’ın setter
method’ları kendisini döndürür, böylece call'lar chain edilebilir ve akıcı `(fluent)` bir API elde edilir. Client kodu
şöyle görünür:

```
NutritionFacts nutritionFacts = new NutritionFacts.Builder(240, 8)
        .calories(100)
        .sodium(35)
        .carbohydrate(27)
        .build();
```

Bunu bir record ile yazmaya kalkarsam aşağıda ki şekilde görünecektir:

```
record NutrifionFacts(int servingSize, int servings, int calories, int fat, int sodium, int carbohydrate) {
    public static class Builder {
        private final int servingSize;
        private final int servings;
        private int calories;
        private int fat;
        private int sodium;
        private int carbohydrate;

        public Builder(int servingSize, int servings) {
            this.servingSize = servingSize;
            this.servings = servings;
        }

        public Builder calories(int val) {
            this.calories = val;
            return this;
        }

        public Builder fat(int val) {
            this.fat = val;
            return this;
        }

        public Builder sodium(int val) {
            this.sodium = val;
            return this;
        }

        public Builder carbohydrate(int val) {
            this.carbohydrate = val;
            return this;
        }

        public NutrifionFacts build() {
            return new NutrifionFacts(
                    this.servingSize,
                    this.servings,
                    this.calories,
                    this.fat,
                    this.sodium,
                    this.carbohydrate
            );
        }
    }
}
```

Bu client kodu yazması kolaydır ve daha da önemlisi okunması kolaydır. Kısalık adına validity check'leri atlanmıştır.
Geçersiz parametreleri mümkün olan en erken aşamada tespit edebilmek için, parametre geçerliliğini builder’ın
constructor’ında ve method’larında kontrol edin. Birden fazla parametreyi içeren invariant’ları, build method’u
tarafından call edilen constructor içinde kontrol edin. Bu invariant’ları saldırılara karşı garanti altına almak için,
parametreleri builder’dan kopyaladıktan sonra object field'leri üzerinde kontrolleri gerçekleştirin. Bir kontrol
fail olursa, hangi parametrelerin geçersiz olduğunu belirten açıklayıcı bir mesaj içeren `IllegalArgumentException`
fırlatın.

Builder pattern, class hiyerarşileri için oldukça uygundur. Her biri ilgili class’ın içinde yer alan parallel bir
builder hiyerarşisi kullanın. Abstract class’lar abstract builder’lara, concrete class’lar ise concrete builder’lara
sahiptir. Örneğin, çeşitli pizza türlerini represent eden bir hiyerarşinin root'unda ki abstract bir class’ı ele alalım:

```
// Builder pattern for class hierarchies
abstract class Pizza {
    enum Topping {
        HAM,
        MUSHROOM,
        ONION,
        PEPPER,
        SAUCAGE
    }

    private final EnumSet<Topping> toppings;

    // Recursive type parameter
    abstract static class Builder<T extends Builder<T>> {
        // Topping içerisinde ki hiçbir enum value'sunu alma
        EnumSet<Topping> toppings = EnumSet.noneOf(Topping.class);

        public T addTopping(Topping topping) {
            toppings.add(Objects.requireNonNull(topping));
            /* Java’nın self type eksikliğine yönelik bu çözüm, simulated self-type idiom olarak bilinir.
            *  Subclass’lar, bu method’u override ederek "this" döndürmelidir.
            * */
            return self();
        }

        abstract Pizza build();
        protected abstract T self();
    }

    Pizza(Builder<?> builder){
        this.toppings = builder.toppings.clone();
    }
}
```

Dikkat edin;

Pizza.Builder `(abstract static class Builder<T extends Builder<T>>)` recursive bir type parametresiyle tanımlanmış
generic bir type’tır. Bu özellik ve abstract `protected abstract T self();` method, subclass'lar da method chaining’in
doğru çalışmasını sağlar; cast yapılmasına gerek kalmaz. Java’nın self type eksikliğine yönelik bu çözüm,
`simulated self-type idiom` olarak bilinir. Eğer `return this;` kullansaydık `Builder.T` type'ında dönüş alacaktık ve
ide error üretecekti. `self()` kullanarak gelen type'ın aynısını döndürmüş oluyoruz. `Pizza(Builder<?> builder)`
constructor'ı yardımı ile Pizza abstract'i içerisinde ki `EnumSet` builder içerisinde ki `EnumSet`'i `clone()`methodu
yardımı ile kopyalar.

İşte Pizza’nın iki concrete alt sınıfı; biri standart New York tarzı pizzayı, diğeri ise Calzone’u temsil eder. İlki
required bir `size` parametresine sahiptir, ikincisi ise `sausage` içte mi dışta mı olacağını belirtmenize olanak tanır:

NYPizza.java;

```
class NYPizza extends Pizza {
    enum Size {
        SMALL,
        MEDIUM,
        LARGE
    }

    private final Size size;

    public static class Builder extends Pizza.Builder<Builder> {
        private final Size size;

        public Builder(Size size) {
            this.size = Objects.requireNonNull(size);
        }

        @Override
        public NYPizza build() {
            return new NYPizza(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }

    private NYPizza(Builder builder) {
        super(builder);
        size = builder.size;
    }
}
```

Calzone.java;

```
class Calzone extends Pizza {
    private final boolean sauceInside;

    public static class Builder extends Pizza.Builder<Builder> {
        private boolean sauceInside = false; // default

        public Builder sauceInside() {
            sauceInside = true;
            return this;
        }

        @Override
        Calzone build() {
            return new Calzone(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }

    private Calzone(Builder builder) {
        super(builder);
        sauceInside = builder.sauceInside;
    }
}
```

Derived;

```
NYPizza nyPizza = new NYPizza.Builder(NYPizza.Size.SMALL)
        .addTopping(Pizza.Topping.SAUCAGE)
        .addTopping(Pizza.Topping.ONION)
        .build();

Calzone calzonePizza = new Calzone.Builder()
        .addTopping(Pizza.Topping.HAM)
        .sauceInside()
        .build();
```

Dikkat edin, her subclass'ın builder’ındaki build method, doğru subclass'ıdönecek şekilde tanımlanmıştır:
`NyPizza.Builder` içindeki `build` method `NyPizza` dönerken, `Calzone.Builder`’daki `build` method `Calzone` döner.
Bu teknik, bir subclass'ın method’unun, parent class'da tanımlanan return type'ının bir subtype'ını return edecek
şekilde tanımlanmasına `covariant return typing` adı verilir. Bu, client’ların bu builder’ları cast işlemi yapmadan
kullanabilmesini sağlar.

Bu “hierarchical builder”lar için client kodu, basit `NutritionFacts` builder’ının koduyla esasen aynıdır. Aşağıda
gösterilen örnek client kodu, kısalık adına enum sabitleri için static import varsaymaktadır:

```
NyPizza pizza = new NyPizza.Builder(SMALL)
        .addTopping(SAUSAGE).addTopping(ONION).build();

Calzone calzone = new Calzone.Builder()
        .addTopping(HAM).sauceInside().build();
```

Builder’ların constructor’lara göre küçük bir avantajı da, her parametrenin kendi method’unda belirtildiği için birden
fazla `varargs` parametresine sahip olabilmeleridir. Alternatif olarak, builder’lar bir metoda yapılan birden fazla
call'da geçirilen parametreleri tek bir field'de toplayabilir; bu, daha önce gösterilen `addTopping` method’unda
olduğu gibi yapılır.

Builder pattern oldukça esnektir. Tek bir builder, birden fazla object oluşturmak için tekrar tekrar kullanılabilir.
Builder’ın parametreleri, build method’u call'ları arasında değiştirilerek oluşturulan object’ler çeşitlendirilebilir.
Bir builder, her object oluşturulduğunda artan bir seri numarası gibi bazı field’ları object creation sırasında otomatik
olarak doldurabilir.

Builder pattern’ın dezavantajları da vardır. Bir object oluşturmak için önce onun builder’ını oluşturmanız gerekir. Bu
builder’ın oluşturulma maliyeti pratikte farkedilmeyecek kadar düşük olsa da, performansın kritik olduğu durumlarda
sorun yaratabilir. Ayrıca, Builder pattern teleskopik constructor pattern’dan daha ayrıntılıdır (verbose), bu yüzden
sadece yeterince çok parametre varsa, örneğin dört veya daha fazla olduğunda kullanılmalıdır. Ama unutmayın, gelecekte
daha fazla parametre eklemek isteyebilirsiniz. Başlangıçta constructor veya static factory kullanıp, parametre sayısı
kontrolden çıktığında builder’a geçerseniz, eskimiş constructor veya static factory’ler dikkat çekici bir sorun haline
gelir. Bu yüzden, çoğu zaman en baştan builder ile başlamak daha iyidir.

Özetle, Builder pattern, constructor veya static factory’lerinin çok sayıda parametreye sahip olduğu, özellikle de bu
parametrelerin birçoğunun optional veya aynı türden olduğu sınıfları tasarlarken iyi bir tercihtir. Client kodu,
teleskopik constructor’lara kıyasla builder’larla çok daha kolay okunur ve yazılır; ayrıca builder’lar JavaBeans’e göre
çok daha güvenlidir.