# Erasure of Generic Types

Type erasure süreci sırasında, Java compiler tüm type parameter'ları siler ve bounded ise her birini first bound'ları
ile, bounded değilse Object ile değiştirir.

Aşağıdaki generic class'ı düşünün, bu class tek yönlü `(singly)` linked list'te bir node'u represent eder:

```java
class Node<T> {
    private T data;
    private Node<T> next;

    public Node(T data, Node<T> next) {
        this.data = data;
        this.next = next;
    }

    public T getData() {
        return data;
    }
}
```

Type parameter `T` unbounded olduğunda, Java compiler bunu `Object` ile değiştirir:

```java
public class Node {

    private Object data;
    private Node next;

    public Node(Object data, Node next) {
        this.data = data;
        this.next = next;
    }

    public Object getData() { return data; }
    // ...
}
```

Aşağıdaki örnekte, generic Node class `bounded type parameter` kullanır:

```java
class Node<T extends Comparable<T>> {
    private T data;
    private Node<T> next;

    public Node(T data, Node<T> next) {
        this.data = data;
        this.next = next;
    }

    public T getData() {
        return data;
    }
}
```

Java compiler bounded type parameter `T`'yi first bound olan `Comparable` ile replace eder:

```java
public class Node {

    private Comparable data;
    private Node next;

    public Node(Comparable data, Node next) {
        this.data = data;
        this.next = next;
    }

    public Comparable getData() { return data; }
    // ...
}
```