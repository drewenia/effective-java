# Use interfaces only to define types

Bir sınıf bir interface’i implement ettiğinde, interface sınıfın instance'larına referans olarak kullanılabilen bir type
görevi görür. Bir sınıfın bir interface’i implement etmesi, bir client’ın sınıfın instance'ları ile ne yapabileceği
hakkında bilgi vermelidir. Başka amaçlarla interface define etmek uygun değildir.

Bu testi geçemeyen interface türlerinden biri, sözde constant interface’tir. Böyle bir interface hiçbir method içermez;
yalnızca her biri bir constant’ı export eden static final field’lardan oluşur. Bu constant’ları kullanan sınıflar,
constant isimlerini bir sınıf adıyla niteleme gerekliliğinden kaçınmak için interface’i implement ederler. İşte bir
örnek:

```
// Constant interface antipattern - do not use!
public interface PhysicalConstants {
    // Avogadro's number (1/mol)
    static final double AVOGADROS_NUMBER = 6.022_140_857e23;
    
    // Boltzmann constant (J/K)
    static final double BOLTZMANN_CONSTANT = 1.380_648_52e-23;
    
    // Mass of the electron (kg)
    static final double ELECTRON_MASS = 9.109_383_56e-31;
```

Constant interface pattern, interface’lerin kötü bir kullanım şeklidir. Bir sınıfın bazı constant’ları internally olarak
kullanması, bir implementation detail’dir. Bir constant interface’i implement etmek, bu implementation detail’in sınıfın
exported API’sine sızmasına `(leak)` neden olur. Bir sınıfın bir constant interface’i implement etmesi, o sınıfın
kullanıcıları açısından hiçbir anlam ifade etmez. Hatta bu durum onları kafa karışıklığına bile sürükleyebilir. Daha da
kötüsü, bu bir bağlılık anlamına gelir: eğer gelecekteki bir sürümde sınıf, artık bu constant’lara ihtiyaç duymayacak
şekilde değiştirilirse bile, binary compatibility’yi korumak için hâlâ interface’i implement etmek zorundadır. Eğer
nonfinal bir sınıf constant interface’i implement ederse, tüm subclass'larının namespace’i interface’teki constant’larla
kirlenir.

Java platform library'lerinde, örneğin `java.io.ObjectStreamConstants` gibi birkaç constant interface vardır. Bu
interface’ler anomali olarak görülmeli ve örnek `(emulated)` alınmamalıdır.

Constant’ları export etmek istiyorsanız, birkaç makul seçenek vardır. Eğer constant’lar mevcut bir sınıfa veya
interface’e sıkı sıkıya bağlıysa `(strongly tied)`, onları o sınıfa veya interface’e eklemelisiniz. Örneğin, Integer ve
Double gibi boxed numerical primitive sınıflarının tamamı `MIN_VALUE` ve `MAX_VALUE` constant’larını export eder. Eğer
constant’lar bir enumerated type'ının member'ları olarak görülüyorsa, onları bir enum type'ı ile dışa aktarmalısınız.
Aksi takdirde, constant’ları `noninstantiable` bir utility sınıfı ile export etmelisiniz. İşte daha önce gösterilen
PhysicalConstants örneğinin utility sınıfı versiyonu:

```
// Constant utility class
package com.effectivejava.science;
public class PhysicalConstants {
    private PhysicalConstants() { } // Prevents instantiation

    public static final double AVOGADROS_NUMBER = 6.022_140_857e23;
    public static final double BOLTZMANN_CONSTANT = 1.380_648_52e-23;
    public static final double ELECTRON_MASS = 9.109_383_56e-31;
```

Bu arada, numeric literal’lerde alt çizgi karakterinin `(_)` kullanımına dikkat edin. Java 7’den beri geçerli olan alt
çizgiler `(_)`, numeric literal’lerin değerleri üzerinde hiçbir etkisi yoktur ancak uygun kullanıldığında okumayı çok
daha kolaylaştırabilir. Beş veya daha fazla ardışık rakam içeren sabit veya floating poing literal’lere alt çizgi
eklemeyi düşünün. On tabanında olan, ister integral ister floating-poing olsun, literal’leri binin pozitif ve negatif
kuvvetlerini gösteren üçlü rakam gruplarına ayırmak için alt çizgi kullanmalısınız.

Normalde bir utility sınıfı, client’ların constant isimlerini sınıf adıyla nitelendirmesini gerektirir; örneğin,
`PhysicalConstants.AVOGADROS_NUMBER`. Bir utility sınıfının export ettiği constant’ları yoğun şekilde kullanıyorsanız,
static import özelliğini kullanarak constant’ları sınıf adıyla niteleme ihtiyacından kaçınabilirsiniz:

```
// Use of static import to avoid qualifying constants
import static com.effectivejava.science.PhysicalConstants.*;

public class Test {
    double atoms(double mols) {
        return AVOGADROS_NUMBER * mols;
    }
    ...
    // Many more uses of PhysicalConstants justify static import
}
```

Özetle, interface’ler yalnızca type defination için kullanılmalıdır. Sadece constant export etmek için
kullanılmamalıdır.