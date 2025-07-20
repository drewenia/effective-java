# Why use generics

Özetle, generic'ler, type'ların (class ve interface'lerin) class, interface ve method tanımlarken parametre olarak
kullanılmasını sağlar. Method declaration'larında kullanılan daha aşina olduğumuz formal parameter'lar gibi, type
parameter'lar da aynı kodu farklı input'larla yeniden kullanmanın bir yolunu sağlar. Fark şudur ki, formal
parameter'ların input'ları value'lar iken, type parameter'ların input'ları type'lardır.

Generics kullanan kodun, generic olmayan koda göre birçok avantajı vardır:

* Compile time'da daha güçlü type kontrolleri. Bir Java compiler, generic koda güçlü type kontrolü uygular ve kod type
  safety'yi ihlal ederse hata verir. Compile-time hatalarını düzeltmek, bulunması zor olabilen runtime hatalarını
  düzeltmekten daha kolaydır.

* Cast işlemlerinin ortadan kaldırılması. Generics olmadan aşağıdaki kod parçası cast işlemi gerektirir:

```java
public static void main(String[] args) {
    List list = new ArrayList();
    list.add("Hello");
    String str = (String) list.get(0);
}
```

Generics kullanılarak yeniden yazıldığında, kod cast işlemi gerektirmez:

```java
public static void main(String[] args) {
    List<String> list = new ArrayList<>();
    list.add("Hello");
    String str = list.get(0); // no cast
}
```

* Programcıların generic algoritmalar implement etmesini mümkün kılar. Generics kullanarak, programcılar farklı
  type'larda koleksiyonlar üzerinde çalışan, özelleştirilebilir, type safe ve okunması daha kolay generic algoritmalar
  implement edebilir.