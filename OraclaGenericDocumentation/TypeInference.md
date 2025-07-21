# Type Inference

Type inference, bir Java compiler’ın her method invocation’ına ve ilgili declaration’a bakarak invocation’ı geçerli
kılan type argümanını (veya argümanlarını) belirleme yeteneğidir. Inference algoritması, argümanların type'larını ve
eğer mevcutsa, result'ın assign edildiği veya return edildiği type'ı belirler. Son olarak, inference algoritması tüm
argümanlarla uyumlu olan en specific type’ı bulmaya çalışır.

Bu son noktayı göstermek için, aşağıdaki örnekte inference, pick method’una geçirilen ikinci argümanın `Serializable`
türünde olduğunu belirler:

```
static <T> T pick(T a1, T a2) { 
    return a2; 
}

Serializable s = pick("d", new ArrayList<String>());
```

## Type Inference and Generic Methods

Generic Method'lar, sizi type inference ile tanıştırdı; bu sayede bir generic method’u, köşeli parantezler arasında bir
type belirtmeden, sıradan bir method gibi invoke edebilirsiniz.

Aşağıdaki örnek olan `BoxDemo`’yu düşünün; bu örnek `Box` class’ını gerektirir:

Box.java;

```java
class Box<T> {
    private T t;

    public T get() {
        return t;
    }

    public void set(T t) {
        this.t = t;
    }
}
```

BoxDemo.java;

```java
class BoxDemo {
    public static <U> void addBox(U u, List<Box<U>> boxes) {
        Box<U> box = new Box<>();
        box.set(u);
        boxes.add(box);
    }

    public static <U> void outBoxes(List<Box<U>> boxes) {
        int counter = 0;
        for (Box<U> box : boxes) {
            U boxContents = box.get();
            System.out.println("Box #" + counter + " contains [" + boxContents.toString() + "]");
            counter++;
        }
    }

    public static void main(String[] args) {
        List<Box<Integer>> listOfIntegerBoxes = new ArrayList<>();
        addBox(10, listOfIntegerBoxes);
        addBox(20, listOfIntegerBoxes);
        addBox(30, listOfIntegerBoxes);
        outBoxes(listOfIntegerBoxes);
    }
}
```

Output;

```
Box #0 contains [10]
Box #1 contains [20]
Box #2 contains [30]
```

Generic method `addBox`, `U` adında bir type parametresi tanımlar. Genel olarak, bir Java compiler bir generic method
call'unun type parametrelerini infer edebilir. Sonuç olarak, çoğu case'de bunları belirtmek zorunda değilsiniz.
Örneğin, generic method olan addBox’ı invoke etmek için, type parametresini bir `type witness` ile aşağıdaki gibi
belirtebilirsiniz:

```
BoxDemo.<Integer>addBox(10), listOfIntegerBoxes);
```

Alternatif olarak, eğer `type witness`’i atlayacak olursanız, bir Java compiler (method’un argümanlarından) type
parametresinin Integer olduğunu otomatik olarak infer eder:

```
addBox(20, listOfIntegerBoxes);
```

## Type Inference and Instantiation of Generic Classes

Generic bir class’ın constructor’ını invoke etmek için gereken type argümanlarını, compiler context'den type
argümanlarını infer edebildiği sürece, boş bir type parametre kümesi `<>` ile değiştirebilirsiniz. Bu çift köşeli
parantez gayri resmi olarak `diamond` olarak adlandırılır.

Örneğin, aşağıdaki variable declaration'ı düşünün:

```
Map<String, List<String>> myMap = new HashMap<String, List<String>>();
```

Constructor’ın parameterized türünü boş bir type parametre kümesi `<>` ile değiştirebilirsiniz:

```
Map<String, List<String>> myMap = new HashMap<>();
```

Generic class instantiation sırasında type inference’den yararlanmak için diamond kullanmanız gerektiğini unutmayın.
Aşağıdaki örnekte, compiler `unchecked conversion` uyarısı üretir çünkü `HashMap()` constructor’ı,
`Map<String, List<String>>` type'ı yerine `HashMap raw type`’ına atıfta bulunur:

```
Map<String, List<String>> myMap = new HashMap(); // unchecked conversion warning
```

## Type Inference and Generic Constructors of Generic and Non-Generic Classes

Constructor’ların, hem generic hem de non-generic class’larda generic olabileceğini (başka bir deyişle, kendi formal
type parametrelerini declare edebileceğini) unutmayın. Aşağıdaki örneği düşünün:

