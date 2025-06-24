import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class PlayGround {
    public static void main(String[] args) {
        Duck mallardDuck = new MallardDuck(); // Bir duck create edelim
        Turkey wildTurkey = new WildTurkey(); // Bir turkey create edelim

        /* Sonra turkey’i bir TurkeyAdapter ile wrap ediyoruz, böylece duck gibi görünmesini sağlıyoruz. */
        Duck turkeyAdapter = new TurkeyAdapter(wildTurkey);
        System.out.println("The turkey's says...");
        wildTurkey.gobble();
        wildTurkey.fly();

        System.out.println("\nThe ducks says...");
        test(mallardDuck);

        // Şimdi büyük test: turkey’yi duck gibi göstermeye çalışıyoruz.
        System.out.println("\nTurkey adapter says...");
        test(turkeyAdapter);
    }

    static void test(Duck duck){
        duck.quack();
        duck.fly();
    }
}

interface Duck {
    void quack();

    void fly();
}

interface Turkey {
    void gobble();

    void fly();
}

class MallardDuck implements Duck {
    @Override
    public void quack() {
        System.out.println("Quack");
    }

    @Override
    public void fly() {
        System.out.println("I am flying");
    }
}

class WildTurkey implements Turkey {
    @Override
    public void gobble() {
        System.out.println("Gobble");
    }

    @Override
    public void fly() {
        System.out.println("I am flying a short distance");
    }
}

/* İlk olarak, adapting ettiğin type’ın interface’ini implement etmen gerekiyor. Bu, client’ının görmek istediği
interface’dir. */
class TurkeyAdapter implements Duck {
    Turkey turkey;

    /* Sonra, adapting ettiğimiz object'e referans almamız gerekiyor; burada bunu constructor aracılığıyla yapıyoruz.*/
    public TurkeyAdapter(Turkey turkey) {
        this.turkey = turkey;
    }

    @Override
    public void quack() {
        /* Şimdi interface’deki tüm method’ları implement etmemiz gerekiyor; quack() metodunun translation'ı kolay:
        sadece gobble() metodunu çağır. */
        turkey.gobble();
    }

    @Override
    public void fly() {
        /* Her iki interface de fly() metoduna sahip olmasına rağmen, Turkey’ler kısa aralıklarla uçarlar — Duck'lar
        gibi uzun mesafeler uçamazlar. Duck’ın fly() metodunu Turkey’in fly() metoduna eşlemek için, Turkey’in fly()
        metodunu beş kez çağırmamız gerekir. */
        for (int i = 0; i < 5; i++) {
            turkey.fly();
        }
    }
}