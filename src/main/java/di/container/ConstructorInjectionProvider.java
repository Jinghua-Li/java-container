package di.container;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static java.util.Arrays.stream;

import jakarta.inject.Provider;

public class ConstructorInjectionProvider<T> implements Provider<T> {
    private final Context context;
    private Constructor<T> constructor;
    private Class<?> component;
    private boolean constructing = false;

    public ConstructorInjectionProvider(Context context, Constructor<T> constructor, Class<?> component) {
        this.context = context;
        this.constructor = constructor;
        this.component = component;
    }

    @Override
    public T get() {
        if (constructing) {
            throw new CyclicDenpendencyFound();
        }

        try {
            constructing = true;
            final Object[] dependencies = stream(constructor.getParameters()).map(
                            p -> context.get(p.getType()).orElseThrow(() ->
                                    new DependencyNotFoundException(p.getType(), component)))
                    .toArray();
            return constructor.newInstance(dependencies);
        } catch (IllegalAccessException | IllegalArgumentException | InstantiationException | InvocationTargetException e) {
            throw new RuntimeException(e);
        } finally {
            constructing = false;
        }
    }
}
