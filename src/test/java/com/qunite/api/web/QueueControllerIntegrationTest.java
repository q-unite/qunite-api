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

  private final String url = "/queues";

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
  UserService userService;
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
    var resultActions = mockMvc.perform(get(url + "/" + queueId)
        .accept(MediaType.APPLICATION_JSON));
    resultActions.andExpect(status().isOk())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.id", is(queueId)));
  }

  @Test
  @Sql({"/users-create.sql", "/queues-create.sql"})
  void retrieveAllQueues() throws Exception {
    var resultActions = mockMvc.perform(get(url).accept(MediaType.APPLICATION_JSON));
    resultActions.andExpect(status().isOk())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$", hasSize(4)));
  }

  @Test
  @Sql({"/users-create.sql", "/queues-create.sql", "/entries-create.sql"})
  void retrieveMembersAmount() throws Exception {
    var queueId = 1;
    var amount = "5";
    var response = mockMvc.perform(get(url + "/" + queueId + "/members-amount")
            .accept(MediaType.APPLICATION_JSON))
        .andReturn().getResponse();

    assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    assertThat(response.getContentAsString()).isEqualTo(amount);
  }

  @Test
  @Sql({"/users-create.sql", "/queues-create.sql", "/entries-create.sql"})
  void retrieveMemberPositionInQueue() throws Exception {
    var queueId = 1;
    var memberId = 3;
    var position = 4+1;
    var response = mockMvc.perform(get(url + "/" + queueId + "/members/" + memberId)
            .accept(MediaType.APPLICATION_JSON))
        .andReturn().getResponse();

    assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    assertThat(response.getContentAsString()).isEqualTo(String.valueOf(position));
  }

  @Test
  @Sql({"/users-create.sql", "/queues-create.sql"})
  void retrieveQueueCreator() throws Exception {
    var queueId = 4;
    var creatorId = 1;
    var creatorName = "First";
    var resultActions = mockMvc.perform(get(url + "/" + queueId + "/creator")
        .accept(MediaType.APPLICATION_JSON));
    resultActions.andExpect(status().isOk())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.creator.firstName", is("First")));
  }

  @Test
  @Sql({"/users-create.sql", "/queues-create.sql", "/queues-managers-create.sql"})
  void retrieveQueueManagers() throws Exception {
    var queueId = 1;
    var size = 2;

    var resultActions = mockMvc.perform(get(url + "/" + queueId + "/managers")
        .accept(MediaType.APPLICATION_JSON));
    resultActions.andExpect(status().isOk())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$", hasSize(size)));
  }

  @Test
  @Sql({"/users-create.sql", "/queues-create.sql", "/entries-create.sql"})
  void retrieveQueueEntries() throws Exception {
    var queueId = 1;
    var size = 5;

    var resultActions = mockMvc.perform(get(url + "/" + queueId + "/entries")
        .accept(MediaType.APPLICATION_JSON));
    resultActions.andExpect(status().isOk())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$", hasSize(size)))
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

    var resultActions = mockMvc.perform(post(url)
        .contentType(MediaType.APPLICATION_JSON)
        .content(json));
    resultActions.andExpect(status().isCreated())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("name", is("SomeQueue")));
  }

  @Test
  @Sql({"/users-create.sql", "/queues-create.sql"})
  void deleteQueue() throws Exception {
    var queueId = 1;
    mockMvc.perform(delete(url + "/" + queueId))
        .andExpect(status().isNoContent());
  }


}
