package com.qunite.api.extension;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.PostgreSQLContainer;

public class PostgreSQLExtension implements BeforeAllCallback, AfterAllCallback {

  private static final String IMAGE_TAG = "postgres:15.2-alpine";

  private static final String DATABASE_NAME = "test_db";

  private static final String DATABASE_USERNAME = "test_user";

  private static final String DATABASE_PASSWORD = "test_password";

  private static final Integer[] EXPOSED_PORTS = {5432};

  @Override
  public void beforeAll(ExtensionContext context) {
    PostgreSQLContainer<?> postgres = getConfiguredContainer();
    postgres.start();
    System.setProperty("POSTGRES_URL", postgres.getJdbcUrl());
    System.setProperty("POSTGRES_USER", postgres.getUsername());
    System.setProperty("POSTGRES_PASSWORD", postgres.getPassword());
  }

  private static PostgreSQLContainer<?> getConfiguredContainer() {
    try (PostgreSQLContainer<?> container = new PostgreSQLContainer<>(IMAGE_TAG)) {
      return container
          .withDatabaseName(DATABASE_NAME)
          .withUsername(DATABASE_USERNAME)
          .withPassword(DATABASE_PASSWORD)
          .withExposedPorts(EXPOSED_PORTS);
    }
  }

  @Override
  public void afterAll(ExtensionContext context) {
    // do nothing, Testcontainers handles container shutdown
  }
}

