import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class PlayGround {
    public static void main(String[] args) {
        NYPizza nyPizza = new NYPizza.Builder(NYPizza.Size.SMALL)
                .addTopping(Pizza.Topping.SAUCAGE)
                .addTopping(Pizza.Topping.ONION)
                .build();

        Calzone calzonePizza = new Calzone.Builder()
                .addTopping(Pizza.Topping.HAM)
                .sauceInside()
                .build();

    }
}

// Builder pattern for class hierarchies
abstract class Pizza {
    enum Topping {
        HAM,
        MUSHROOM,
        ONION,
        PEPPER,
        SAUCAGE
    }

    private final EnumSet<Topping> toppings;

    abstract static class Builder<T extends Builder<T>> {
        EnumSet<Topping> toppings = EnumSet.noneOf(Topping.class);

        public T addTopping(Topping topping) {
            toppings.add(Objects.requireNonNull(topping));
            return self();
        }

        abstract Pizza build();

        protected abstract T self();
    }

    Pizza(Builder<?> builder) {
        this.toppings = builder.toppings.clone();
    }
}

class NYPizza extends Pizza {
    enum Size {
        SMALL,
        MEDIUM,
        LARGE
    }

    private final Size size;

    public static class Builder extends Pizza.Builder<Builder> {
        private final Size size;

        public Builder(Size size) {
            this.size = Objects.requireNonNull(size);
        }

        @Override
        public NYPizza build() {
            return new NYPizza(this);
        }

        @Override
        protected Builder self() {
            return this;
        }

    }

    private NYPizza(Builder builder) {
        super(builder);
        size = builder.size;
    }
}

class Calzone extends Pizza {
    private final boolean sauceInside;

    public static class Builder extends Pizza.Builder<Builder> {
        private boolean sauceInside = false; // default

        public Builder sauceInside() {
            sauceInside = true;
            return this;
        }

        @Override
        Calzone build() {
            return new Calzone(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }

    private Calzone(Builder builder) {
        super(builder);
        sauceInside = builder.sauceInside;
    }
}






