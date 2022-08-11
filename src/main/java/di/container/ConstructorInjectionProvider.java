package di.container;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;

import jakarta.inject.Inject;

public class ConstructorInjectionProvider<T> implements Provider<T> {
    private Constructor<T> constructor;

    public ConstructorInjectionProvider(Class<T> component) {
        this.constructor = getConstructor(component);
    }

    static <T, K extends T> Constructor<K> getConstructor(Class<K> instance) {
        final List<Constructor<?>> constructors = stream(instance.getConstructors())
                .filter(c -> c.isAnnotationPresent(Inject.class)).collect(Collectors.toList());
        if (constructors.size() > 1) {
            throw new IllegalComponentException();
        }

        return (Constructor<K>) constructors.stream().findFirst().orElseGet(() -> {
            try {
                return instance.getConstructor();
            } catch (NoSuchMethodException e) {
                throw new IllegalComponentException();
            }
        });
    }

    @Override
    public T getT(Context context) {
        try {
            final Object[] dependencies = stream(constructor.getParameters()).map(
                            p -> context.get(p.getType()).get()).toArray();
            return constructor.newInstance(dependencies);
        } catch (IllegalAccessException | IllegalArgumentException | InstantiationException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Class<?>> getDependencies() {
        return stream(constructor.getParameters()).map(Parameter::getType).collect(Collectors.toList());
    }
}
