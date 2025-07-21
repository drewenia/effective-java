# Erasure of Generic Methods

Java compiler generic method argümanlarındaki type parameter'ları da siler. Aşağıdaki generic method'u düşünün:

```java
// elem'in anArray içindeki tekrar sayısını sayar.
public static <T> int count(T[] anArray,T elem){
    int count = 0;
    for (T e : anArray){
        if (e.equals(elem))
            ++count;
    }
    return count;
}
```

`T` unbounded olduğu için, Java compiler bunu `Object` ile replace eder:

```java
public static int count(Object[] anArray, Object elem) {
    int count = 0;
    for (Object e : anArray)
        if (e.equals(elem))
            ++count;
    return count;
}
```

Aşağıdaki class'ların define edildiğini varsayın:

```java
class Shape { /* ... */ }
class Circle extends Shape { /* ... */ }
class Rectangle extends Shape { /* ... */ }
```

Farklı Shape'ler çizmek için generic bir method yazabilirsiniz:

```java
public static <T extends Shape> void draw(T shape) { /* ... */ }
```

Java compiler `T`'yi Shape ile replace eder:

```java
public static void draw(Shape shape) { /* ... */ }
```