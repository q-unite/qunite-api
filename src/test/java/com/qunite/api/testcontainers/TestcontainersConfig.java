package com.qunite.api.testcontainers;

import org.testcontainers.containers.Container;

public interface TestcontainersConfig<T extends Container<T>> {
  T forContainer();
}
