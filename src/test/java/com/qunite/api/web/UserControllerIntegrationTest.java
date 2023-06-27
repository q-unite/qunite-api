package com.qunite.api.web;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@IntegrationTest
public class UserControllerIntegrationTest {
  private final String url = "/users";

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
  @Sql("/users-create.sql")
  void retrieveByIdWhenExists() throws Exception {
    var userId = 1L;
    var resultActions =
        mockMvc.perform(get(url + "/" + userId).accept(MediaType.APPLICATION_JSON_VALUE));
    resultActions.andExpect(status().isOk())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(content().json("""
            {"id": 1}"""));
  }

  @Test
  @Sql("/users-create.sql")
  void retrieveAll() throws Exception {
    var size = 7;
    var resultActions = mockMvc.perform(get(url).accept(MediaType.APPLICATION_JSON));
    resultActions.andExpect(status().isOk())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$", hasSize(7)));
  }

  @Test
  @Sql({"/users-create.sql", "/queues-create.sql", "/queues-managers-create.sql"})
  void retrieveManagedQueues() throws Exception {
    var userId = 1;
    var size = 2;
    var resultActions =
        mockMvc.perform(get(url + "/" + userId + "/managed-queues")
            .accept(MediaType.APPLICATION_JSON));

    resultActions.andExpect(status().isOk())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$", hasSize(size)));
  }

  @Test
  @Sql({"/users-create.sql", "/queues-create.sql"})
  void retrieveCreatedQueues() throws Exception {
    var userId = 1;
    var size = 2;
    var resultActions =
        mockMvc.perform(get(url + "/" + userId + "/created-queues")
            .accept(MediaType.APPLICATION_JSON));

    resultActions.andExpect(status().isOk())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$", hasSize(2)));
  }

  @Test
  void createUser() throws Exception {
    var user = new User();
    user.setFirstName("SomeUser");
    var dto = userMapper.toDto(user);
    var json = new ObjectMapper().writeValueAsString(dto);

    var resultActions =
        mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json));
    resultActions.andExpect(status().isCreated())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.firstName", is("SomeUser")));
  }

  @Test
  @Sql("/users-create.sql")
  void updateUser() throws Exception {
    final var user = new User();
    user.setFirstName("John");
    final var dto = userMapper.toDto(user);
    final var json = new ObjectMapper().writeValueAsString(dto);
    var userId = 1;

    var resultActions =
        mockMvc.perform(
            patch(url + "/" + userId).contentType(MediaType.APPLICATION_JSON).content(json));
    resultActions.andExpect(status().isOk())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.firstName", is("John")));

  }

  @Test
  @Sql("/users-create.sql")
  void deleteQueue() throws Exception {
    var userId = 1;
    mockMvc.perform(delete(url + "/" + userId))
        .andExpect(status().isNoContent());
  }

}
