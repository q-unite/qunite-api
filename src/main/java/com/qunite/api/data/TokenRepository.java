package com.qunite.api.data;

import com.qunite.api.domain.Tokens;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TokenRepository extends JpaRepository<Tokens, Long> {
  @Query("SELECT count(t) > 0 FROM Tokens t WHERE t.accessToken = :value"
      + " OR t.refreshToken = :value")
  boolean existsByValue(@Param("value") String value);

  @Query("delete FROM Tokens t WHERE t.accessToken = :value OR t.refreshToken = :value")
  void deleteByValue(@Param("value") String value);
}
