package com.qunite.api.service;

import com.qunite.api.domain.User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface UserService {
    void createUser(User user);

    Optional<User> getUser(Long id);

    void deleteUser(Long id);

}
