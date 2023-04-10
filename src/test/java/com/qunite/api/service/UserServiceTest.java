package com.qunite.api.service;

import com.qunite.api.data.QueueRepository;
import com.qunite.api.data.UserRepository;
import com.qunite.api.domain.User;
import com.qunite.api.extension.PostgreSQLExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@Transactional
@ExtendWith(PostgreSQLExtension.class)
public class UserServiceTest {
    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    QueueRepository queueRepository;

    @Test
    @Sql(value = "/users-create.sql")
    void getUserReturnsActualUser() {
        assertEquals(userService.getUser(1L), userRepository.findById(1L));
    }

    @Test
    void createUserCreatesNewUser() {
        User user = new User();
        user.setFirstName("Creator");
        user.setLastName("Creator");

        userService.createUser(user);

        assertTrue(userRepository.existsById(1L));
        assertEquals(userRepository.findById(user.getId()), Optional.of(user));

    }

    @Test
    @Sql(value = "/users-create.sql")
    void deleteUserDeletesUser() {

        userService.deleteUser(1L);

        assertFalse(userRepository.existsById(1L));

    }

}
