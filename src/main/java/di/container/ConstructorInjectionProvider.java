package di.container;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static java.util.Arrays.stream;

public class ConstructorInjectionProvider<T> implements Provider<T> {
    private Constructor<T> constructor;

    public ConstructorInjectionProvider(Constructor<T> constructor) {
        this.constructor = constructor;
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
}
