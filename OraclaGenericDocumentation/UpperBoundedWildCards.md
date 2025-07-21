# Upper Bounded Wildcards

Bir variable üzerindeki kısıtlamaları gevşetmek için `upper bounded wildcard` kullanabilirsiniz. Örneğin,
`List<Integer>`, `List<Double>` ve `List<Number>` üzerinde çalışan bir method yazmak istiyorsanız; bunu upper bounded
wildcard kullanarak başarabilirsiniz.

Upper-bounded wildcard'ı declare etmek için wildcard karakteri `?`, ardından extends keyword'ü ve son olarak üst
sınırı `(upper bound)` kullanılır. Bu context'de, `extends` terimi genel anlamda ya "extends" (class'larda olduğu gibi)
ya da "implements" (interface'lerde olduğu gibi) anlamında kullanılır.

Number ve onun subtype'lari olan Integer, Double ve Float gibi type'ların listeleri üzerinde çalışan method'u yazmak
için `List<? extends Number>` belirtirsiniz. `List<Number>` terimi, sadece Number type'ında ki bir liste ile eşleşirken,
`List<? extends Number>` terimi Number türündeki veya onun herhangi bir subclass'ına ait liste ile eşleştiği için daha
kısıtlayıcıdır.

Aşağıdaki process method'unu dikkate alın:

```
public static void process(List<? extends Foo> list) { /* ... */ }
```

Upper bounded wildcard, `<? extends Foo>`, burada Foo herhangi bir type olabilir, Foo ve Foo'nun herhangi bir subtype'i
ile eşleşir. Process method, liste elementlerine `Foo` type'ı olarak erişebilir:

```
public static void process(List<? extends Foo> list) {
    for (Foo elem : list) {
        // ...
    }
}
```

Foreach ifadesinde, elem variable'ı listedeki her bir element üzerinde iteration yapar. Artık Foo class'ında tanımlanmış
herhangi bir method elem üzerinde kullanılabilir.

SumOfList method'u listedeki sayıların toplamını döner:

```
public static double sumOfList(List<? extends Number> list) {
    double s = 0.0;
    for (Number n : list)
        s += n.doubleValue();
    return s;
}
```

Aşağıdaki kod, Integer object'lerden oluşan bir liste kullanarak `sum = 6.0` yazdırır:

```
List<Integer> li = Arrays.asList(1, 2, 3);
System.out.println("sum = " + sumOfList(li));
```

Double değerlerden oluşan bir liste aynı `sumOfList` method'unu kullanabilir. Aşağıdaki kod `sum = 7.0` yazdırır:

```
List<Double> ld = Arrays.asList(1.2, 2.3, 3.5);
System.out.println("sum = " + sumOfList(ld));
```






