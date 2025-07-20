# Raw Types

Raw type, herhangi bir type argument içermeyen generic bir class veya interface adıdır. Örneğin, generic Box class'ı göz
önüne alındığında:

```java
public class Box<T> {
    public void set(T t) { /* ... */ }
    // ...
}
```

`Box<T>` için parameterized type oluşturmak için, formal type parameter `T` için gerçek bir type argument sağlarsınız:

```
Box<Integer> intBox = new Box<>();
```

Gerçek type argument belirtilmezse, `Box<T>`'nin bir `raw type`'ı oluşturulmuş olur:

```
Box rawBox = new Box();
```

Bu nedenle, `Box`, generic type `Box<T>`'nin `raw type`'ıdır. Ancak, generic olmayan bir class veya interface type'ı raw
type değildir.

Raw type'lar, birçok API class'ının (örneğin Collections class'ları) JDK 5.0 öncesinde generic olmadığı legacy kodlarda
karşınıza çıkar. Raw type kullanırken, temelde pre-generic davranışı elde edersiniz — bir `Box` size `Object` verir.
Geriye dönük uyumluluk için, parameterized type'ın raw type'ına ataması izinlidir:

```
Box<String> stringBox = new Box<>();
Box rawBox = stringBox;               // OK
```

Ancak raw type'ı parameterized type'a atarsanız, bir uyarı alırsınız:

```
Box rawBox = new Box();           // rawBox is a raw type of Box<T>
Box<Integer> intBox = rawBox;     // warning: unchecked conversion
```

Ayrıca, karşılık gelen generic type içinde tanımlanmış generic method'ları çağırmak için raw type kullanırsanız da uyarı
alırsınız:

```
Box<String> stringBox = new Box<>();
Box rawBox = stringBox;
rawBox.set(8);  // warning: unchecked invocation to set(T)
```

Uyarı, raw type'ların generic type kontrollerini atladığını ve unsafe kodun yakalanmasının runtime'a ertelendiğini
`(deferring)` gösterir. Bu nedenle, raw type kullanmaktan kaçınmalısınız.

## Unchecked Error Messages

Daha önce belirtildiği gibi, legacy kodu generic kodla karıştırdığınızda, aşağıdakine benzer uyarı mesajlarıyla
karşılaşabilirsiniz:

```
Note: Example.java uses unchecked or unsafe operations.
Note: Recompile with -Xlint:unchecked for details.
```

Bu, raw type'lar üzerinde çalışan eski bir API kullanırken olabilir, aşağıdaki örnekte gösterildiği gibi:

```
public static void main(String[] args) {
    Box<Integer> bi;
    bi = createBox();
}

static Box createBox(){
    return new Box();
}
```

"Unchecked" terimi, compiler'ın type safety'yi sağlamak için gerekli tüm type kontrollerini yapacak yeterli type
bilgisine sahip olmadığı anlamına gelir. "Unchecked" uyarısı default olarak devre dışıdır, ancak compiler bir ipucu
verir. Tüm "unchecked" uyarılarını görmek için, `-Xlint:unchecked` ile yeniden compile edin.

Önceki örneği `-Xlint:unchecked` ile yeniden compile etmek, aşağıdaki ek bilgileri ortaya çıkarır:

```
WarningDemo.java:4: warning: [unchecked] unchecked conversion
found   : Box
required: Box<java.lang.Integer>
        bi = createBox();
                      ^
1 warning
```

Unchecked uyarılarını tamamen devre dışı bırakmak için `-Xlint:-unchecked` flag'ini kullanın.
`@SuppressWarnings("unchecked")` annotation'ı unchecked uyarılarını bastırır.