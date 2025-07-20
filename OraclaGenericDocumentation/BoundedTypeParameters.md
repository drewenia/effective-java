# Bounded Type Parameters

Bazı durumlarda, parameterized type içinde type argument olarak kullanılabilecek type'ları kısıtlamak isteyebilirsiniz.
Örneğin, sayılar üzerinde çalışan bir method yalnızca `Number` veya onun subclass'larının instance'larını kabul etmek
isteyebilir. Bounded type parameter'lar bunun için vardır.

Bounded type parameter define etmek için, type parameter adını yazın, ardından `extends` keyword'ünü ve son olarak upper
bound olarak kullanılacak type'ı — bu örnekte `Number` — yazın. Bu context'de, `extends` ifadesinin hem "extends"
(class'lar için) hem de "implements" (interface'ler için) anlamında genel bir şekilde kullanıldığını unutmayın.

```
class Box<T> {
    private T t;

    public T get() {
        return t;
    }

    public void set(T t) {
        this.t = t;
    }

    public <U extends Number> void inspect(U u) {
        System.out.println("T : " + t.getClass().getName());
        System.out.println("U : " + u.getClass().getName());
    }

    public static void main(String[] args) {
        Box<Integer> integerBox = new Box<>();
        integerBox.set(10);
        integerBox.inspect("hello"); // hata : bu hala String
    }
}
```

Generic method'umuz (`inspect(U u)`) bounded type parameter içerecek şekilde değiştirildiğinde, artık compile başarısız
olur; çünkü inspect invocation'ı hâlâ bir String içermektedir:

```
Box.java:21: <U>inspect(U) in Box<java.lang.Integer> cannot
  be applied to (java.lang.String)
                        integerBox.inspect("10");
                                  ^
1 error
```

Generic bir type'ı instantiate ederken kullanabileceğiniz type'ları sınırlandırmanın yanı sıra, bounded type
parameter'lar bound içinde tanımlanmış method'ları çağırmanıza da olanak tanır.

```
class NaturalNumber<T extends Integer>{
    private T n;
 
    public NaturalNumber(T n){
        this.n = n;
    }
    
    public boolean isEven(){
        return n.intValue() % 2 == 0;
    }
    // ...
}
```

isEven method'u, `n` üzerinden `Integer` class içinde tanımlı olan `intValue` method'unu invoke eder.

## Multiple Bounds

Önceki örnek, tek bir bound'a sahip type parameter kullanımını göstermektedir; ancak bir type parameter birden fazla
bound'a sahip olabilir:

```
<T extends B1 & B2 & B3>
```

Birden fazla bound'a sahip type variable, bound içinde listelenen tüm type'ların subtype'ıdır. Bound'lardan biri class
ise, önce o belirtilmelidir. Örneğin:

```
Class A { /* ... */ }
interface B { /* ... */ }
interface C { /* ... */ }

class D <T extends A & B & C> { /* ... */ }
```

Bound A ilk belirtilmezse, compile-time hatası alırsınız:

```
class D <T extends B & A & C> { /* ... */ }  // compile-time error
```