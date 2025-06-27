# Methods Common to All Objects

Object concrete bir class olmasına rağmen, esas olarak genişletilmek `(extension)` üzere tasarlanmıştır. `Nonfinal`
tüm method'ları `(equals, hashCode, toString, clone ve finalize)`, override edilmek üzere tasarlandıkları için explicit
genel `(general)` sözleşmelere `(contracts)` sahiptir. Bu method'ları override eden herhangi bir class'ın, bu general
contract'lara uyması gerekir. Bunu yapmamak `(contract'lara uymamak)`, bu contract'lara bağlı olan diğer class'ların
(örneğin `HashMap` ve `HashSet` gibi) söz konusu class ile birlikte düzgün şekilde çalışmasını engeller.

Bu bölüm, nonfinal `Object` method'larının ne zaman ve nasıl override edileceğini açıklar. `finalize` methodu bu
bölümden çıkarılmıştır çünkü Item 8'de ele alınmıştır. Bir `Object` metodu olmamakla birlikte, `Comparable.compareTo`
metodu benzer bir niteliğe sahip olduğu için bu bölümde ele alınmıştır.




