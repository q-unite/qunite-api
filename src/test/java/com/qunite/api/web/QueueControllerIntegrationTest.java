package com.qunite.api.web;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import com.qunite.api.domain.Queue;
import com.qunite.api.service.UserService;
import com.qunite.api.web.mapper.QueueMapper;
import java.util.Random;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@IntegrationTest
public class QueueControllerIntegrationTest {

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
  private UserService userService;

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
    var resultActions = mockMvc.perform(get(url));

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
    var memberId = 3;

    var resultActions = mockMvc.perform(get("/{url}/{queueId}/members/{memberId}",
        url, queueId, memberId));

    resultActions.andExpect(status().isOk())
        .andExpect(content().string(is("5")));
  }

  @Test
  @Sql({"/users-create.sql", "/queues-create.sql"})
  void retrieveQueueCreator() throws Exception {
    var queueId = 4;

    var resultActions = mockMvc.perform(get("/{url}/1/{id}/creator", url, queueId));

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
    var queueId = 1;

    var resultActions = mockMvc.perform(get("/{url}/{id}/entries", url, 1));

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
    var queue = new Queue();
    var creator = userService.findOne(1L);
    queue.setName("SomeQueue");
    queue.setCreator(creator.get());

    var dto = queueMapper.toDto(queue);
    var json = new ObjectMapper().writeValueAsString(dto);

    var resultActions = mockMvc.perform(post("/{url}", url)
        .contentType(MediaType.APPLICATION_JSON)
        .content(json));

    resultActions.andExpect(status().isCreated())
        .andExpect(jsonPath("$.name", is("SomeQueue")));
  }

  @Test
  @Sql({"/users-create.sql", "/queues-create.sql"})
  void deleteQueue() throws Exception {
    var queueId = new Random().nextLong();

    mockMvc.perform(delete("/{url}/{id}", url, queueId))
        .andExpect(status().isNoContent());
  }

}
