package di.container;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CyclicDependencyFound extends RuntimeException {
    private Set<Class<?>> components = new HashSet<>();

    public CyclicDependencyFound(Class<?> component) {
        this.components.add(component);
    }

    public CyclicDependencyFound(Class<?> component, CyclicDependencyFound e) {
        this.components.add(component);
        this.components.addAll(e.getComponents());
    }

    public Set<Class<?>> getComponents() {
        return this.components;
    }
}
