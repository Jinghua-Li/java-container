package di.container;

import java.util.HashMap;
import java.util.Map;

import jakarta.inject.Provider;

public class Context {
    private final Map<Class<?>, Provider<?>> container = new HashMap<>();

    public <T> void bind(Class<T> componentClass, T instance) {
        container.put(componentClass, () -> instance);
    }

    public <T, K extends T> void bind(Class<T> componentClass, Class<K> instance) {
        container.put(componentClass, () -> {
            try {
                return instance.getConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public <T> T get(Class<T> componentClass) {
        return (T) container.get(componentClass).get();
    }
}
