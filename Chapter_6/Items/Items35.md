# Use instance fields instead of ordinals

Pek çok enum doğal `(naturally)` olarak tek bir int value ile ilişkilidir. Tüm enum'ların, her enum constant'unun kendi
type'ındaki sayısal konumunu döndüren `ordinal` method'u vardır. İlişkili bir int değeri ordinal'dan derive etmeye
teşvik edilebilirsin:

```java
// İlişkili bir değer türetmek (derive) için ordinal'ın kötüye kullanımı - BUNU YAPMA
enum Ensemble {
    SOLO, DUET, TRIO, QUARTET, QUINTET,
    SEXTET, SEPTET, OCTET, NONET, DECTET;

    public int numberOfMusicians() {
        return ordinal() + 1;
    }
}
```

Bu enum çalışsa da, bakım açısından bir kabus olur. Constant'lar yeniden sıralanırsa `(reordered)`, `numberOfMusicians`
method'u bozulur. Daha önce kullandığın bir int değeriyle ilişkili ikinci bir enum constant eklemek istersen, şansın
kalmaz. Örneğin, double quartet için, tıpkı octet gibi sekiz müzisyenden oluşan bir constant eklemek güzel olabilir,
ancak bunu yapmanın bir yolu yoktur. Örneğin, on iki müzisyenden oluşan triple quartet'i temsil eden bir constant
eklemek istediğini varsayalım. On bir müzisyenden oluşan bir topluluk için standart bir terim olmadığından,
kullanılmayan int değeri `(11)` için sahte bir constant eklemek zorunda kalırsın. En iyi ihtimalle bu çirkindir. Eğer
birçok int değeri kullanılmıyorsa, bu pratik değildir.

Neyse ki, bu problemlere basit bir çözüm vardır. Bir enum ile ilişkili bir değeri ordinal'dan türetme `(derive)`; bunun
yerine instance field içinde store et:

```java
// Instance field içinde saklanan integer data'ya sahip enum
enum Ensemble {
    SOLO(1), DUET(2), TRIO(3), QUARTET(4), QUINTET(5),
    SEXTET(6), SEPTET(7), OCTET(8), DOUBLE_QUARTET(8),
    NONET(9), DECTET(10), TRIPLE_QUARTET(12);

    private final int numberOfMusicians;

    Ensemble(int size) {
        this.numberOfMusicians = size;
    }

    public int getNumberOfMusicians() {
        return numberOfMusicians;
    }
}
```

Enum spesifikasyonu ordinal hakkında şöyle der: “Çoğu programmer bu method'u kullanmaz. Genel amaçlı enum based data
structure'lar, örneğin `EnumSet` ve `EnumMap` tarafından kullanılmak üzere tasarlanmıştır.”. Bu karakterde kod
yazmıyorsan, ordinal method'tan tamamen kaçınman en iyisidir.