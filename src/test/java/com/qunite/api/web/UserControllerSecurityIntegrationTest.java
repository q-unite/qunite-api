package com.qunite.api.web;

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
import com.qunite.api.domain.User;
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
class UserControllerSecurityIntegrationTest {
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

  @AfterEach
  public void cleanAll() {
    userRepository.deleteAll();
    entryRepository.deleteAll();
    queueRepository.deleteAll();
  }

  @Test
  void unauthenticatedUserShouldDoNothing() throws Exception {
    mockMvc.perform(get("/{url}", url))
        .andExpect(status().isForbidden());
    mockMvc.perform(get("/{url}/1", url))
        .andExpect(status().isForbidden());
    mockMvc.perform(get("/{url}/1/created-queues", url))
        .andExpect(status().isForbidden());
    mockMvc.perform(get("/{url}/self", url))
        .andExpect(status().isForbidden());
    final var user = new User();
    user.setUsername("NewUsername");
    final var dto = userMapper.toDto(user);
    final var json = new ObjectMapper().writeValueAsString(dto);

    mockMvc.perform(patch("/{url}/self", url).contentType(MediaType.APPLICATION_JSON).content(json))
        .andExpect(status().isForbidden());
    mockMvc.perform(delete("/{url}/self", url))
        .andExpect(status().isForbidden());
    mockMvc.perform(get("/{url}/1/managed-queues", url))
        .andExpect(status().isForbidden());
  }


  @Test
  @WithMockUser("Seventh")
  @Sql({"/users-create.sql"})
  void anyAuthenticatedUserShouldRetrieveAllUsers() throws Exception {
    mockMvc.perform(get("/{url}", url))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser("Seventh")
  @Sql({"/users-create.sql"})
  void anyAuthenticatedUserShouldRetrieveUserById() throws Exception {
    mockMvc.perform(get("/{url}/1", url))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser("Seventh")
  @Sql({"/users-create.sql", "/queues-create.sql", "/queues-managers-create.sql"})
  void anyAuthenticatedUserShouldRetrieveManagedQueuesByUserId() throws Exception {
    mockMvc.perform(get("/{url}/1/managed-queues", url))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser("Seventh")
  @Sql({"/users-create.sql", "/queues-create.sql"})
  void anyAuthenticatedUserShouldRetrieveCreatedQueuesByUserId() throws Exception {
    mockMvc.perform(get("/{url}/1/created-queues", url))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser("Seventh")
  @Sql({"/users-create.sql"})
  void userShouldRetrieveHimself() throws Exception {
    mockMvc.perform(get("/{url}/self", url))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.username", is("Seventh")));
  }

  @Test
  @WithMockUser("Seventh")
  @Sql({"/users-create.sql"})
  void userShouldUpdateHimself() throws Exception {
    final var user = new User();
    user.setUsername("NewUsername");

    final var dto = userMapper.toDto(user);
    final var json = new ObjectMapper().writeValueAsString(dto);

    mockMvc.perform(patch("/{url}/self", url).contentType(MediaType.APPLICATION_JSON).content(json))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser("Seventh")
  @Sql({"/users-create.sql"})
  void userShouldDeleteHimself() throws Exception {
    mockMvc.perform(delete("/{url}/self", url))
        .andExpect(status().isNoContent());
  }
}
