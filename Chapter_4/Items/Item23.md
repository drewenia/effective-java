# Prefer class hierarchies to tagged classes

# Class hiyerarşilerini tagged class'lara tercih edin.

Bazen, instance'ları iki veya daha fazla çeşitte olan ve instance'ın çeşidini belirten bir tag field içeren bir sınıfa
rastlayabilirsiniz. Örneğin, bir circle'i veya rectangle'i represent edebilen bu sınıfı düşünün:

```
public class Figure {
    enum Shape {
        CIRCLE,
        RECTANGLE
    }

    // Tag field - bu Shape'in türü
    final Shape shape;

    // Bu field’lar sadece shape RECTANGLE ise kullanılır
    double length;
    double width;

    // Bu field sadece shape CIRCLE ise kullanılır
    double radius;

    // Constructor for Rectangle
    Figure(double length, double width) {
        shape = Shape.RECTANGLE;
        this.length = length;
        this.width = width;
    }

    // Constructor for Circle
    Figure(double radius) {
        shape = Shape.CIRCLE;
        this.radius = radius;
    }

    double area() {
        switch (shape) {
            case RECTANGLE:
                return length * width;
            case CIRCLE:
                return Math.PI * (radius * radius);
            default:
                throw new AssertionError(shape);
        }
    }
}
```

Böyle tagged sınıfların birçok eksikliği vardır. Enum declaration'ları, tag field’lar ve switch statement'ları gibi
boilerplate ile doludur. Okunabilirlik, birden fazla implementasyonun tek bir sınıfta karışık halde bulunması nedeniyle
daha da zarar görür. Bellek kullanımı artar çünkü instance'lar, başka type'lara ait gereksiz field’larla yüklenir.
Field’lar, constructor’lar gereksiz field’ları da initialize etmedikçe final yapılamaz, bu da daha fazla boilerplate’e
yol açar. Constructor’lar, compiler’dan hiçbir yardım almadan tag field’ı ayarlamalı ve doğru data field’larını
initialize etmelidir: Eğer yanlış field’ları initialize ederseniz, program runtime'da fail olur. Bir tagged sınıfa yeni
bir tür `(flavor)` ekleyemezsiniz, source code'unu değiştiremediğiniz sürece. Eğer yeni bir tür `(flavor)` eklerseniz,
her switch statement'ına bir case eklemeyi unutmamalısınız, aksi takdirde sınıf runtime'da fail olur. Son olarak, bir
instance'ın data type’ı onun hangi tür'de `(flavor)` olduğuna dair hiçbir ipucu vermez. Kısacası, tagged sınıflar
ayrıntılıdır, hata yapmaya açıktır ve verimsizdir.

Neyse ki, Java gibi object oriented diller, birden fazla tür'de ki `(flavor)` object'leri represent edebilen single bir
data type'ı define etmek için çok daha iyi bir alternatif sunar: `subtyping`. Bir tagged sınıf, bir class hierarchy’nin
sadece soluk `(pallid)` bir taklididir `(imitation)`.

Bir tagged sınıfı class hierarchy’ye dönüştürmek için önce, tag value'ya bağlı olarak davranışı değişen her method için
bir abstract method içeren bir abstract sınıf tanımlayın. Figure sınıfında, bu tür sadece bir method vardır: `area`.
Bu abstract sınıf, class hierarchy’nin root'udur. Eğer tag value'suna bağlı olmayan method’lar varsa, onları bu sınıfa
koyun. Benzer şekilde, tüm türler `(flavor)` tarafından kullanılan data field’lar varsa, onları da bu sınıfa koyun.
Figure sınıfında türden `(flavor)` bağımsız `(independent)` method veya field yoktur.

Sonra, orijinal tagged sınıfın her türü `(flavor)` için root sınıfın concrete bir subclass'ını define edin. Örneğimizde
iki tane var: circle ve rectangle. Her subclass'a, o türe `(flavor)` özgü data field’ları dahil edin. Örneğimizde,
`radius` circle’a özgüdür; `length` ve `width` ise rectangle’a özgüdür. Ayrıca, her subclass'a root sınıftaki abstract
method’ların uygun implementasyonlarını da ekleyin. İşte orijinal Figure sınıfına karşılık gelen class hierarchy:

```
// Class hierarchy replacement for a tagged class
abstract class Figure {
    abstract double area();
}

class Circle extends Figure {
    final double radius;

    Circle(double radius) {
        this.radius = radius;
    }

    @Override
    double area() {
        return Math.PI * (radius * radius);
    }
}

class Rectangle extends Figure {
    final double length;
    final double width;

    Rectangle(double length, double width) {
        this.length = length;
        this.width = width;
    }

    @Override
    double area() {
        return length * width;
    }
}
```

Bu class hierarchy, önceki tagged sınıflarda belirtilen tüm eksiklikleri giderir. Kod basit ve açıktır, orijinaldeki
boilerplate’i hiç içermez. Her türün `(flavor)` implementasyonu kendi sınıfına ayrılmıştır ve bu sınıfların hiçbiri
gereksiz data field’larla yüklenmemiştir. Tüm field'ler final'dir. Compiler, her sınıfın constructor’ının data
field’larını initialize ettiğini ve her sınıfın root sınıfta tanımlı tüm abstract method’ları implement ettiğini garanti
eder. Bu, eksik switch case nedeniyle runtime hatası olasılığını ortadan kaldırır. Birden fazla programcı, root sınıfın
source'una erişmeden class hierarchy’i bağımsız ve birlikte çalışabilir şekilde extend edebilir. Her tür `(flavor)` ile
ilişkili ayrı bir data type'ı vardır, bu da programcıların bir değişkenin türünü (flavor) belirtmesini ve değişkenleri
ile input parametrelerini belirli bir tür `(flavor)` ile sınırlamasını sağlar.

Class hierarchy’lerinin bir diğer avantajı, type'lar arasındaki natural hiyerarşik ilişkileri reflect edecek şekilde
oluşturulabilmeleri, bu sayede artan flexibility ve daha iyi compile time type checking sağlamalarıdır. Orijinal
örnekteki tagged sınıfın `Square`'lere de izin verdiğini varsayalım. Class hierarchy, bir `Square`'in special bir
`Rectangle` türü olduğu gerçeğini reflect edecek şekilde oluşturulabilir (her ikisi de immutable varsayılırsa):

```
class Square extends Rectangle {
    Square(double side) {
        super(side, side);
    }
}
```

Yukarıdaki hierarchy’deki field’ların accessor method’lar yerine directly erişildiğine dikkat edin. Bu, kısalık için
yapılmıştır ve hierarchy public olsaydı kötü bir tasarım olurdu.

Özetle, tagged sınıflar nadiren uygundur. Eğer explicit bir tag field içeren bir sınıf yazmayı düşünüyorsanız, tag’in
kaldırılıp sınıfın bir hierarchy ile değiştirilip değiştirilemeyeceğini düşünün. Bir tag field içeren mevcut bir sınıfa
rastladığınızda, onu bir hierarchy’ye dönüştürmeyi düşünün.