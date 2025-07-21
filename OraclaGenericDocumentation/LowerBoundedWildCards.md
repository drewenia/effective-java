# Lower Bounded Wildcards

Upper Bounded Wildcards bölümü, upper bounded wildcard'ın unknown type'ı belirli bir type veya o type'ın bir subtype'ı
ile sınırlandırdığını ve `extends` keyword'ü kullanılarak represent edildiğini gösterir. Benzer şekilde, lower bounded
wildcard unknown type'ı belirli bir type veya o type'ın bir super type'ı ile sınırlandırır.

Lower bounded wildcard, wildcard karakteri `?` kullanılarak, ardından `super` keyword'ü ve ardından alt sınırı (lower
bound) belirterek ifade edilir: `<? super A>`.

* Note : Bir wildcard için upper bound belirtebilirsiniz ya da lower bound belirtebilirsiniz, ancak ikisini birden
  belirtemezsiniz.

Integer object'lerini bir listeye ekleyen bir method yazmak istediğinizi varsayın. Esnekliği en üst düzeye çıkarmak
için, method'unuzun `List<Integer>`, `List<Number>` ve `List<Object>` — yani Integer değerleri tutabilen her şey —
üzerinde çalışmasını istersiniz.

Integer ve onun supertypes'leri olan Integer, Number ve Object gibi type'ların listeleri üzerinde çalışan method'u
yazmak için `List<? super Integer>` belirtirsiniz. `List<Integer>` terimi, sadece Integer type'ında ki bir liste ile
eşleşirken, `List<? super Integer>` terimi Integer türünün supertype'ı olan herhangi bir type'da ki liste ile eşleştiği
için daha kısıtlayıcıdır.

Aşağıdaki kod, 1'den 10'a kadar olan sayıları bir listenin sonuna ekler:

```
public static void addNumbers(List<? super Integer> list){
    for (int i = 0; i < 10; i++) {
        list.add(i);
    }
}
```