package com.qunite.api.utils;

import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.repository.JpaRepository;

@UtilityClass
public class JpaRepositoryUtils {
  public static <T> T findEntityById(Long id, JpaRepository<T, Long> jpaRepository) {
    return jpaRepository.findById(id).orElseThrow(AssertionError::new);
  }
}
