# Introduction

BU kitap,Java programlama dili ve temel kütüphaneleri — `java.lang`, `java.util` ve `java.io` ile `java.util.concurrent`
ve `java.util.function` gibi subpackage'ler — etkili bir şekilde kullanmanıza yardımcı olmak için tasarlanmıştır. Diğer
kütüphaneler de yeri geldikçe ele alınmaktadır.

Bu kitaptaki kuralların çoğu, birkaç temel prensibe dayanmaktadır. Açıklık ve sadelik son derece önemlidir. Bir
component’in kullanıcısı, onun behavior'u karşısında asla şaşırmamalıdır. Component’ler mümkün olduğunca küçük olmalı,
ancak bundan daha küçük olmamalıdır. (Bu kitapta "component" terimi, bireysel bir method'dan birden fazla package içeren
karmaşık bir framework'e kadar herhangi bir reusable yazılım öğesini ifade eder.) Kod, kopyalanmak yerine yeniden
kullanılmalıdır. Component’ler arasındaki bağımlılıklar minimumda tutulmalıdır. Hatalar, yapıldıkları anda, ideal olarak
compile time da, mümkün olan en erken aşamada tespit edilmelidir.

