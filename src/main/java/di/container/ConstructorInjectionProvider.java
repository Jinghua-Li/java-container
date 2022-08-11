package di.container;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.stream;

import jakarta.inject.Inject;

public class ConstructorInjectionProvider<T> implements Provider<T> {
    private Constructor<T> constructor;
    private List<Field> fields;

    public ConstructorInjectionProvider(Class<T> component) {
        this.constructor = getConstructor(component);
        this.fields = getFields(component);
    }

    @Override
    public T getT(Context context) {
        try {
            final Object[] dependencies = stream(constructor.getParameters()).map(
                    p -> context.get(p.getType()).get()).toArray();
            final T instance = constructor.newInstance(dependencies);
            for (Field f : fields) {
                f.set(instance, context.get(f.getType()).get());
            }
            return instance;
        } catch (IllegalAccessException | IllegalArgumentException | InstantiationException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Class<?>> getDependencies() {
        return Stream.concat(stream(constructor.getParameters()).map(Parameter::getType),
                fields.stream().map(Field::getType)).collect(Collectors.toList());
    }

    static <T> List<Field> getFields(Class<T> instance) {
        List injectFields = new ArrayList<>();
        Class current = instance;
        while (current != Object.class) {
            injectFields.addAll(
                    stream(current.getDeclaredFields()).filter(f -> f.isAnnotationPresent(Inject.class)).collect(
                            Collectors.toList()));
            current = current.getSuperclass();
        }
        return injectFields;
    }

    static <T, K extends T> Constructor<K> getConstructor(Class<K> instance) {
        final List<Constructor<?>> constructors = stream(instance.getConstructors())
                .filter(c -> c.isAnnotationPresent(Inject.class)).collect(Collectors.toList());
        if (constructors.size() > 1) {
            throw new IllegalComponentException();
        }

        return (Constructor<K>) constructors.stream().findFirst().orElseGet(() -> {
            try {
                return instance.getDeclaredConstructor();
            } catch (NoSuchMethodException e) {
                throw new IllegalComponentException();
            }
        });
    }
}
