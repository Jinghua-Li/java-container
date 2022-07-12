package di.container;

import java.util.HashMap;
import java.util.Map;

public class Context<T> {
    private final Map<Class<T>, T> container = new HashMap<>();

    public void bind(Class<T> componentClass, T instance) {
        container.put(componentClass, instance);
    }

    public T get(Class<T> componentClass) {
        return container.get(componentClass);
    }
}
