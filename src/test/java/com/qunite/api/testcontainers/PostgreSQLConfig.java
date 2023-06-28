package com.qunite.api.testcontainers;

import lombok.Data;
import org.testcontainers.containers.PostgreSQLContainer;

@Data
public class PostgreSQLConfig<T extends PostgreSQLContainer<T>> implements TestcontainersConfig<T> {
  public static final String NAME = "postgresql";
  private String dockerImage;

  public T forContainer() {
    @SuppressWarnings("unchecked")
    T container = (T) new PostgreSQLContainer<>(dockerImage);
    return container;
  }
}
