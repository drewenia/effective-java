import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PlayGround {
    public static void main(String[] args) {
        // Typesafe heterogeneous container pattern - client
        Favorites f = new Favorites();

        f.putFavorite(String.class, "Java");
        String stringFavorite = f.getFavorite(String.class);

        f.putFavorite(Integer.class, 0xcafebabe);
        Integer integerFavorite = f.getFavorite(Integer.class);

        f.putFavorite(Class.class, Favorites.class);
        Class<?> classFavorite = f.getFavorite(Class.class);

        System.out.printf("%s %x %s%n", stringFavorite, integerFavorite, classFavorite.getName());
        // => Java cafebabe Favorites
    }


}

// Typesafe heterogeneous container pattern - API
class Favorites {
    private Map<Class<?>, Object> favorites = new HashMap<>();

    public <T> void putFavorite(Class<T> type, T instance) {
        favorites.put(type, type.cast(instance));
    }

    public <T> T getFavorite(Class<T> type) {
        return type.cast(favorites.get(type));
    }
}
