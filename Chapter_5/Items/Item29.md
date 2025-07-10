# Favor generic types

Genel olarak, declaration'larını parameterize etmek ve JDK tarafından sağlanan generic type'ları ve method'ları
kullanmak çok zor değildir. Kendi generic type'larını yazmak biraz daha zordur, ancak nasıl yapılacağını öğrenmeye
değer. Item 7'deki basit stack implementasyonunu düşün:

```
// Object-based collection - a prime candidate for generics
public class Stack {
    private Object[] elements;
    private int size = 0;
    private static final int DEFAULT_INITIAL_CAPACITY = 16;

    public Stack() {
        elements = new Object[DEFAULT_INITIAL_CAPACITY];
    }

    public void push(Object e) {
        ensureCapacity();
        elements[size++] = e;
    }

    public Object pop() {
        if (size == 0)
            throw new EmptyStackException();

        Object result = elements[--size];
        elements[size] = null; // Eliminate obsolete reference
        return result;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    private void ensureCapacity() {
        if (elements.length == size)
            elements = Arrays.copyOf(elements, 2 * size + 1);
    }
}
```

Bu class en başta parameterize edilmeliydi, fakat edilmediği için, sonradan generic hale getirebiliriz. Başka bir
deyişle, orijinal parameterize edilmemiş versiyonun client'larına zarar vermeden onu parameterize edebiliriz. Şu
haliyle, client stack'ten çıkarılan `(popped)` object'leri cast etmek zorundadır ve bu cast'ler runtime'da başarısız
olabilir. Bir class'ı generic hale getirmenin ilk adımı, declaration'ına bir veya daha fazla type parameter eklemektir.
Bu case de, stack'in element type'ını represent eden bir type parameter vardır ve bu type parameter için geleneksel isim
`E`'dir. Sonraki adım, Object type'ının tüm kullanımlarını uygun type parameter ile değiştirmek ve ardından ortaya çıkan
programı compile etmeyi denemektir.

```
// Stack'i generic hale getirmek için yapılan ilk deneme - compile edilmeyecek!
public class Stack<E> {
    private E[] elements;
    private int size = 0;
    private static final int DEFAULT_INITIAL_CAPACITY = 16;

    public Stack() {
        elements = new E[DEFAULT_INITIAL_CAPACITY];
    }

    public void push(E e) {
        ensureCapacity();
        elements[size++] = e;
    }

    public E pop() {
        if (size == 0) throw new EmptyStackException();
        E result = elements[--size];
        elements[size] = null; // Eski referansı eliminate et
        return result;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    private void ensureCapacity() {
        if (elements.length == size)
            elements = Arrays.copyOf(elements, 2 * size + 1);
    }
}
```

Genellikle en az bir error veya warning alırsınız ve bu class da istisna değildir. Neyse ki, bu class yalnızca bir error
üretir:

```
Stack.java:8: generic array creation
elements = new E[DEFAULT_INITIAL_CAPACITY];
^
```

Item 28'de açıklandığı gibi, `E` gibi `non-reifiable` bir type'ın array'ini oluşturamazsınız. Bu problem, arkası bir
array ile desteklenen generic type yazdığınız her seferinde ortaya çıkar. Bunu çözmenin iki makul yolu vardır. İlk
çözüm, generic array creation yasağını doğrudan atlatır: Object türünde bir array oluşturup bunu generic array type'ına
cast etmek.

```
public Stack() {
    elements = (E[]) new Object[DEFAULT_INITIAL_CAPACITY];
}
```

Artık bir error yerine, compiler bir warning verecektir. Bu kullanım legal'dir, ancak (genel olarak) typesafe değildir:

```
Stack.java:8: warning: [unchecked] unchecked cast
found: Object[], required: E[]
elements = (E[]) new Object[DEFAULT_INITIAL_CAPACITY];
^
```

Compiler programınızın typesafe olduğunu kanıtlayamayabilir, ancak siz kanıtlayabilirsiniz. Unchecked cast'in programın
type safety'sini tehlikeye atmayacağına kendinizi ikna etmelisiniz. Bahsi geçen array `(elements)` private bir field
içinde saklanır ve hiçbir zaman client'a döndürülmez ya da başka bir method'a aktarılmaz. Array'e depolanan tek
element'ler, type'ı `E` olan push method'una geçirilenlerdir, bu yüzden unchecked cast herhangi bir zarara yol açmaz.
Bir unchecked cast'in safe olduğunu kanıtladıktan sonra, warning'i mümkün olan en dar `(narrow)` scope içinde suppress
edin. Bu case de, constructor yalnızca unchecked array creation işlemini içerdiğinden, warning'i tüm constructor boyunca
suppress etmek uygundur. Bunu yapmak için bir annotation eklenmesiyle, Stack sorunsuz şekilde compile olur ve onu
explicit cast'ler veya `ClassCastException` korkusu olmadan kullanabilirsiniz.

```
// elements array'i yalnızca push(E) ile gelen E instance'larını içerecektir.
/* Bu, type safety'yi sağlamak için yeterlidir, ancak array'in runtime type'ı E[] olmayacaktır; her zaman Object[]
   olacaktır!
*/
@SuppressWarnings("unchecked")
public Stack() {
    elements = (E[]) new Object[DEFAULT_INITIAL_CAPACITY];
}
```

Stack içindeki generic array creation error'ını ortadan kaldırmanın ikinci yolu, elements field'ının type'ını `E[]`'den
`Object[]`'e değiştirmektir.

```
private Object[] elements;
```

Bunu yaparsanız, farklı bir error alırsınız:

```
Stack.java:19: incompatible types
found: Object, required: E
E result = elements[--size];
^
```

