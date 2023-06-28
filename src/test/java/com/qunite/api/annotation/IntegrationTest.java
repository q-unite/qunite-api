package com.qunite.api.annotation;

import com.qunite.api.testcontainers.TestcontainersInitializer;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.Tag;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Tag("integration")
@ActiveProfiles("test")
@ContextConfiguration(initializers = TestcontainersInitializer.class)
public @interface IntegrationTest {
}
