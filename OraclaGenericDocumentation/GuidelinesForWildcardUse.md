# Guidelines for Wildcard Use

Generics ile programlamayı öğrenirken en kafa karıştırıcı konulardan biri, ne zaman upper bounded wildcard ne zaman
lower bounded wildcard kullanılacağını belirlemektir. Bu sayfa, kodunuzu tasarlarken izlemeniz gereken bazı kılavuzlar
sağlar.

Bu tartışma için, variable'ların iki function'dan birini sağladığını düşünmek faydalıdır:

* An "In" Variable : Bir `in` variable, koda data sağlar. İki argümanlı `copy(src, dest)` method'unu hayal edin. `src`
  argümanı kopyalanacak data'yı sağladığı için `in` parameter'dır.

* An "Out" Variable : Bir `out` variable, başka bir yerde kullanılmak üzere data'yı tutar. Copy örneğinde, copy(src,
  dest) `dest` argümanı data'yı kabul eder, bu nedenle `out` parameter'dır.

Elbette, bazı variable'lar hem `in` hem de `out` amaçlı kullanılır. Wildcard kullanıp kullanmamaya ve hangi tür
wildcard'ın uygun olduğuna karar verirken "in" ve "out" prensibini kullanabilirsiniz. Aşağıdaki liste, izlenmesi gereken
kılavuzları sağlar:

### Wildcard guidelines:

* Bir `in` variable, `extends` keyword'ü kullanılarak `upper bounded wildcard` ile define edilir.

* Bir `out` variable, `super` keyword'ü kullanılarak `lower bounded wildcard` ile tanımlanır.

* `In` variable, Object class'ında tanımlı method'lar kullanılarak erişilebiliyorsa, `unbounded wildcard` kullanın.

* Kodun bir variable'a hem `in` hem de `out` olarak erişmesi gerekiyorsa, wildcard kullanmayın.

Bu kılavuzlar bir method'un return type'ı için geçerli değildir. Return type olarak wildcard kullanmaktan kaçınılmalıdır
çünkü bu, kodu kullanan programcıların wildcard'larla uğraşmasını zorunlu kılar.

`List<? extends ...>` ile define edilmiş bir liste gayri resmi olarak salt okunur `(read-only)` olarak düşünülebilir,
ancak bu kesin bir garanti değildir.

Aşağıdaki iki class'a sahip olduğunuzu varsayın:

```
class NaturalNumber {
    private int i;

    public NaturalNumber(int i) {
        this.i = i;
    }
    // ...
}

class EvenNumber extends NaturalNumber{
    public EvenNumber(int i){
        super(i);
    }
    // ...
}
```

Aşağıdaki kodu dikkate alın:

```
public static void main(String[] args) {
    List<EvenNumber> le = new ArrayList<>();
    List<? extends NaturalNumber> ln = le;
    ln.add(new NaturalNumber(35)); // => COMPILER ERROR
}
```

`List<EvenNumber>`, `List<? extends NaturalNumber>`'ın subtype'ı olduğu için, `le`'i `ln`'ye assign edebilirsiniz.
Ancak `ln`'yi kullanarak bir NaturalNumber ekleyip `EvenNumber` listesini değiştiremezsiniz.

List üzerinde aşağıdaki işlemler mümkündür:

* Null ekleyebilirsiniz.

* `clear` invoke edebilirsiniz

* Iterator alabilir ve remove method'unu invoke edebilirsiniz.

* Wildcard'ı capture edip listeden read ettiğiniz element'leri write edebilirsiniz.

`List<? extends NaturalNumber>` ile tanımlanmış listenin kelimenin tam anlamıyla salt okunur olmadığını görebilirsiniz,
ancak listeye yeni bir element ekleyemediğiniz veya mevcut bir elementi değiştiremediğiniz için böyle düşünebilirsiniz.