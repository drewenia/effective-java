# Java Generics Tutorial

Java Generics, Java 5'te tanıtılan ve daha fazla type safety ve yeniden kullanılabilirlik ile kod yazmanıza olanak
tanıyan güçlü bir özelliktir. Generics'ten önce, ArrayList gibi collection'larla çalışmak, onlara herhangi bir type'da
object saklayabileceğin anlamına geliyordu. Bu, genellikle object'leri yanlış type'a cast ederken ve geri alırken
runtime hatalarına yol açıyordu. Generics, bir collection'ın veya class'ın tutabileceği object'lerin type'ını
compile-time'da belirtmeni sağlayarak bu problemi çözer.

## Java Generics'in Temel Kavramları

### Type Parameters

Type parameter'lar gerçek `(actual)` type'lar için yer tutuculardır `(placeholder)`. Bunlar açılı parantezler `(<>)`
içinde belirtilir. Örneğin, generic bir class olan `Box<T>`'de `T` type parameter'dır. Box class'ını kullanırken
Integer, String veya custom bir class type'ı gibi geçerli herhangi bir Java type'ı ile değiştirilebilir.

### Generic Classes

Generic class, bir veya daha fazla type parameter'a sahip olan class'tır. Herhangi bir type'dan object tutabilen basit
bir Box class'ı örneği şudur:

```
class Box<T> {
    private T item;

    public T getItem() {
        return item;
    }

    public void setItem(T item) {
        this.item = item;
    }
}
```

Box class'ını şöyle kullanabilirsin;

```
public static void main(String[] args) {
    Box<Integer> integerBox = new Box<>();
    integerBox.setItem(42);
    Integer item = integerBox.getItem();;
    System.out.println(item);
}
```

### Generic Methods

Generic method, ait olduğu class'tan bağımsız olarak kendi type parameter'larına sahip olan method'dur. Type
parameter'lar return type'ından önce belirtilir.

```
static <T> void printArray(T[] array){
    for (T element : array){
        System.out.println(element);
    }
    System.out.println();
}
```

printArray method'unu şu şekilde call edebilirsin:

```
public static void main(String[] args) {
    String[] strArray = new String[]{"Ocean","Foo","Bar"};
    printArray(strArray); // => Ocean, Foo, Bar
}
```

## Java Generics'in Kullanım Yöntemleri

### Using Generics with Collections

Java'daki ArrayList, HashMap gibi collection'lar genellikle type safety'yi sağlamak için generics ile kullanılır.

```
List<String> list = new ArrayList<>();
list.add("Hello");
list.add("World");

// Eğer String olmayan bir object eklemeye çalışırsan, bu compile olmaz.
// list.add(10);
```

### Creating Custom Generic Classes

Kendi generic class'larını oluşturarak kodunu daha reusable yapabilirsin. Örneğin, generic bir linked list:

```
class Node<T>{
    T data;
    Node<T> next;

    public Node(T data){
        this.data = data;
        this.next = null;
    }
}

class LinkedList<T>{
    private Node<T> head;

    public void add (T data){
        Node<T> newNode = new Node<>(data);
        if (head == null){
            head = newNode;
        } else {
            Node<T> current = head;
            while(current.next != null){
                current = current.next;
            }
            current.next = newNode;
        }
    }

    public void printList(){
        Node<T> current = head;
        while(current!=null){
            System.out.println(current.data);
            current = current.next;
        }
        System.out.println();
    }
}
```

Derived;

```
LinkedList<Integer> intList = new LinkedList<>();
intList.add(1);
intList.add(2);
intList.add(3);
intList.add(4);
intList.printList();
```

### Implementing Generic Methods

Yukarıdaki Utils class örneğinde gösterildiği gibi, generic method'lar farklı türdeki data üzerinde type-safe ve
reusable şekilde operation'lar yapmak için kullanılabilir. Generic method'da birden fazla type parameter da
kullanabilirsin.

```
public static <T extends Number> double average(T[] array) {
    double sum = 0;
    for (T number : array) {
        sum+= number.doubleValue();
    }
    return sum / array.length;
}
```

