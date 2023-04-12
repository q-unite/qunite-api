package com.qunite.api.utils;

import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.repository.JpaRepository;

@UtilityClass
public class JpaRepositoryUtils {
  public static <T, ID> T getById(ID id, JpaRepository<T, ID> jpaRepository) {
    return jpaRepository.findById(id).orElseThrow(AssertionError::new);
  }
}