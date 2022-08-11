package di.container;

import java.util.*;

public class ContextConfig {
    private final Map<Class<?>, Provider<?>> providers = new HashMap<>();

    public <T> void bind(Class<T> componentClass, T instance) {
        providers.put(componentClass, new Provider<T>() {
            @Override
            public T getT(Context context) {
                return instance;
            }

            @Override
            public List<Class<?>> getDependencies() {
                return Collections.emptyList();
            }
        });
    }

    public <T, K extends T> void bind(Class<T> componentClass, Class<K> instance) {
        providers.put(componentClass, new ConstructorInjectionProvider<>(instance));
    }

    public Context getContext() {
        providers.keySet().forEach(component -> checkDependencies(component, new Stack()));

        return new Context() {
            @Override
            public <T> Optional<T> get(Class<T> componentClass) {
                return Optional.ofNullable(providers.get(componentClass)).map(instance -> (T) instance.getT(this));
            }
        };
    }

    private void checkDependencies(Class<?> component, Stack<Class<?>> visiting) {
        providers.get(component).getDependencies().forEach(dependency -> {
            if (!providers.containsKey(dependency)) {
                throw new DependencyNotFoundException(dependency, component);
            }
            if (visiting.contains(dependency)) {
                throw new CyclicDependencyFound(visiting);
            }

            visiting.push(dependency);
            checkDependencies(dependency, visiting);
            visiting.pop();
        });
    }
}
