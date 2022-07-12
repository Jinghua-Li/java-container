package di.container;

import org.junit.Assert;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class ContainerTest {

    interface Component {

    }

    static class ComponentImpl implements Component {
        public ComponentImpl() {
        }
    }

    @Nested
    class ComponentConstruction {

        @Test
        void should_bind_type_to_a_specific_instance() {
            Context context = new Context();
            final Component instance = new Component() {
            };
            context.bind(Component.class, instance);

            Assert.assertSame(instance, context.get(Component.class));
        }

        @Nested
        public class ConstructorInjection {

            @Test
            void should_bind_type_to_a_class_with_default_construction() {
                Context context = new Context();

                context.bind(Component.class, ComponentImpl.class);

                final Component instance = context.get(Component.class);

                Assert.assertNotNull(instance);
                Assert.assertTrue(instance instanceof ComponentImpl);
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
