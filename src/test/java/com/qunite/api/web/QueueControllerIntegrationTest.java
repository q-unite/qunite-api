package com.qunite.api.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qunite.api.annotation.IntegrationTest;
import com.qunite.api.data.EntryRepository;
import com.qunite.api.data.QueueRepository;
import com.qunite.api.data.UserRepository;
import com.qunite.api.service.QueueService;
import com.qunite.api.utils.JpaRepositoryUtils;
import com.qunite.api.web.dto.queue.QueueCreationDto;
import com.qunite.api.web.mapper.QueueMapper;
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
class QueueControllerIntegrationTest {

  private final String url = "queues";

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private QueueRepository queueRepository;

  @Autowired
  private EntryRepository entryRepository;

  @Autowired
  private QueueMapper queueMapper;

  @Autowired
  private QueueService queueService;

  @AfterEach
  public void cleanAll() {
    userRepository.deleteAll();
    entryRepository.deleteAll();
    queueRepository.deleteAll();
  }

  @Test
  @Sql({"/users-create.sql", "/queues-create.sql"})
  void retrieveByIdWhenExists() throws Exception {
    var queueId = 1;

    var resultActions = mockMvc.perform(get("/{url}/{id}", url, queueId));

    resultActions.andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(queueId)));
  }

  @Test
  @Sql({"/users-create.sql", "/queues-create.sql"})
  void retrieveAllQueues() throws Exception {
    var resultActions = mockMvc.perform(get("/{url}", url));

    resultActions.andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(4)));
  }

  @Test
  @Sql({"/users-create.sql", "/queues-create.sql", "/entries-create.sql"})
  void retrieveMembersAmount() throws Exception {
    var queueId = 1;

    var resultActions = mockMvc.perform(get("/{url}/{id}/members-amount", url, queueId));

    resultActions.andExpect(status().isOk())
        .andExpect(content().string(is("5")));
  }

  @Test
  @Sql({"/users-create.sql", "/queues-create.sql", "/entries-create.sql"})
  void retrieveMemberPositionInQueue() throws Exception {
    var queueId = 1;
    var username = "Third";

    var resultActions = mockMvc.perform(get("/{url}/{queueId}/members/{username}",
        url, queueId, username));

    resultActions.andExpect(status().isOk())
        .andExpect(content().string(is("5")));
  }

  @Test
  @Sql({"/users-create.sql", "/queues-create.sql"})
  void retrieveQueueCreator() throws Exception {
    var queueId = 4;

    var resultActions = mockMvc.perform(get("/{url}/{id}/creator", url, queueId));

    resultActions.andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(1)));
  }

  @Test
  @Sql({"/users-create.sql", "/queues-create.sql", "/queues-managers-create.sql"})
  void retrieveQueueManagers() throws Exception {
    var queueId = 1;

    var resultActions = mockMvc.perform(get("/{url}/{id}/managers", url, queueId));

    resultActions.andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)));
  }

  @Test
  @Sql({"/users-create.sql", "/queues-create.sql", "/entries-create.sql"})
  void retrieveQueueEntries() throws Exception {
    var resultActions = mockMvc.perform(get("/{url}/{id}/members", url, 1));

    resultActions.andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(5)))
        .andExpect(content().json("""
              [
              {memberId:3, queueId:1},
              {memberId:4, queueId:1},
              {memberId:5, queueId:1},
              {memberId:6, queueId:1},
              {memberId:7, queueId:1}
            ]""")
        );
  }

  @Test
  @Sql({"/users-create.sql"})
  void createQueue() throws Exception {
    var queueName = "SomeQueue";

    var queue = new QueueCreationDto();
    queue.setName(queueName);

    var json = new ObjectMapper().writeValueAsString(queue);

    var resultActions = mockMvc.perform(post("/{url}", url)
        .contentType(MediaType.APPLICATION_JSON)
        .content(json));

    resultActions.andExpect(status().isCreated())
        .andExpect(jsonPath("$.name", is(queueName)))
        .andExpect(jsonPath("$.creatorId", is(1)));
  }

  @Test
  @Sql({"/users-create.sql", "/queues-create.sql"})
  void deleteQueue() throws Exception {
    var queueId = 1L;

    mockMvc.perform(delete("/{url}/{id}", url, queueId))
        .andExpect(status().isNoContent());
    assertThat(queueService.findById(queueId)).isEmpty();
  }

  @Test
  @Sql({"/users-create.sql", "/queues-create.sql"})
  void addManager() throws Exception {
    var queueId = 1L;
    var managerId = 3L;

    mockMvc.perform(post("/{url}/{id}/managers/{managerId}", url, queueId, managerId))
        .andExpect(status().isOk());
    var managersList = queueService.getManagers(queueId).orElseThrow();
    var manager = JpaRepositoryUtils.getById(managerId, userRepository);

    assertThat(managersList).containsOnly(manager);
  }

  @Test
  @Sql({"/users-create.sql", "/queues-create.sql", "/queues-managers-create.sql"})
  void deleteManager() throws Exception {
    var queueId = 1L;
    var managerId = 3L;

    mockMvc.perform(delete("/{url}/{id}/managers/{managerId}", url, queueId, managerId))
        .andExpect(status().isNoContent());
    var managersList = queueService.getManagers(queueId).orElseThrow();
    var manager = JpaRepositoryUtils.getById(managerId, userRepository);

    assertThat(managersList).doesNotContain(manager);
  }
}