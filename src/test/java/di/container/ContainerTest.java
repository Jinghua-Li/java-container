package di.container;

import org.junit.Assert;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class ContainerTest {

    interface Component {

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
