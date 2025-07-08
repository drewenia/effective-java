# Generics

Java 5’ten beri generics dilin bir parçasıdır. Generics’ten önce, bir collection’dan okunan her object'i cast etmek
zorundaydınız. Birisi yanlışlıkla hatalı türde bir object eklerse, cast işlemleri runtime’da başarısız olabilirdi.
Generics ile, compiler'a her collection’da hangi türde object'lerin kabul edildiğini söylersiniz. Compiler sizin için
otomatik olarak cast işlemleri ekler ve yanlış türde bir object eklemeye çalışırsanız compile time'da sizi uyarır.
Bu, hem daha güvenli hem de daha anlaşılır programlar ortaya çıkarır; ancak bu avantajlar, sadece collection’larla
sınırlı olmamakla birlikte, bazı bedellerle gelir. Bu bölüm, avantajları en üst düzeye çıkarmayı ve karmaşıklıkları en
aza indirmeyi anlatır.