package com.qunite.api.testcontainers;

import java.util.Map;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;

public class TestcontainersInitializer implements
    ApplicationContextInitializer<ConfigurableApplicationContext> {
  static PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>(DockerImageName.parse("postgres:15.1"));

  static {
    Startables.deepStart(postgres).join();
  }

  @Override
  public void initialize(ConfigurableApplicationContext ctx) {
    TestPropertyValues.of(Map.of(
        "spring.datasource.url", postgres.getJdbcUrl(),
        "spring.datasource.username", postgres.getUsername(),
        "spring.datasource.password", postgres.getPassword()
    )).applyTo(ctx.getEnvironment());
  }
}