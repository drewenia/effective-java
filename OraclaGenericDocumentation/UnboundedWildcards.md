# Unbounded Wildcards

Unbounded wildcard türü, wildcard karakteri `?` kullanılarak belirtilir, örneğin List<`?`>. Buna `unknown type` liste
denir. `Unbounded wildcard`'ın kullanışlı olduğu iki durum vardır:

* Object class'ında sağlanan functionality kullanılarak implemented bir method yazıyorsanız.

* Kod, generic class'taki type parameter'a depend olmayan method'ları kullanıyorsa. Örneğin, List.size veya List.clear.
  Aslında, Class<`?`> çok sık kullanılır çünkü `Class<T>`'deki çoğu method `T`'ye depent değildir.

Aşağıdaki printList method'unu dikkate alın:

```
public static void printList(List<Object> list) {
    for (Object elem : list){
        System.out.println(elem + " ");
    }
    System.out.println();
}
```

`printList`'in amacı herhangi bir type'dan listeyi yazdırmaktır, ancak bu hedefe ulaşamaz — sadece Object
instance'larından oluşan bir listeyi yazdırır; `List<Integer>`, `List<String>`, `List<Double>` vb. yazdıramaz çünkü
bunlar `List<Object>`'in subtype'ları değildir.

Generic bir printList method'u yazmak için List<`?`> kullanın:

```
public static void printList(List<?> list) {
    for (Object elem: list)
        System.out.print(elem + " ");
    System.out.println();
}
```

Herhangi concrete bir `A` type için, `List<A>`, List<`?`>'nin subtype'ı olduğundan, printList'i herhangi bir type'da ki
listeyi yazdırmak için kullanabilirsiniz:

```
List<Integer> li = Arrays.asList(1, 2, 3);
List<String>  ls = Arrays.asList("one", "two", "three");
printList(li);
printList(ls);
```

* Note : `Arrays.asList` method'u bu ders boyunca örneklerde kullanılmıştır. Bu static factory method belirtilen array'i
  convert eder ve fixed-size bir liste döner.

`List<Object>` ve List<`?`>'nin aynı olmadığını belirtmek önemlidir. Bir Object veya Object'in herhangi bir subtype'ını
`List<Object>`'e ekleyebilirsiniz. Ancak List<`?`>'ye sadece `null` ekleyebilirsiniz.