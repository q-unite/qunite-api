package com.qunite.api.utils;

import com.qunite.api.data.EntryRepository;
import com.qunite.api.domain.Entry;
import com.qunite.api.domain.EntryId;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.repository.JpaRepository;

@UtilityClass
public class JpaRepositoryUtils {
  public static <T> T findEntityById(Long id, JpaRepository<T, Long> jpaRepository) {
    return jpaRepository.findById(id).orElseThrow(AssertionError::new);
  }

  public static Entry findEntityById(EntryId entryId, EntryRepository entryRepository) {
    return entryRepository.findById(entryId).orElseThrow(AssertionError::new);
  }
}