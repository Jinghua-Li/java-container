package di.container;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

public class Context {
    private final Map<Class<?>, Provider<?>> container = new HashMap<>();

    public <T> void bind(Class<T> componentClass, T instance) {
        container.put(componentClass, () -> instance);
    }

    public <T, K extends T> void bind(Class<T> componentClass, Class<K> instance) {
        final Constructor<K> constructor = getConstructor(instance);
        container.put(componentClass, () -> {
            try {
                final Object[] dependencies = stream(constructor.getParameters()).map(p -> get(p.getType()))
                        .toArray();
                return constructor.newInstance(dependencies);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private <T, K extends T> Constructor<K> getConstructor(Class<K> instance) {
        final List<Constructor<?>> constructors = stream(instance.getConstructors())
                .filter(c -> c.isAnnotationPresent(Inject.class)).collect(Collectors.toList());
        if (constructors.size() > 1) {
            throw new IllegalCopmonentException();
        }

        return (Constructor<K>) constructors.stream().findFirst().orElseGet(() -> {
                    try {
                        return instance.getConstructor();
                    } catch (NoSuchMethodException e) {
                        throw new IllegalCopmonentException();
                    }
                });
    }

    public <T> T get(Class<T> componentClass) {
        return (T) container.get(componentClass).get();
    }
}
