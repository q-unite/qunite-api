package com.qunite.api.service;

import com.qunite.api.data.UserRepository;
import com.qunite.api.domain.User;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private UserRepository userRepository;

    @Override
    public void createUser(User user) {
        userRepository.save(user);
    }

    @Override
    public Optional<User> getUser(Long id) {
        return Optional.ofNullable(id).flatMap(userRepository::findById);
    }

    @Override
    public void deleteUser(Long id) {
        Optional.ofNullable(id).ifPresent(userRepository::deleteById);
    }

}
