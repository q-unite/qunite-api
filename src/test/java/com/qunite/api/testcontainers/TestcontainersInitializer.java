package com.qunite.api.testcontainers;

import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.lifecycle.Startables;

public class TestcontainersInitializer implements
    ApplicationContextInitializer<ConfigurableApplicationContext> {
  private static final String PREFIX = "testcontainers.";
  protected static PostgreSQLContainer<?> postgres;

  private static <T> T getConfig(ConfigurableApplicationContext ctx, String name, Class<T> clazz) {
    var env = ctx.getEnvironment();
    return Binder.get(env).bind(PREFIX + name, clazz).get();
  }

  @Override
  public void initialize(ConfigurableApplicationContext ctx) {
    synchronized (TestcontainersInitializer.class) {
      if (postgres == null) {
        var postgreSQLConfig = getConfig(ctx, PostgreSQLConfig.NAME, PostgreSQLConfig.class);
        postgres = postgreSQLConfig.forContainer();
        Startables.deepStart(postgres).join();
      }
    }
    TestPropertyValues.of(
        "spring.datasource.url=" + postgres.getJdbcUrl(),
        "spring.datasource.username=" + postgres.getUsername(),
        "spring.datasource.password=" + postgres.getPassword()
    ).applyTo(ctx.getEnvironment());
  }
}