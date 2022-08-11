package di.container;

import java.util.List;

public interface Provider<T> {
    T getT(Context context);

    List<Class<?>> getDependencies();
}
