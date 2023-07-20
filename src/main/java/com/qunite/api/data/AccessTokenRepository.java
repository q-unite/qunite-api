package com.qunite.api.data;

import com.qunite.api.domain.AccessToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccessTokenRepository extends JpaRepository<AccessToken, Long> {
  Optional<AccessToken> findByValue(String value);
}
