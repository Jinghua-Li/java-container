package di.container;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

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
                final DependencyNotFoundException exception =
                        assertThrows(DependencyNotFoundException.class, () -> context.get(Component.class));

                assertEquals(Dependency.class, exception.getDependency());
                assertEquals(Component.class, exception.getComponent());
            }

            @Test
            void should_throw_exception_when_cyclic_dependency_found() {
                context.bind(Component.class, ComponentWithDependency.class);
                context.bind(Dependency.class, ComponentWithCyclicDependency.class);

                final CyclicDependencyFound exception =
                        assertThrows(CyclicDependencyFound.class, () -> context.get(Dependency.class));
                final Set<Class<?>> components = exception.getComponents();
                assertEquals(2, components.size());
                assertTrue(components.contains(Component.class));
                assertTrue(components.contains(Dependency.class));
            }

            @Test
            void should_throw_exception_if_transitive_cyclic_dependencies_found() {
                context.bind(Component.class, ComponentWithDependency.class);
                context.bind(Dependency.class, AnotherDependencyOnAnotherDependency.class);
                context.bind(AnotherDependency.class, AnotherDependencyCyclic.class);

                final CyclicDependencyFound exception =
                        assertThrows(CyclicDependencyFound.class, () -> context.get(Dependency.class));

                final Set<Class<?>> components = exception.getComponents();
                assertEquals(3, components.size());
                assertTrue(components.contains(Component.class));
                assertTrue(components.contains(Dependency.class));
                assertTrue(components.contains(AnotherDependency.class));
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

interface AnotherDependency {

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

class ComponentWithCyclicDependency implements Dependency {
    private Component component;

    @Inject
    public ComponentWithCyclicDependency(Component component) {
        this.component = component;
    }
}

class AnotherDependencyCyclic implements AnotherDependency {
    private Component component;

    @Inject
    public AnotherDependencyCyclic(Component component) {
        this.component = component;
    }
}

class AnotherDependencyOnAnotherDependency implements Dependency {
    private AnotherDependency anotherDependency;

    @Inject
    public AnotherDependencyOnAnotherDependency(AnotherDependency anotherDependency) {
        this.anotherDependency = anotherDependency;
    }
}
