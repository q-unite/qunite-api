package com.qunite.api.web;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qunite.api.annotation.IntegrationTest;
import com.qunite.api.data.EntryRepository;
import com.qunite.api.data.QueueRepository;
import com.qunite.api.data.UserRepository;
import com.qunite.api.service.UserService;
import com.qunite.api.web.dto.user.UserDto;
import com.qunite.api.web.mapper.UserMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@IntegrationTest
@WithMockUser("First")
class UserControllerIntegrationTest {
  private final String url = "users";

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private UserMapper userMapper;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private QueueRepository queueRepository;

  @Autowired
  private EntryRepository entryRepository;

  @Autowired
  private UserService userService;

  @AfterEach
  public void cleanAll() {
    userRepository.deleteAll();
    entryRepository.deleteAll();
    queueRepository.deleteAll();
  }


  @Test
  @Sql("/users-create.sql")
  void retrieveByIdWhenExists() throws Exception {
    var userId = 1;

    var resultActions =
        mockMvc.perform(get("/{url}/{id}", url, userId));

    resultActions.andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(userId)));
  }

  @Test
  @Sql("/users-create.sql")
  void retrieveAll() throws Exception {
    var resultActions = mockMvc.perform(get("/{url}", url));

    resultActions.andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(7)));
  }

  @Test
  @Sql({"/users-create.sql", "/queues-create.sql", "/queues-managers-create.sql"})
  void retrieveManagedQueues() throws Exception {
    var userId = 1;

    var resultActions =
        mockMvc.perform(get("/{url}/{id}/managed-queues", url, userId));

    resultActions.andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)));
  }

  @Test
  @Sql({"/users-create.sql", "/queues-create.sql"})
  void retrieveCreatedQueues() throws Exception {
    var userId = 1;

    var resultActions =
        mockMvc.perform(get("/{url}/{id}/created-queues", url, userId));

    resultActions.andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)));
  }

  @Test
  @Sql("/users-create.sql")
  void updateUser() throws Exception {
    final var user = new UserDto();
    user.setUsername("John");

    final var json = new ObjectMapper().writeValueAsString(user);

    var resultActions =
        mockMvc.perform(patch("/{url}/self", url)
            .contentType(MediaType.APPLICATION_JSON).content(json));

    resultActions.andExpect(status().isOk())
        .andExpect(jsonPath("$.username", is("John")));
  }

  @Test
  @Sql("/users-create.sql")
  void deleteUser() throws Exception {
    var userId = 1L;

    mockMvc.perform(delete("/{url}/self", url))
        .andExpect(status().isNoContent());
    assertThat(userService.findOne(userId)).isEmpty();
  }
}