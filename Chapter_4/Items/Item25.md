# Limit source files to a single top-level class

Java compiler, single source file'da multiple `top-level` sınıf tanımlamanıza izin verir; ancak bunun hiçbir avantajı
yoktur ve önemli riskler taşır. Riskler, bir source file'da multiple `top-level` sınıf tanımlamanın, bir sınıf için
multiple definition sağlama olanağı vermesinden kaynaklanır. Hangi definition'ın kullanılacağı, source file'larının
compiler'a pass edilmesi sırasından etkilenir.

Somutlaştırmak için, sadece `Main` sınıfını içeren ve bu sınıfın iki başka `top-level` sınıfın `(Utensil ve Dessert)`
member'larına referans verdiği bu source file'i ele alalım:

```
public class Main {
    public static void main(String[] args) {
        System.out.println(Utensil.NAME + Dessert.NAME);
    }
}
```

Şimdi, hem `Utensil` hem de `Dessert` sınıflarını `Utensil.java` adlı tek bir source file'da tanımladığınızı varsayalım:

```
// Tek file'da tanımlanmış iki sınıf. Sakın bunu yapmayın!
class Utensil {
    static final String NAME = "pan";
}

class Dessert {
    static final String NAME = "cake";
}
```

Tabii ki, main program pancake yazdırır.

Şimdi, yanlışlıkla aynı iki sınıfı tanımlayan `Dessert.java` adlı başka bir source file yaptığınızı varsayalım:

```
// Tek file'da tanımlanmış iki sınıf. Sakın bunu yapmayın!
class Utensil {
    static final String NAME = "pot";
}

class Dessert {
    static final String NAME = "pie";
}
```

Şanslıysanız, programı `javac Main.java Dessert.java` komutuyla compile ettiğiniz de, compile başarısız olur ve
compiler size `Utensil` ve `Dessert` sınıflarını multiply define ettiğinizi bildirir. Bunun sebebi, compiler'in önce
`Main.java`’yı compile etmesi ve `Utensil`’e (Dessert’ten önce gelen referans) rastladığında `Utensil.java` dosyasında
hem Utensil hem de Dessert sınıflarını bulmasıdır. Compiler command line'da `Dessert.java` dosyasına rastladığında, onu
da dahil eder ve böylece `Utensil` ve `Dessert`’in her iki definition'ı ile karşılaşır.

Programı `javac Main.java` veya `javac Main.java Utensil.java` command'leri ile compile ederseniz, `Dessert.java`
dosyasını yazmadan önceki gibi davranır ve `pancake` yazdırır. Ama programı `javac Dessert.java Main.java` command'i ile
compile ederseniz, `potpie` yazdırır. Programın davranışı, source file'ların compiler'a iletilme sırasına bağlı olarak
değişir ki bu kesinlikle kabul edilemez bir durumdur.

Sorunu çözmek, `top-level` sınıfları (örneğimizde Utensil ve Dessert) ayrı source file'larına ayırmak kadar basittir.
Eğer birden fazla top-level sınıfı tek bir source file'a koymayı düşünüyorsanız, sınıfları ayrı dosyalara ayırmak
yerine `static member class`’ları alternatif olarak kullanmayı düşünün. Eğer sınıflar başka bir sınıfa bağlıysa, onları
static member class yapmak genellikle daha iyi bir alternatiftir; çünkü okunabilirliği artırır ve sınıfların
erişilebilirliğini private yaparak sınırlandırmayı mümkün kılar. İşte örneğimizin static member class’larla nasıl
göründüğü:

```
// Birden fazla top-level sınıf yerine static member class’lar
public class Test {
    public static void main(String[] args) {
        System.out.println(Utensil.NAME + Dessert.NAME);
    }

    private static class Utensil {
        static final String NAME = "pan";
    }

    private static class Dessert {
        static final String NAME = "cake";
    }
}
```

Ders açıktır: Asla multiple `top-level` sınıf veya `interface`’i tek bir source file'da bulundurmayın. Bu kurala uymak,
compile time'da tek bir sınıf için birden fazla definition olmasını engeller. Bu da compiler ile oluşturulan class
dosyalarının ve ortaya çıkan programın davranışının, source file'ların compiler'a iletilme sırasından bağımsız olmasını
garanti eder.