Array'den alınan element'i `E`'ye cast ederek bu error'ı bir warning'e çevirebilirsiniz, ancak bir warning alırsınız:

```
public E pop() {
    if (size == 0) throw new EmptyStackException();
    E result = (E) elements[--size];
    elements[size] = null; // Eski referansı eliminate et
    return result;
}
```

Warning;

```
Stack.java:19: warning: [unchecked] unchecked cast
found: Object, required: E
E result = (E) elements[--size];
^
```

`E non-reifiable` bir type olduğu için, compiler cast'i runtime'da kontrol edemez. Yine, unchecked cast'in safe olduğunu
kolayca kanıtlayabilirsiniz, bu yüzden warning'i suppress etmek uygundur. Item 27'nin önerisine uygun olarak, warning'i
yalnızca unchecked cast içeren assignment üzerinde suppress ederiz, tüm pop method'unda değil:

```
// Unchecked warning'in uygun şekilde suppress edilmesi
public E pop() {
    if (size == 0) throw new EmptyStackException();
    
    // push, elements'in type'ının E olmasını gerektirir, bu yüzden cast doğrudur.
    @SuppressWarnings("unchecked")
    E result = (E) elements[--size];
    elements[size] = null; // Eski referansı eliminate et
    return result;
}
```

Generic array creation'ı ortadan kaldırmak için her iki teknik de kendi taraftarlarına sahiptir. Birincisi daha
okunabilir: array `E[]` type'ında declare edilir, bu da içinde sadece `E` instance'ları olduğunu açıkça gösterir. Ayrıca
daha kısa ve özdür: tipik bir generic class'ta, kodun birçok noktasında array'den okuma yapılır; birinci teknik sadece
array'in oluşturulduğu yerde tek bir cast gerektirirken, ikinci teknik her array elemanı okunduğunda ayrı bir cast
gerektirir. Bu nedenle, birinci teknik tercih edilir ve pratikte daha yaygın olarak kullanılır. Ancak, bu durum heap
kirliliğine `(pollution)` neden olur. Array'in runtime type'ı, compile-time type'ı ile uyuşmaz (E Object olmadığı
sürece). Bu durum bazı programcıları rahatsız edecek kadar tedirgin eder ve ikinci tekniği tercih etmelerine yol açar,
ancak bu durumda heap kirliliği `(pollution)` zararsızdır.

Aşağıdaki program, generic Stack class'ımızın kullanımını göstermektedir. Program, command line argümanlarını ters
sırada ve büyük harfe dönüştürülmüş şekilde yazdırır. Stack'ten çıkarılan `(popped)` elementler üzerinde String'in
toUpperCase method'unu invoke etmek için explicit cast gerekmez ve otomatik olarak oluşturulan cast'in başarılı olması
garanti edilir:

```
public static void main(String[] args) {
    Stack<String> stack = new Stack<>();
    for (String arg : args){
        stack.push(arg);
        while(!stack.isEmpty()){
            System.out.println(stack.pop().toUpperCase());
        }
    }
}
```

Önceki örnek, array'ler yerine list'lerin kullanılmasını teşvik eden Item 28 ile çelişiyor gibi görünebilir. Generic
type'larınız içinde list kullanmak her zaman mümkün veya arzu edilen bir şey değildir. Java list'leri doğal olarak
`(natively)` desteklemez, bu yüzden ArrayList gibi bazı generic type'lar array üzerinde implement edilmek zorundadır.
HashMap gibi diğer generic type'lar ise performans için array'ler üzerinde implement edilmiştir.

Generic type'ların büyük çoğunluğu, type parameter'larının herhangi bir kısıtlaması olmadığı Stack örneğimiz gibi olur:
`Stack<Object>`, `Stack<int[]>`, `Stack<List<String>>` veya herhangi başka bir object reference type'ın Stack'ini
oluşturabilirsiniz. Bir primitive type'ın Stack'ini oluşturamazsınız: `Stack<int>` veya `Stack<double>` oluşturmaya
çalışmak compile-time error'a yol açar. Bu, Java'nın generic type sisteminin temel bir sınırlamasıdır. Bu kısıtlamayı,
boxed primitive type'ları kullanarak aşabilirsiniz.

Bazı generic type'lar, type parameter'larının izin verilen değerlerini kısıtlar. Örneğin, declaration'ı şu şekilde olan
`java.util.concurrent.DelayQueue`'yu düşünün:

```
class DelayQueue<E extends Delayed> implements BlockingQueue<E>
```

Type parameter listesi `(<E extends Delayed>)`, actual type parameter `E`'nin `java.util.concurrent.Delayed` subtype'ı
olmasını gerektirir. Bu, DelayQueue implementasyonu ve client'larının `DelayQueue` elementleri üzerinde `Delayed`
method'larından yararlanmasını sağlar; explicit cast yapmaya gerek kalmadan veya ClassCastException riski olmadan. Type
parameter `E`, `bounded type parameter` olarak adlandırılır. Subtype ilişkisi öyle tanımlanmıştır ki, her type kendi
subtype'ıdır `[JLS, 4.10]`, bu nedenle `DelayQueue<Delayed>` oluşturmak legal'dir.

Özetle, generic type'lar, client kodunda cast gerektiren type'lardan daha güvenli ve kullanımı daha kolaydır. Yeni
type'lar tasarladığınızda, bu tür cast'lere gerek kalmadan kullanılabileceklerinden emin olun. Bu genellikle type'ları
generic yapmak anlamına gelir. Eğer mevcutta generic olmayan ama generic olması gereken type'larınız varsa, onları
generic hale getirin. Bu, mevcut client'ları bozmadan bu type'ların yeni kullanıcıları için hayatı kolaylaştıracaktır.