# Eliminate unchecked warnings

Generic'lerle program yaparken birçok compiler uyarısı göreceksiniz: unchecked cast uyarıları, unchecked method
invocation uyarıları, unchecked parameterized vararg type uyarıları ve unchecked conversion uyarıları. Generic'lerle ne
kadar çok deneyim kazanırsanız, o kadar az uyarı alırsınız, ancak yeni yazılan kodun tamamen uyarısız compile edilmesini
beklemeyin. Birçok unchecked uyarısı kolayca giderilebilir. Örneğin, yanlışlıkla şu declaration'ı yaptığınızı
varsayalım:

```
Set<Lark> exaltation = new HashSet();
```

Compiler size nazikçe neyi yanlış yaptığınızı hatırlatacaktır:

```
Venery.java:4: warning: [unchecked] unchecked conversion
Set<Lark> exaltation = new HashSet();
^
required: Set<Lark>
found: HashSet
```

Daha sonra belirtilen düzeltmeyi yapabilir ve uyarının kaybolmasını sağlayabilirsiniz. Type parametresini aslında
belirtmeniz gerekmez, sadece Java 7 ile gelen diamond operatörü `(<>)` ile type parametresinin var olduğunu göstermeniz
yeterlidir. Compiler daha sonra doğru actual type parametresini `(bu durumda Lark)` infer edecektir:

```
Set<Lark> exaltation = new HashSet<>();
```

Bazı uyarıları ortadan kaldırmak çok daha zor olacaktır. Bu bölüm, bu tür uyarıların örnekleriyle doludur. Düşünmeyi
gerektiren uyarılar aldığınızda, sabırlı olun! Mümkün olan her unchecked uyarıyı giderin. Tüm uyarıları giderirseniz,
kodunuzun type-safe olduğundan emin olursunuz ki bu çok iyi bir şeydir. Bu, runtime'da `ClassCastException`
almayacağınız anlamına gelir ve programınızın istediğiniz şekilde davranacağına olan güveninizi artırır. Bir uyarıyı
gideremiyorsanız ancak uyarıya neden olan kodun type-safe olduğunu kanıtlayabiliyorsanız, o zaman (ve yalnızca o
zaman) uyarıyı `@SuppressWarnings("unchecked")` annotation'ı ile bastırın. Uyarıları kodun type-safe olduğunu
kanıtlamadan bastırırsanız, kendinize yanlış bir güvenlik hissi veriyorsunuz demektir. Kod uyarı vermeden compile
edilebilir, ancak runtime'da yine de `ClassCastException` fırlatabilir. Ancak, safe olduğunu bildiğiniz unchecked
uyarıları bastırmak yerine görmezden gelirseniz, gerçek bir sorunu temsil eden yeni bir uyarı ortaya çıktığında bunu
fark edemezsiniz. Yeni uyarı, susturmadığınız tüm false alarmlar arasında kaybolacaktır.

SuppressWarnings annotation'ı, bireysel `(individual)` bir local variable declaration'ınından tüm bir sınıfa kadar
herhangi bir bildirime uygulanabilir. SuppressWarnings annotation'nını her zaman mümkün olan en küçük scope'da kullanın.
Genellikle bu, bir değişken declaration'ı veya çok kısa bir metot ya da constructor olur. SuppressWarnings
annotation'ınını asla tüm bir sınıfta kullanmayın. Bunu yapmak kritik uyarıların gizlenmesine neden olabilir.

Eğer SuppressWarnings annotation'ını birden fazla satır uzunluğunda bir method veya constructor üzerinde
kullanıyorsanız, bunu bir local variable declaration üzerine taşıyabilirsiniz. Yeni bir local variable declare etmeniz
gerekebilir, ancak buna değer. Örneğin, ArrayList'ten gelen bu toArray method'una bakın:

```
public <T> T[] toArray(T[] a) {
    if (a.length < size)
        return (T[]) Arrays.copyOf(elements, size, a.getClass());
        System.arraycopy(elements, 0, a, 0, size);
    if (a.length > size)
        a[size] = null;
    return a;
}
```

Eğer ArrayList'i compile ederseniz, method bu warning'i üretir:

```
ArrayList.java:305: warning: [unchecked] unchecked cast
return (T[]) Arrays.copyOf(elements, size, a.getClass());
^
required: T[]
found: Object[]
```

SuppressWarnings annotation'ını return statement üzerine koymak yasaktır, çünkü return statement bir declaration
değildir `[JLS, 9.7]`. Annotation'ı tüm method üzerine koymaya teşvik edilebilirsiniz, ancak koymayın. Bunun yerine,
return değerini tutacak bir local variable declare edin ve declaration'ına annotation ekleyin, şöyle:

```
// @SupressWarnings scope'unu azaltmak için local variable ekleme
public <T> T[] toArray(T[] a) {
    if (a.length < size) {
        // Bu cast doğrudur çünkü oluşturduğumuz array geçilen array ile aynı type'a sahiptir, yani T[].
        @SuppressWarnings("unchecked") 
        T[] result = (T[]) Arrays.copyOf(elements, size, a.getClass());
        return result;
    }

    System.arraycopy(elements, 0, a, 0, size);
    if (a.length > size)
        a[size] = null;
    return a;
}
```

Ortaya çıkan method sorunsuz bir şekilde compile olur ve unchecked warning'lerin suppress edildiği scope'u minimize
eder.

Her `@SuppressWarnings("unchecked")` annotation'ı kullandığınızda, neden bunun safe olduğunu belirten bir comment
ekleyin. Bu, başkalarının kodu anlamasına yardımcı olur ve daha da önemlisi, birinin kodu güvenli olmayan hale getirecek
şekilde değiştirme olasılığını azaltır. Böyle bir comment'i yazmakta zorlanıyorsanız, düşünmeye devam edin. Sonunda
unchecked operation'ın aslında safe olmadığını fark edebilirsiniz.

Özetle, unchecked warning'ler önemlidir. Onları görmezden gelmeyin. Her unchecked warning, runtime'da ClassCastException
oluşma potansiyelini temsil eder. Bu warning'leri ortadan kaldırmak için elinizden geleni yapın. Eğer bir unchecked
warning'i ortadan kaldıramıyorsanız ve uyarıyı tetikleyen kodun typesafe olduğunu kanıtlayabiliyorsanız, warning'i
mümkün olan en dar `(narrowest)` scope'da `@SuppressWarnings("unchecked")` annotation'ı ile suppress edin. Warning'i
suppress etme kararınızın gerekçesini bir comment'e kaydedin.