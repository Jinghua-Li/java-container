package di.container;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.inject.Inject;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class ContainerTest {
    ContextConfig contextConfig;

    @BeforeEach
    void setUp() {
        contextConfig = new ContextConfig();
    }

    @Nested
    class ComponentConstruction {

        @Test
        void should_bind_type_to_a_specific_instance() {
            final Component instance = new Component() {
            };
            contextConfig.bind(Component.class, instance);

            final Context context = contextConfig.getContext();
            Assert.assertSame(instance, context.get(Component.class).get());
        }

        @Test
        void should_return_empty_if_component_not_bind() {
            Optional<Component> instance = contextConfig.getContext().get(Component.class);
            assertFalse(instance.isPresent());
        }

        @Nested
        public class ConstructorInjection {

            @Test
            void should_bind_type_to_a_class_with_default_construction() {
                contextConfig.bind(Component.class, ComponentImpl.class);

                final Component instance =
                        contextConfig.getContext().get(Component.class).get();

                Assert.assertNotNull(instance);
                Assert.assertTrue(instance instanceof ComponentImpl);
            }

            @Test
            void should_bind_type_to_a_class_with_inject_constructor() {
                contextConfig.bind(Component.class, ComponentWithDependency.class);
                final Dependency dependency = new Dependency() {
                };
                contextConfig.bind(Dependency.class, dependency);

                final Component instance =
                        contextConfig.getContext().get(Component.class).get();
                Assert.assertNotNull(instance);
                Assert.assertSame(dependency, ((ComponentWithDependency) instance).getDependency());
            }

            @Test
            void should_bind_type_to_a_class_with_transitive_dependencies() {
                contextConfig.bind(Component.class, ComponentWithDependency.class);
                contextConfig.bind(Dependency.class, TransitiveDependency.class);
                contextConfig.bind(String.class, "test transitive dependency");

                final Component instance =
                        contextConfig.getContext().get(Component.class).get();
                Assert.assertNotNull(instance);
                final Dependency dependency = ((ComponentWithDependency) instance).getDependency();
                Assert.assertNotNull(dependency);
                Assert.assertSame("test transitive dependency", ((TransitiveDependency) dependency).getDependency());
            }

            @Test
            void should_throw_exception_when_bind_type_a_class_with_multi_constructors() {
                assertThrows(IllegalComponentException.class,
                        () -> contextConfig.bind(Component.class, ComponentWithMultiConstructors.class));
            }

            @Test
            void should_throw_exception_when_bind_type_a_class_with_no_default_constructors() {
                assertThrows(IllegalComponentException.class,
                        () -> contextConfig.bind(Component.class, ComponentWithNoDefaultConstructors.class));
            }

            @Test
            void should_throw_exception_when_dependency_not_found() {
                contextConfig.bind(Component.class, ComponentWithDependency.class);
                final DependencyNotFoundException exception =
                        assertThrows(DependencyNotFoundException.class,
                                () -> contextConfig.getContext());

                assertEquals(Dependency.class, exception.getDependency());
                assertEquals(Component.class, exception.getComponent());
            }

            @Test
            void should_throw_exception_when_cyclic_dependency_found() {
                contextConfig.bind(Component.class, ComponentWithDependency.class);
                contextConfig.bind(Dependency.class, ComponentWithCyclicDependency.class);

                final CyclicDependencyFound exception =
                        assertThrows(CyclicDependencyFound.class, () -> contextConfig.getContext());
                final Set<Class<?>> components = exception.getComponents();
                assertEquals(2, components.size());
                assertTrue(components.contains(Component.class));
                assertTrue(components.contains(Dependency.class));
            }

            @Test
            void should_throw_exception_if_transitive_cyclic_dependencies_found() {
                contextConfig.bind(Component.class, ComponentWithDependency.class);
                contextConfig.bind(Dependency.class, AnotherDependencyOnAnotherDependency.class);
                contextConfig.bind(AnotherDependency.class, AnotherDependencyCyclic.class);

                final CyclicDependencyFound exception =
                        assertThrows(CyclicDependencyFound.class, () -> contextConfig.getContext());

                final Set<Class<?>> components = exception.getComponents();
                assertEquals(3, components.size());
                assertTrue(components.contains(Component.class));
                assertTrue(components.contains(Dependency.class));
                assertTrue(components.contains(AnotherDependency.class));
            }
        }

        @Nested
        public class FieldInjection {
            @Test
            void should_inject_dependency_by_field() {
                final Dependency dependency = new Dependency() {
                };
                contextConfig.bind(Dependency.class, dependency);
                contextConfig.bind(ComponentWithFieldInjection.class, ComponentWithFieldInjection.class);

                final ComponentWithFieldInjection instance =
                        contextConfig.getContext().get(ComponentWithFieldInjection.class).get();

                assertEquals(dependency, instance.getDependency());
            }

            @Test
            void should_inject_dependency_by_parent_field() {
                final Dependency dependency = new Dependency() {
                };
                contextConfig.bind(Dependency.class, dependency);
                contextConfig.bind(SubClassWithFieldInjection.class, SubClassWithFieldInjection.class);

                final ComponentWithFieldInjection instance =
                        contextConfig.getContext().get(SubClassWithFieldInjection.class).get();

                assertEquals(dependency, instance.getDependency());
            }

            @Test
            void should_create_component_with_inject_field() {
                final Context context = Mockito.mock(Context.class);
                final Dependency dependency = Mockito.mock(Dependency.class);
                Mockito.when(context.get(Mockito.eq(Dependency.class))).thenReturn(Optional.of(dependency));

                ConstructorInjectionProvider<ComponentWithFieldInjection> provider =new ConstructorInjectionProvider<>(ComponentWithFieldInjection.class);

                final ComponentWithFieldInjection instance = provider.getT(context);

                assertEquals(dependency, instance.getDependency());
            }

            @Test
            void should_include_field_dependency_with_injection_field() {
                ConstructorInjectionProvider<ComponentWithFieldInjection> provider =new ConstructorInjectionProvider<>(ComponentWithFieldInjection.class);

                assertArrayEquals(new Class<?>[]{Dependency.class}, provider.getDependencies().toArray());
            }
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

class ComponentWithFieldInjection {
    @Inject
    Dependency dependency;

    public Dependency getDependency() {
        return dependency;
    }
}

class SubClassWithFieldInjection extends ComponentWithFieldInjection {}