Derived;

```
public static void main(String[] args) {
    Double[] doubleArray = {1.0, 2.0, 3.0, 4.0};
    double average = average(doubleArray);
    System.out.println(average); // => 2.5
}
```

## Java Generics'te Yaygın Kullanım Yöntemleri

### Bounded Type Parameters

Bazen type parameter'ı belirli bir type kümesiyle sınırlandırmak istersin. Bu, bounded type parameter'lar kullanılarak
yapılır. Örneğin, generic bir method'un sadece Number class'ını extend eden type'larla çalışmasını istersen:

```
public static <T extends Number> T max(T a, T b) {
    return a.doubleValue() > b.doubleValue() ? a : b;
}
```

Derived;

```
public static void main(String[] args) {
    float maxFloat = max(5f, 10f);
    int maxInt = max(12,11);
    double maxDouble = max(1.4D,2.8D);

    System.out.println(maxFloat); // => 10.0
    System.out.println(maxInt); // => 12
    System.out.println(maxDouble); // => 2.8
}
```

### Wildcards

Wildcard'lar bilinmeyen bir type'ı represent etmek için kullanılır. İki ana wildcard türü vardır:

`upper-bound wildcard'lar (? extends)` ve `lower-bound wildcard'lar (? super)`.

* Upper-bound wildcard, bir collection'dan data read etmek istediğinde kullanılır. Örneğin:

```
class Scratch {
    public static void main(String[] args) {
        List<Dog> dogList = new ArrayList<>();
        dogList.add(new Dog());
        printAnimals(dogList);
    }

    static void printAnimals(List<? extends Animal> list) {
        for (Animal animal : list) {
            System.out.println(animal);
        }
    }
}

class Animal {
}

class Dog extends Animal {
}
```

* Lower-bound wildcard, bir collection'a data write etmek istediğinde kullanılır. Örneğin:

```
class Scratch {
    public static void main(String[] args) {
        List<Fruit> fruitList = new ArrayList<>();
        addApples(fruitList);
        System.out.println(fruitList); // => [Apple@1b28cdfa]
    }

    public static void addApples(List<? super Apple> list){
        list.add(new Apple());
    }
}

class Fruit {
}

class Apple extends Fruit {
}
```

## Best Practices in Java Generics

### Unchecked Warning'leri minimuma indirin

Legacy code'ları kullanırken veya bazı complex generic senaryolarda `unchecked` uyarılarla karşılaşabilirsin. Bu
uyarıları ortadan kaldırmak için kodu yeniden yazmaya çalış. Örneğin, raw type (type parameter belirtilmemiş bir type)
kullanıyorsan, bu `unchecked` uyarılara yol açabilir. Raw type'ları uygun generic type'larla değiştir.

### Upper ve Lower Bound'ları Akıllıca Kullan

Upper-bound ve lower-bound wildcard'lar arasındaki farkı anla ve bunları uygun şekilde kullan. Upper-bound wildcard'lar
bir collection'dan data read etmen gerektiğinde kullanışlıdır, lower-bound wildcard'lar ise bir collection'a data write
etmen gerektiğinde kullanışlıdır.

### Generic Type'ları Raw Type'lara Tercih Et

Collection'larla veya custom generic class'larla çalışırken her zaman generic type'ları kullan. Raw type'lar esas olarak
geriye dönük uyumluluk için sağlanır ve runtime'da type-safety problemlerine yol açabilir.

# Sonuç

Java Generics, Java kodunun type safety'sini ve yeniden kullanılabilirliğini artıran önemli bir özelliktir. Type
parameter'lar, generic class'lar ve method'lar gibi temel kavramları anlayarak ve bunları collection'lar ve custom
class'lar gibi çeşitli senaryolarda nasıl kullanacağını öğrenerek daha sağlam ve sürdürülebilir kod yazabilirsin.
Unchecked uyarılarını en aza indirmek ve bound ile wildcard'ları doğru kullanmak gibi best practice'leri takip etmek,
Java uygulamalarının kalitesini daha da artıracaktır.