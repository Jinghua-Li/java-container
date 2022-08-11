package di.container;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;

import jakarta.inject.Inject;

public class ContextConfig {
    private final Map<Class<?>, Provider<?>> container = new HashMap<>();
    private final Map<Class<?>, List<Class<?>>> dependencies = new HashMap<>();

    public <T> void bind(Class<T> componentClass, T instance) {
        container.put(componentClass, context -> instance);
        dependencies.put(componentClass, Collections.emptyList());
    }

    public <T, K extends T> void bind(Class<T> componentClass, Class<K> instance) {
        final Constructor<K> constructor = getConstructor(instance);
        container.put(componentClass, new ConstructorInjectionProvider<>(constructor));
        dependencies.put(componentClass,
                stream(constructor.getParameters()).map(Parameter::getType).collect(Collectors.toList()));
    }

    private <T, K extends T> Constructor<K> getConstructor(Class<K> instance) {
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

    public Context getContext() {
        dependencies.keySet().forEach(component -> checkDependencies(component, new Stack()));

        return new Context() {
            @Override
            public <T> Optional<T> get(Class<T> componentClass) {
                return Optional.ofNullable(container.get(componentClass)).map(instance -> (T) instance.getT(this));
            }
        };
    }

    private void checkDependencies(Class<?> component, Stack<Class<?>> visiting) {
        dependencies.get(component).forEach(dependency -> {
            if (!dependencies.containsKey(dependency)) {
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
