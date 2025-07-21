# Wildcard Capture and Helper Methods

Bazı durumlarda compiler, wildcard'ın type'ını infer eder. Örneğin, bir liste List<`?`> olarak define edilebilir ancak
bir expression değerlendirilirken compiler, koddan belirli bir type'ı infer eder. Bu durum wildcard capture olarak
bilinir.

Çoğunlukla wildcard capture konusunda endişelenmenize gerek yoktur, sadece "capture of" ifadesi içeren bir hata mesajı
gördüğünüzde dikkat etmelisiniz.

WildcardError örneği compile edildiğinde capture error üretir:

```
public class WildcardError {
    void foo(List<?> i) {
        i.set(0, i.get(0));
    }
}
```

Bu örnekte compiler, `i` input parameter'ını Object type olarak process eder. Foo method'u `List.set(int, E)`'yi invoke
ettiğinde, compiler listeye eklenen object'in type'ını doğrulayamaz ve bir hata oluşur. Bu tür bir hata oluştuğunda,
genellikle compiler'ın bir variable'a yanlış type atadığınızı düşündüğü anlamına gelir. Generics, bu nedenle Java diline
eklendi — compile zamanı typesafe'liği sağlamak için.

WildcardError örneği, Oracle'ın JDK 7 javac implementasyonu tarafından compile edildiğinde aşağıdaki hatayı üretir:

```
WildcardError.java:6: error: method set in interface List<E> cannot be applied to given types;
    i.set(0, i.get(0));
     ^
  required: int,CAP#1
  found: int,Object
  reason: actual argument Object cannot be converted to CAP#1 by method invocation conversion
  where E is a type-variable:
    E extends Object declared in interface List
  where CAP#1 is a fresh type-variable:
    CAP#1 extends Object from capture of ?
1 error
```

Bu örnekte, kod güvenli bir operation yapmaya çalışıyor; peki compiler hatasının önüne nasıl geçebilirsiniz? Wildcard'ı
capture eden private helper method yazarak düzeltebilirsiniz. Bu durumda, WildcardFixed'te gösterildiği gibi private
helper method olan `fooHelper`'ı oluşturarak sorunu çözebilirsiniz.

```
class WildCardFixed {
    void foo(List<?> i) {
        fooHelper(i);
    }

    // Wildcard'ın type inference yoluyla capture edilebilmesi için oluşturulmuş helper method.
    private <T> void fooHelper(List<T> list) {
        list.set(0, list.getFirst());
    }
}
```

Helper method sayesinde compiler, invocation'da `T`'nin capture değişkeni `CAP#1` olduğunu inference ile belirler. Örnek
artık başarılı bir şekilde compile olur. Konvansiyon olarak, helper method'lar genellikle originalMethodNameHelper
şeklinde isimlendirilir.

Şimdi daha karmaşık bir örnek olan `WildcardErrorBad`'i dikkate alın:

```
class WildCardErrorBad{
    void swapFirst(List<? extends Number> l1, List<? extends Number> l2){
        Number temp = l1.getFirst();
        
        // CAP#1 extends Number bekleniyor, CAP#2 extends Number alındı; Aynı bound, ancak farklı type'lar.
        l1.set(0,l2.getFirst()); // => COMPILER ERROR
        
        // CAP#1 extends Number bekleniyor, Number alındı
        l2.set(0,temp); // => COMPILER ERROR
    }
}
```

Bu örnekte, kod güvensiz bir işlem yapmaya çalışıyor. Örneğin, aşağıdaki swapFirst method invocation'ı dikkate alın:

```
List<Integer> li = Arrays.asList(1, 2, 3);
List<Double>  ld = Arrays.asList(10.10, 20.20, 30.30);
swapFirst(li, ld);
```

`List<Integer>` ve `List<Double>` her ikisi de `List<? extends Number>` kriterini karşılamasına rağmen, Integer değerler
içeren bir listeden bir öğe alıp bunu Double değerler içeren bir listeye koymaya çalışmak açıkça yanlıştır. Oracle'ın
JDK javac compiler'ı ile compile edilen kod aşağıdaki hatayı üretir:

```
WildcardErrorBad.java:7: error: method set in interface List<E> cannot be applied to given types;
      l1.set(0, l2.get(0)); // expected a CAP#1 extends Number,
        ^
  required: int,CAP#1
  found: int,Number
  reason: actual argument Number cannot be converted to CAP#1 by method invocation conversion
  where E is a type-variable:
    E extends Object declared in interface List
  where CAP#1 is a fresh type-variable:
    CAP#1 extends Number from capture of ? extends Number
WildcardErrorBad.java:10: error: method set in interface List<E> cannot be applied to given types;
      l2.set(0, temp);      // expected a CAP#1 extends Number,
        ^
  required: int,CAP#1
  found: int,Number
  reason: actual argument Number cannot be converted to CAP#1 by method invocation conversion
  where E is a type-variable:
    E extends Object declared in interface List
  where CAP#1 is a fresh type-variable:
    CAP#1 extends Number from capture of ? extends Number
WildcardErrorBad.java:15: error: method set in interface List<E> cannot be applied to given types;
        i.set(0, i.get(0));
         ^
  required: int,CAP#1
  found: int,Object
  reason: actual argument Object cannot be converted to CAP#1 by method invocation conversion
  where E is a type-variable:
    E extends Object declared in interface List
  where CAP#1 is a fresh type-variable:
    CAP#1 extends Object from capture of ?
3 errors
```

Sorunu aşmak için bir helper method yoktur, çünkü kod temelde yanlıştır: Integer değerler içeren bir listeden bir öğe
alıp bunu Double değerler içeren bir listeye koymaya çalışmak açıkça yanlıştır.