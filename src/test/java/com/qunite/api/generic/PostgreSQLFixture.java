package com.qunite.api.generic;

import com.qunite.api.annotation.IntegrationTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

@DirtiesContext
@IntegrationTest
public interface PostgreSQLFixture {
  PostgreSQLContainer<?> POSTGRE_SQL_CONTAINER = start();

  private static PostgreSQLContainer<?> start() {
    final var postgres = new PostgreSQLContainer<>("postgres:15.2-alpine")
        .withDatabaseName("test")
        .withUsername("postgres")
        .withPassword("postgres")
        .withReuse(true);
    postgres.start();
    return postgres;
  }

  @DynamicPropertySource
  static void postgresqlProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.jpa.properties.hibernate.dialect",
        () -> "org.hibernate.dialect.PostgreSQLDialect");
    registry.add("spring.datasource.driver-class-name",
        () -> "org.postgresql.Driver");
    registry.add("spring.datasource.url", POSTGRE_SQL_CONTAINER::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRE_SQL_CONTAINER::getUsername);
    registry.add("spring.datasource.password", POSTGRE_SQL_CONTAINER::getPassword);
  }
}

