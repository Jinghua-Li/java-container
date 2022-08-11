package di.container;

import java.util.*;

public class CyclicDependencyFound extends RuntimeException {
    private Set<Class<?>> components = new HashSet<>();

    public CyclicDependencyFound(Stack<Class<?>> visiting) {
        components.addAll(visiting);
    }

    public Set<Class<?>> getComponents() {
        return this.components;
    }
}
