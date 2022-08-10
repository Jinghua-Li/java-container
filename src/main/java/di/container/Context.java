package di.container;

import java.util.Optional;

public interface Context {
    <T> Optional<T> get(Class<T> componentClass);
}
