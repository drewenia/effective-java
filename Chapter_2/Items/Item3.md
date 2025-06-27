# Enforce the singleton property with a private constructor or an enum type

# Singleton propertysini private constructor veya enum type ile sağlayın.

Singleton, yalnızca bir kez instantiate edilen bir class’tır. Singletonlar genellikle stateless bir object'i, örneğin
bir fonksiyonu, ya da doğası gereği unique olan bir sistem component'ini represent eder. Bir class’ı singleton yapmak,
eğer bir interface implement etmiyorsa, onun yerine mock implementation koymayı imkânsız kıldığı için client’larının
test edilmesini zorlaştırabilir.

Singleton implement etmek için iki yaygın yöntem vardır. Her ikisi de constructor’ı private tutmaya ve tek instance’a
erişim sağlamak için public static bir `member` sağlamaya dayanır. Bir yöntem, bu `member`'ın `final` bir `field`
olmasıdır:

```
// Singleton with public final field
class Elvis {
    
    public static final Elvis INSTANCE = new Elvis();

    private Elvis() {
        ...
    }

    public void leaveTheBuilding() {
        ...
    }
}
```

Private constructor yalnızca bir kez call edilir ve `public static final field Elvis.INSTANCE`’i initialize eder. Public
veya protected constructor olmaması “monoelvistic” bir evren garantiler: Elvis class’ı initialize edildiğinde tam olarak
bir Elvis instance’ı var olacaktır — ne fazlası ne eksigi. Client’in yapabilecekleri bunu değiştiremez, ancak bir
istisna vardır: ayrıcalıklı `(privileged)` bir client, `AccessibleObject.setAccessible` method’u yardımıyla private
constructor’ı reflective olarak invoke edebilir. Bu saldırıya karşı koruma gerekirse, constructor’ı ikinci bir instance
oluşturulmaya çalışıldığında exception fırlatacak şekilde değiştirin.

```
// Singleton with static factory
class Elvis {

    private static final Elvis INSTANCE = new Elvis();

    private Elvis() {
        ...
    }

    public static Elvis getInstance() {
        return INSTANCE;
    }

    public void leaveTheBuilding() {
        ...
    }
}
```

`Elvis.getInstance` call'larının tümü aynı object referansını döner ve (önceki istisna geçerli olmakla birlikte) başka
hiçbir `Elvis` instance’ı oluşturulmaz.

Public field yaklaşımının başlıca avantajı, API’nin class’ın bir singleton olduğunu açıkça göstermesidir: public static
field final’dır, dolayısıyla her zaman aynı object referansını içerir. İkinci avantajı ise daha sade olmasıdır.

Static factory yaklaşımının bir avantajı da, class’ın singleton olup olmayacağına dair fikrinizi API’yi değiştirmeden
sonradan değiştirme esnekliği sunmasıdır. Factory method tek `(sole)` instance’ı döner, ancak örneğin, her invoke eden
thread için ayrı bir instance dönecek şekilde değiştirilebilir. İkinci bir avantaj ise, uygulamanızın ihtiyaç duyması
hâlinde `generic` bir singleton factory yazabilmenizdir. Static factory kullanmanın son bir avantajı da, method
referansının bir supplier olarak kullanılabilmesidir; örneğin, `Elvis::instance` bir `Supplier<Elvis>` olarak
kullanılabilir. Bu avantajlardan biri sizin için geçerli değilse, `public field` yaklaşımı tercih edilmelidir.

Bu yaklaşımlardan birini kullanan bir singleton class’ı serializable (bkz. Chapter 12) yapmak için, sadece
`implements Serializable` eklemek yeterli değildir. Singleton garantisini korumak için, tüm instance field’ları
`transient` olarak tanımlayın ve bir `readResolve` method’u sağlayın. Aksi takdirde, serialized bir instance her
deserialized edildiğinde yeni bir instance oluşturulur ve bu da örneğimizde olduğu gibi sahte Elvis görünümlerine yol
açar. Bunun olmasını önlemek için Elvis class’ına şu `readResolve` method’unu ekleyin:

```
// Singleton property korumak için readResolve methodu
private Object readResolve(){
    // Tek true Elvis’i döndürür ve garbage collector'ın eski object'i temizlemesine izin verir.
    return INSTANCE;
}
```

Singleton implement etmenin üçüncü yolu, single-element bir enum tanımlamaktır:

```
public class PlayGround {
    public static void main(String[] args) {
        Elvis elvis = Elvis.INSTANCE;
        elvis.leaveTheBuilding();
    }
}

enum Elvis {
    INSTANCE;

    public void leaveTheBuilding(){
        System.out.println("Im outta here!");
    }
}
```

Bu yaklaşım public field yaklaşımına benzer, ancak daha özlüdür, serialization mekanizmasını otomatik sağlar ve
sophisticated serialization veya reflection saldırılarına karşı bile çok güçlü bir multiple instantiation engelini
garanti eder. Bu yaklaşım biraz yapay gelebilir, ancak single-element bir enum type genellikle singleton
implementasyonunun `en iyi` yoludur. Dikkat edin, singleton’ınız Enum dışındaki bir superclass’ı extend etmek zorundaysa
bu yaklaşımı kullanamazsınız (ancak enum’un interface implement etmesi mümkündür).