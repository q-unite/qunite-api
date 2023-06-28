package com.qunite.api.testcontainers;

import lombok.Data;
import org.testcontainers.containers.PostgreSQLContainer;

@Data
public class PostgreSQLConfig<C extends PostgreSQLContainer<C>> implements TestcontainersConfig<C> {
  public static final String NAME = "postgresql";
  private String dockerImage;

  public C forContainer() {
    @SuppressWarnings("unchecked")
    C container = (C) new PostgreSQLContainer<>(dockerImage);
    return container;
  }
}
