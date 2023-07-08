package com.qunite.api.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qunite.api.annotation.IntegrationTest;
import com.qunite.api.data.EntryRepository;
import com.qunite.api.data.QueueRepository;
import com.qunite.api.data.UserRepository;
import com.qunite.api.domain.Queue;
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
class QueueControllerSecurityIntegrationTest {
  private final String url = "queues";

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private QueueMapper queueMapper;

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
  void unauthenticatedUserShouldHaveNoAccess() throws Exception {
    mockMvc.perform(get("/{url}", url))
        .andExpect(status().isForbidden());
    mockMvc.perform(get("/{url}/1", url))
        .andExpect(status().isForbidden());
    mockMvc.perform(get("/{url}/1/members-amount", url))
        .andExpect(status().isForbidden());
    mockMvc.perform(get("/{url}/1/members/3", url))
        .andExpect(status().isForbidden());
    mockMvc.perform(get("/{url}/1/creator", url))
        .andExpect(status().isForbidden());
    mockMvc.perform(get("/{url}/1/managers", url))
        .andExpect(status().isForbidden());
    mockMvc.perform(get("/{url}/1/entries", url))
        .andExpect(status().isForbidden());

    var queue = new Queue();
    queue.setName("SomeQueue");

    var dto = queueMapper.toDto(queue);
    var json = new ObjectMapper().writeValueAsString(dto);

    mockMvc.perform(post("/{url}", url)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andExpect(status().isForbidden());

    mockMvc.perform(delete("/{url}/1", url))
        .andExpect(status().isForbidden());
  }


  @Test
  @WithMockUser("Seventh")
  @Sql({"/users-create.sql", "/queues-create.sql"})
  void anyAuthenticatedUserShouldRetrieveAllQueues() throws Exception {
    mockMvc.perform(get("/{url}", url))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser("Seventh")
  @Sql({"/users-create.sql", "/queues-create.sql"})
  void anyAuthenticatedUserShouldRetrieveById() throws Exception {
    mockMvc.perform(get("/{url}/1", url))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser("Seventh")
  @Sql({"/users-create.sql", "/queues-create.sql"})
  void anyAuthenticatedUserShouldRetrieveMembersAmount() throws Exception {
    mockMvc.perform(get("/{url}/1/members-amount", url))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser("Seventh")
  @Sql({"/users-create.sql", "/queues-create.sql", "/entries-create.sql"})
  void anyAuthenticatedUserShouldRetrieveMemberPosition() throws Exception {
    mockMvc.perform(get("/{url}/1/members/3", url))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser("Seventh")
  @Sql({"/users-create.sql", "/queues-create.sql"})
  void anyAuthenticatedUserShouldRetrieveCreator() throws Exception {
    mockMvc.perform(get("/{url}/1/creator", url))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser("Seventh")
  @Sql({"/users-create.sql", "/queues-create.sql", "/queues-managers-create.sql"})
  void anyAuthenticatedUserShouldRetrieveManagers() throws Exception {
    mockMvc.perform(get("/{url}/1/managers", url))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser("Seventh")
  @Sql({"/users-create.sql", "/queues-create.sql", "/entries-create.sql"})
  void anyAuthenticatedUserShouldRetrieveQueueEntries() throws Exception {
    mockMvc.perform(get("/{url}/1/entries", url))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser("Seventh")
  @Sql("/users-create.sql")
  void anyAuthenticatedUserShouldCreateQueue() throws Exception {
    var queue = new Queue();
    queue.setName("SomeQueue");

    var dto = queueMapper.toDto(queue);
    var json = new ObjectMapper().writeValueAsString(dto);

    mockMvc.perform(post("/{url}", url)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andExpect(status().isCreated());
  }

  @Test
  @WithMockUser("First")
  @Sql({"/users-create.sql", "/queues-create.sql"})
  void queueCreatorShouldDeleteQueue() throws Exception {
    mockMvc.perform(delete("/{url}/1", url))
        .andExpect(status().isNoContent());
  }

  @Test
  @WithMockUser("Seventh")
  @Sql({"/users-create.sql", "/queues-create.sql"})
  void notQueueCreatorShouldNotDeleteQueue() throws Exception {
    mockMvc.perform(delete("/{url}/1", url))
        .andExpect(status().isForbidden());
  }
}
