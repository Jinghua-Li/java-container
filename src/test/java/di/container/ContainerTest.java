package di.container;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import jakarta.inject.Inject;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class ContainerTest {
    Context context;

    @BeforeEach
    void setUp() {
        context = new Context();
    }

    @Nested
    class ComponentConstruction {

        @Test
        void should_bind_type_to_a_specific_instance() {
            final Component instance = new Component() {
            };
            context.bind(Component.class, instance);

            Assert.assertSame(instance, context.get(Component.class).get());
        }

        @Test
        void should_return_empty_if_component_not_bind() {
            Optional<Component> instance = context.get(Component.class);
            assertFalse(instance.isPresent());
        }

        @Nested
        public class ConstructorInjection {

            @Test
            void should_bind_type_to_a_class_with_default_construction() {
                context.bind(Component.class, ComponentImpl.class);

                final Component instance =
                        context.get(Component.class).get();

                Assert.assertNotNull(instance);
                Assert.assertTrue(instance instanceof ComponentImpl);
            }

            @Test
            void should_bind_type_to_a_class_with_inject_constructor() {
                context.bind(Component.class, ComponentWithDependency.class);
                final Dependency dependency = new Dependency() {
                };
                context.bind(Dependency.class, dependency);

                final Component instance =
                        context.get(Component.class).get();
                Assert.assertNotNull(instance);
                Assert.assertSame(dependency, ((ComponentWithDependency) instance).getDependency());
            }

            @Test
            void should_bind_type_to_a_class_with_transitive_dependencies() {
                context.bind(Component.class, ComponentWithDependency.class);
                context.bind(Dependency.class, TransitiveDependency.class);
                context.bind(String.class, "test transitive dependency");

                final Component instance =
                        context.get(Component.class).get();
                Assert.assertNotNull(instance);
                final Dependency dependency = ((ComponentWithDependency) instance).getDependency();
                Assert.assertNotNull(dependency);
                Assert.assertSame("test transitive dependency", ((TransitiveDependency) dependency).getDependency());
            }

            @Test
            void should_throw_exception_when_bind_type_a_class_with_multi_constructors() {
                assertThrows(IllegalComponentException.class,
                        () -> context.bind(Component.class, ComponentWithMultiConstructors.class));
            }

            @Test
            void should_throw_exception_when_bind_type_a_class_with_no_default_constructors() {
                assertThrows(IllegalComponentException.class,
                        () -> context.bind(Component.class, ComponentWithNoDefaultConstructors.class));
            }

            @Test
            void should_throw_exception_when_dependency_not_found() {
                context.bind(Component.class, ComponentWithDependency.class);
                assertThrows(DependencyNotFoundException.class,
                        () -> context.get(Component.class));
            }
        }

        @Nested
        public class FieldInjection {

        }

        @Nested
        public class MethodInjection {

        }
    }

    @Nested
    public class DependenciesSelection {

    }

    @Nested
    public class LifeCycleManagement {

    }
}

interface Component {

}

interface Dependency {

}

class ComponentImpl implements Component {
    public ComponentImpl() {
    }
}

class ComponentWithDependency implements Component {
    private Dependency dependency;

    @Inject
    public ComponentWithDependency(Dependency dependency) {
        this.dependency = dependency;
    }

    public Dependency getDependency() {
        return dependency;
    }
}

class TransitiveDependency implements Dependency {
    private String dependency;

    @Inject
    public TransitiveDependency(String dependency) {
        this.dependency = dependency;
    }

    public String getDependency() {
        return dependency;
    }
}

class ComponentWithMultiConstructors implements Component {
    @Inject
    public ComponentWithMultiConstructors(String str, Double price) {
    }

    @Inject
    public ComponentWithMultiConstructors(String str) {
    }
}

class ComponentWithNoDefaultConstructors implements Component {
    public ComponentWithNoDefaultConstructors(String str) {
    }
}
