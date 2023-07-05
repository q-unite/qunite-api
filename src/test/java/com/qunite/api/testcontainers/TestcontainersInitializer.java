package com.qunite.api.testcontainers;

import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.lifecycle.Startables;

public class TestcontainersInitializer implements
    ApplicationContextInitializer<ConfigurableApplicationContext> {
  private static final String PREFIX = "testcontainers.";

  private static <T> T getConfig(ConfigurableApplicationContext ctx, String name, Class<T> target) {
    var env = ctx.getEnvironment();
    return Binder.get(env).bind(PREFIX + name, target).get();
  }

  @Override
  public void initialize(ConfigurableApplicationContext ctx) {
    var postgres = getConfig(ctx, PostgreSQLConfig.NAME, PostgreSQLConfig.class).forContainer();
    Startables.deepStart(postgres).join();

    TestPropertyValues.of(
        "spring.datasource.url=" + postgres.getJdbcUrl(),
        "spring.datasource.username=" + postgres.getUsername(),
        "spring.datasource.password=" + postgres.getPassword()
    ).applyTo(ctx.getEnvironment());
  }
}