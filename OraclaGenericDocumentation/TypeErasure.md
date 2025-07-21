# Type Erasure

Generics, compile zamanında daha sıkı type kontrolleri sağlamak ve generic programming'i desteklemek için Java diline
eklendi. Generics'i implement etmek için, Java compiler şu öğelere type erasure uygular:

* Generic type'larda ki tüm type parameter'ları, eğer type parameter'lar `bounded` değilse Object ile veya bounded ise
  bound'ları ile değiştirir. Oluşan bytecode bu nedenle sadece sıradan class'lar, interface'ler ve method'lar içerir.

* Type safety'i korumak için gerekirse type cast'ler ekler.

* Extended generic type'larda polymorphism'i korumak için bridge method'lar oluşturur.

Type erasure, parameterized türler için yeni class'lar oluşturulmadığını garanti eder; dolayısıyla, generic'ler runtime
overhead oluşturmaz.