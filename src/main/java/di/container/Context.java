package di.container;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.stream;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

public class Context {
    private final Map<Class<?>, Provider<?>> container = new HashMap<>();

    public <T> void bind(Class<T> componentClass, T instance) {
        container.put(componentClass, () -> instance);
    }

    public <T, K extends T> void bind(Class<T> componentClass, Class<K> instance) {
        final long size = stream(instance.getConstructors())
                .filter(c -> c.isAnnotationPresent(Inject.class)).count();
        if (size > 1) {
            throw new IllegalCopmonentException();
        }
        if (size == 0 && stream(instance.getConstructors()).filter(c -> c.getParameters().length == 0).findFirst()
                .map(c -> false).orElse(true)) {
            throw new IllegalCopmonentException();
        }
        container.put(componentClass, () -> {
            try {
                final Constructor<K> constructor = getConstructor(instance);
                final Object[] dependencies = stream(constructor.getParameters()).map(p -> get(p.getType()))
                        .toArray();
                return constructor.newInstance(dependencies);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private <T, K extends T> Constructor<K> getConstructor(Class<K> instance) {
        return (Constructor<K>) stream(instance.getConstructors())
                .filter(c -> c.isAnnotationPresent(Inject.class))
                .findFirst().orElseGet(() -> {
                    try {
                        return instance.getConstructor();
                    } catch (NoSuchMethodException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    public <T> T get(Class<T> componentClass) {
        return (T) container.get(componentClass).get();
    }
}
