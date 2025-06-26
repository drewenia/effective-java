import com.google.common.cache.*;

import java.util.concurrent.ConcurrentMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PlayGround {
    public static void main(String[] args) throws InterruptedException {
        CacheLoader<String, String> loader = new CacheLoader<String, String>() {
            @Override
            public String load(String key) throws Exception {
                return key.toUpperCase();
            }
        };

        RemovalListener<String, String> listener = notification -> {
            if (notification.wasEvicted()) {
                String cause = notification.getCause().name();
                assertEquals(RemovalCause.SIZE.toString(), cause);
                System.out.println(cause); // => SIZE
            }
        };

        LoadingCache<String,String> cache = CacheBuilder.newBuilder()
                .maximumSize(3)
                .removalListener(listener)
                .build(loader);

        cache.getUnchecked("first");
        cache.getUnchecked("second");
        cache.getUnchecked("third");
        cache.getUnchecked("last");

        ConcurrentMap<String, String> map = cache.asMap();
        System.out.println(map); // => {second=SECOND, third=THIRD, last=LAST}

        assertEquals(3, cache.size());
    }
}



