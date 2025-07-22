# Effective Java

### [Chapter 1 - Introduction](Chapter_1/Introduction.md)

### [Chapter 2 - Creating and Destroying Objects](Chapter_2/CreatingAndDestoyingObject.md)

[Item 1](Chapter_2/Items/Item1.md) (Consider static factory methods instead of constructors)

    * EnumSet Explanation

    * Service Provider Interface (SPI) in Java Explanation

[Item 2](Chapter_2/Items/Item2.md) (Consider a builder when faced with many constructor parameters)

[Item 3](Chapter_2/Items/Item3.md) (Enforce the singleton property with a private constructor or an enum type)

[Item 4](Chapter_2/Items/Item4.md) (Enforce noninstantiability with a private constructor)

[Item 5](Chapter_2/Items/Item5.md) (Prefer dependency injection to hardwiring resources)

[Item 6](Chapter_2/Items/Item6.md) (Avoid creating unnecessary objects)

    * String Literals Explanation

    * Adapter Design Pattern Explanation

[Item 7](Chapter_2/Items/Item7.md) (Eliminate obsolete object references)

    * WeakHashMap Explanatiton

    * Strong, Soft, and Weak References Explanation

    * LinkedHashMap removeEldestEntry() Method Explanation

    * Guava Cache Explanation

[Item 8](Chapter_2/Items/Item8.md) (Avoid finalizers and cleaners)

[Item 9](Chapter_2/Items/Item9.md) (Prefer try-with-resources to try-finally)

### [Chapter 3 - Methods Common to All Objects](Chapter_3/MethodsCommonToAllObjects.md)

[Item 10](Chapter_3/Items/Item10.md) (Obey the general contract when overriding equals)

    * Value based classes Explanation

    * What Is a Monitor in Computer Science?

        * Guide to the Synchronized Keyword in Java

        * wait() and notify() methods in Java

    * [JLS, 15.20.2] Type Comparison Operator instanceof

    * [JLS 15.21.1] Numerical Equality Operators == and !=

    * Float.equals method examples

    * Google AutoValue

[Item 11](Chapter_3/Items/Item11.md) (Always override hashCode when you override equals)

[Item 12](Chapter_3/Items/Item12.md) (Always override toString)

[Item 13](Chapter_3/Items/Item13.md) (Override clone judiciously)

[Item 14](Chapter_3/Items/Item14.md) (Consider implementing Comparable)

### [Chapter 4 - Classes and Interfaces](Chapter_4/ClassesAndInterfaces.md)

[Item 15](Chapter_4/Items/Item15.md) (Minimize the accessibility of classes and members)

[Item 16](Chapter_4/Items/Item16.md) (In public classes, use accessor methods, not public fields)

[Item 17](Chapter_4/Items/Item17.md) (Minimize mutability)

    * [JLS 17.5] final Field Semantics

[Item 18](Chapter_4/Items/Item18.md) (Favor composition over inheritance)

    * Guava Forwarding Class

        * PeekingIterator

        * AbstractIterator

[Item 19](Chapter_4/Items/Item19.md) (Design and document for inheritance or else prohibit it)

[Item 20](Chapter_4/Items/Item20.md) (Prefer interfaces to abstract classes)

    * [JLS 9.4.3] Interface Method Body

    * Template Method Pattern Example

    * Skeletal class implementation

    * Java simulated multiple inheritance example

    * AbstractMap.SimpleEntry Explanation

    * AbstractMap.SimpleImmutableEntry Explanation

[Item 21](Chapter_4/Items/Item21.md) (Design interfaces for posterity)

    * Java Collections.synchronizedCollection
    
    * Java Collections.synchronizedCollection Multi-threaded Access
    
    * Java Collections.synchronizedCollection Compound Operations (check-then-act pattern)

    * Iterating with Synchronization

    * Synchronized Collection vs Concurrent Collections (CopyOnWriteArrayList)

    * Synchronized Collection with Custom Objects

    * Synchronized Collection Performance Considerations

[Item 22](Chapter_4/Items/Item22.md) (Use interfaces only to define types)

[Item 23](Chapter_4/Items/Item23.md) (Prefer class hierarchies to tagged classes)

[Item 24](Chapter_4/Items/Item24.md) (Favor static member classes over nonstatic)

[Item 25](Chapter_4/Items/Item25.md) (Limit source files to a single top-level class)

### [Chapter 5 - Generics](Chapter_5/Generics.md)

[Item 26](Chapter_5/Items/Item26.md) (Don’t use raw types)

[Item 27](Chapter_5/Items/Item27.md) (Eliminate unchecked warnings)

[Item 28](Chapter_5/Items/Item28.md) (Prefer lists to arrays)

[Item 29](Chapter_5/Items/Item29.md) (Favor generic types)

[Item 30](Chapter_5/Items/Item30.md) (Favor generic methods)

    * Type erasure in java explained

[Item 31](Chapter_5/Items/Item31.md) (Use bounded wildcards to increase API flexibility)

    * Get and Put principle

[Item 32](Chapter_5/Items/Item32.md) (Combine generics and varargs judiciously)

[Item 33](Chapter_5/Items/Item33.md) (Consider typesafe heterogeneous containers)

    * Type Token