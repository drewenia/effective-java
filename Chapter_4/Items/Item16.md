# In public classes, use accessor methods, not public fields

# Public class’larda, public field’lar yerine accessor method’ları kullanın.

Bazen, yalnızca instance field’ları gruplamak dışında hiçbir amacı olmayan degenerate class’lar yazma eğiliminde
olabilirsiniz:

```
// Bu tür degenerate class’lar public olmamalıdır!
class Point {
    public double x;
    public double y;
}
```

Bu tür class'ların data field'larına directly erişildiği için, bu class'lar encapsulation avantajlarını sunmaz.
Representation'ı API'yi değiştirmeden değiştiremezsin, değişmezleri `(invariants)` zorlayamazsın ve bir field'a
erişildiğinde yardımcı `(auxiliary)` bir action gerçekleştiremezsin. Sertlik yanlısı object-oriented programcılar bu tür
class'ları tamamen yanlış bulur ve her zaman private field'lara ve public accessor method'lara (getter'lar) ve mutable
class'lar için mutator'lara (setter'lar) sahip class'larla değiştirilmesi gerektiğini düşünür.

```
// Data’nın accessor method’lar ve mutator’lar ile encapsulation’ı
class Point {
    private double x;
    private double y;
    
    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() { return x; }

    public double getY() { return y; }

    public void setX(double x) { this.x = x; }

    public void setY(double y) { this.y = y; }
}
```

Kesinlikle, public sınıflar konusunda katı tutum sergileyenler haklı: Bir class package dışından erişilebilir
durumdaysa, class'ın internal representation'ını değiştirme esnekliğini korumak için accessor method'lar sağlamalısın.
Eğer bir public class data field'larını açığa çıkarırsa `(expose)`, representation'ını değiştirme umudu tamamen kaybolur
çünkü client code her yere dağılmış `(distributed)` olabilir.

Ancak, bir class `package-private` ya da `private` bir nested class ise, sağladığı abstraction'ı yeterince iyi
tanımladıkları sürece data field'larını açığa çıkarmasında `(exposing)` doğası gereği yanlış bir şey yoktur. Bu
yaklaşım, hem class definition'ının da hem de onu kullanan client code'da accessor method yaklaşımına göre daha az
görsel karmaşa yaratır. Client code class'ın internal representation'ına bağlı `(tied)` olsa da, bu code class'ı içeren
package ile sınırlıdır. Eğer representation'da bir değişiklik istenirse, package dışındaki herhangi bir koda dokunmadan
değişikliği yapabilirsin. Private nested class durumunda, değişikliğin scope'u daha da sınırlanarak sadece kapsayan
`(enclosing)` class ile sınırlı olur.

Java platform library'lerinde ki birkaç class, public class'ların field'ları doğrudan açığa çıkarmaması `(not expose)`
gerektiği tavsiyesini ihlal eder. Dikkate değer örnekler arasında `java.awt` package'ındaki `Point` ve `Dimension`
class'ları yer alır. Taklit `(emulated)` edilecek örnekler olmaktan ziyade, bu class'lar uyarıcı hikâyeler olarak
görülmelidir. Dimension class'ının internal'ını açığa çıkarma `(expose)` kararı, bugün hâlâ devam eden ciddi bir
performans problemine yol açtı.

Bir public class'ın field'ları directly açığa çıkarması `(expose)` hiçbir zaman iyi bir fikir olmasa da, field'lar
immutable ise bu durum daha az zararlıdır. Böyle bir class'ın representation'ını API'sini değiştirmeden değiştiremezsin
ve bir field okunduğunda yardımcı `(auxiliary)` işlemler `(action)` gerçekleştiremezsin, ancak değişmezleri (invariant)
zorlayabilirsin. Örneğin, bu class her instance'ın geçerli bir `time` represent ettiğini garanti eder:

```
//Field'ları açıkta (exposed) olan public class - sorgulanabilir
final class Time{
    private static final int HOURS_PER_DAY = 24;
    private static final int MINUTES_PER_HOUR = 60;

    public final int hour;
    public final int minute;

    public Time(int hour, int minute){
        if (hour < 0 || hour >= HOURS_PER_DAY)
            throw new IllegalArgumentException("Hour : " + hour);

        if (minute < 0 || minute >= MINUTES_PER_HOUR)
            throw new IllegalArgumentException("Min: " + minute);

        this.hour = hour;
        this.minute = minute;
    }
    
    ... // Remainder omitted
}
```

Özetle, public class'lar hiçbir zaman mutable field'ları açığa çıkarmamalıdır `(expose)`. Public class'ların immutable
field'ları açığa çıkarması `(expose)` daha az zararlı olsa da, yine de sorgulanmalıdır. Ancak, `package-private` veya
`private nested class`'ların `mutable` ya da `immutable` olsun, field'ları açığa çıkarması `(expose)` bazen arzu edilir.