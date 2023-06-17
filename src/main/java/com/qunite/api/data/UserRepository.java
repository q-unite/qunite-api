package com.qunite.api.data;

import com.qunite.api.domain.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

Optional<User> findByUsernameOrEmail(String username, String email);
}