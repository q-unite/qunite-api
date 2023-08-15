package com.qunite.api.data;

import com.qunite.api.domain.TokenPair;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TokenRepository extends JpaRepository<TokenPair, Long> {
  @Query("SELECT count(t) > 0 FROM TokenPair t WHERE t.accessToken = :value"
      + " OR t.refreshToken = :value")
  boolean existsByValue(@Param("value") String value);

  @Modifying
  @Query("delete FROM TokenPair t WHERE t.accessToken = :value OR t.refreshToken = :value")
  void deleteByValue(@Param("value") String value);
}
