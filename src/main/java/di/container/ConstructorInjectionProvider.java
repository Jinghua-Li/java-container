package di.container;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static java.util.Arrays.stream;

public class ConstructorInjectionProvider<T> implements Provider<T> {
    private Constructor<T> constructor;
    private Class<?> component;
    private boolean constructing = false;

    public ConstructorInjectionProvider(Constructor<T> constructor, Class<?> component) {
        this.constructor = constructor;
        this.component = component;
    }

    @Override
    public T getT(Context context) {
        if (constructing) {
            throw new CyclicDependencyFound(component);
        }

        try {
            constructing = true;
            final Object[] dependencies = stream(constructor.getParameters()).map(
                            p -> context.get(p.getType()).orElseThrow(() ->
                                    new DependencyNotFoundException(p.getType(), component)))
                    .toArray();
            return constructor.newInstance(dependencies);
        } catch (CyclicDependencyFound e) {
            throw new CyclicDependencyFound(component, e);
        } catch (IllegalAccessException | IllegalArgumentException | InstantiationException | InvocationTargetException e) {
            throw new RuntimeException(e);
        } finally {
            constructing = false;
        }
    }
}
