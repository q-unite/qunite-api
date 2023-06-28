package com.qunite.api.testcontainers;

import java.util.Map;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.lifecycle.Startables;

public class TestcontainersInitializer implements
    ApplicationContextInitializer<ConfigurableApplicationContext> {
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

  static {
    Startables.deepStart(postgres).join();
  }

  @Override
  public void initialize(ConfigurableApplicationContext ctx) {
    TestPropertyValues.of(Map.ofEntries(
        Map.entry("spring.datasource.url", postgres.getJdbcUrl()),
        Map.entry("spring.datasource.username", postgres.getUsername()),
        Map.entry("spring.datasource.password", postgres.getPassword())
    )).applyTo(ctx.getEnvironment());
  }
}