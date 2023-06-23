package com.qunite.api.data;

import com.qunite.api.domain.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  @Query("SELECT u FROM User u WHERE u.email = :data OR u.username = :data")
  Optional<User> findByEmailOrUsername(@Param("data") String loginData);

  boolean existsByUsernameOrEmail(String username, String email);


}