```
class MyClass<X> {
  <T> MyClass(T t) {
    // ...
  }
}
```

MyClass class’ının aşağıdaki instantiation'ınını düşünün:

```
new MyClass<Integer>("")
```

Bu statement, parameterized türü `MyClass<Integer>` olan bir instance oluşturur; bu statement, generic class
`MyClass<X>`’in formal type parametresi `X` için açıkça `Integer` türünü belirtir. Bu generic class’ın constructor’ının
formal type parametresi `T` içerdiğini unutmayın. Compiler, bu generic class’ın constructor’ının formal type parametresi
`T` için tür olarak String’i infer eder (çünkü bu constructor’ın actual parametresi bir String object'idir).

Java SE 7 öncesi sürümlerin compiler’ları, generic method’lara benzer şekilde generic constructor’ların actual type
parametrelerini infer edebilir. Ancak, Java SE 7 ve sonrasındaki compiler’lar, diamond `<>` kullanırsanız,
instantiated generic class’ın actual type parametrelerini infer edebilir. Aşağıdaki örneği düşünün:

```
MyClass<Integer> myObject = new MyClass<>("");
```

Bu örnekte, compiler, generic class `MyClass<X>`’in formal type parametresi `X` için type olarak Integer’ı infer eder.
Ayrıca, bu generic class’ın constructor’ının formal type parametresi `T` için tür olarak String’i infer eder.

* Note : Inference algoritmasının type'ları infer etmek için yalnızca invocation argümanlarını, target type'ları ve
  muhtemelen belirgin bir beklenen return türünü kullandığını belirtmek önemlidir. Inference algoritması, programın
  ilerleyen kısımlarındaki result'ları kullanmaz.

## Target Types

Java compiler, bir generic method invocation’ının type parametrelerini infer etmek için target typing’den yararlanır.
Bir expression'ın target type’ı, expression'ın yer aldığı konuma bağlı olarak Java compiler’ın beklediği data type’tır.
Aşağıdaki gibi declare edilen `Collections.emptyList` method’unu düşünün:

```
static <T> List<T> emptyList();
```

Aşağıdaki assignment statement'ını düşünün:

```
List<String> listOne = Collections.emptyList();
```

Bu statement, `List<String>` instance'ını beklemektedir; bu data type, target type’tır. Çünkü method `emptyList()`,
`List<T>` type’ında bir değer döndürür, Compiler type argument `T`’nin değerinin String olması gerektiğini infer eder.
Bu, hem Java SE 7 hem de 8’de çalışır. Alternatif olarak, bir `type witness` kullanabilir ve `T`’nin değerini şöyle
belirtebilirsiniz:

```
List<String> listOne = Collections.<String>emptyList();
```

Ancak, bu context'de gerekli değildir. Diğer context'lerde ise gerekliydi. Aşağıdaki method’u dikkate alın:

```
void processStringList(List<String> stringList) {
    // process stringList
}
```

Diyelim ki method `processStringList`’i empty bir liste ile invoke etmek istiyorsunuz. Java SE 7’de, aşağıdaki statement
compile olmaz:

```
processStringList(Collections.emptyList());
```

Java SE 7 compiler, aşağıdakine benzer bir error message üretir:

```
List<Object> cannot be converted to List<String>
```

Compiler, type argument `T` için bir değer ister, bu yüzden Object değeri ile başlar. Sonuç olarak,
Collections.emptyList’in invocation’ı `List<Object>` type’ında bir değer döndürür, bu da `processStringList` method’u
ile uyumsuzdur. Böylece, Java SE 7’de type argument’in değeri şu şekilde belirtilmelidir:

```
processStringList(Collections.<String>emptyList());
```

Bu, Java SE 8’de artık gerekli değildir. Target type kavramı, `processStringList` method’unun argümanı gibi method
argümanlarını da kapsayacak şekilde genişletilmiştir. Bu durumda, `processStringList`, `List<String>` type’ında bir
argüman gerektirir. `Collections.emptyList` method’u `List<T>` type’ında bir değer döndürür, bu yüzden `List<String>`
target type’ını kullanarak Compiler, type argument `T`’nin değerinin `String` olduğunu infer eder. Böylece, Java SE 8’de
aşağıdaki statement compile olur:

```
processStringList(Collections.emptyList());
```