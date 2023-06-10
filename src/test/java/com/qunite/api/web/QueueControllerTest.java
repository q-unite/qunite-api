package com.qunite.api.web;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.qunite.api.annotation.IntegrationTest;
import com.qunite.api.data.EntryRepository;
import com.qunite.api.data.UserRepository;
import com.qunite.api.domain.Entry;
import com.qunite.api.domain.EntryId;
import com.qunite.api.domain.Queue;
import com.qunite.api.domain.User;
import com.qunite.api.service.QueueService;
import com.qunite.api.web.mapper.QueueMapper;
import com.qunite.api.web.mapper.QueueMapperImpl;
import com.toedter.spring.hateoas.jsonapi.MediaTypes;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;


@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class})
@Import(QueueMapperImpl.class)
@MockBeans({@MockBean(UserRepository.class), @MockBean(EntryRepository.class)})
class QueueControllerTest {

  @Value(value = "${local.server.port}")
  private Integer port;

  private String url;

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private QueueService queueService;

  @Autowired
  private QueueMapper queueMapper;


  @BeforeEach
  void initUrl() {
    url = "http://localhost:" + port + "/api/v1/queues";
  }

  @Test
  void retrieveByIdWhenExists() throws Exception {
    var queue = queue(1L);
    given(queueService.findById(anyLong())).willReturn(Optional.of(queue));

    var resultActions = mockMvc.perform(get(url + "/" + queue.getId())
        .accept(MediaTypes.JSON_API_VALUE));
    resultActions.andExpect(status().isOk())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.JSON_API_VALUE))
        .andExpect(jsonPath("links.self.meta.affordances", notNullValue()))
        .andExpect(jsonPath("links.self.href", is(url + "/" + queue.getId())))
        .andExpect(jsonPath("data.id", is(queue.getId().toString())))
        .andExpect(jsonPath("data.type", is("queues")))
        .andExpect(jsonPath("data.relationships", notNullValue()))
        .andExpect(jsonPath("data.relationships.creator.links.related",
            is(url + "/" + queue.getId() + "/creator")));
  }

  @Test
  void retrieveAllQueues() throws Exception {
    var queues = queues(3);
    given(queueService.findAll()).willReturn(queues);

    var resultActions = mockMvc.perform(get(url).accept(MediaTypes.JSON_API_VALUE));
    resultActions.andExpect(status().isOk())
        .andExpect(jsonPath("data", hasSize(queues.size())))
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.JSON_API_VALUE))
        .andExpect(jsonPath("links.self", is(url)))
        .andExpect(content().string(allOf(
            containsString("\"id\":\"1\""),
            containsString("\"id\":\"2\""),
            containsString("\"id\":\"3\""))));
  }

  @Test
  void retrieveMembersAmount() throws Exception {
    var amount = 5;
    given(queueService.getMembersAmountInQueue(anyLong())).willReturn(Optional.of(amount));

    var response = mockMvc.perform(get(url + "/1/members-amount")
            .accept(MediaTypes.JSON_API_VALUE))
        .andReturn().getResponse();

    assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    assertThat(response.getContentAsString()).isEqualTo(String.valueOf(amount));
  }

  @Test
  void retrieveMemberPositionInQueue() throws Exception {
    var position = 5;
    given(queueService.getMemberPositionInQueue(anyLong(), anyLong()))
        .willReturn(Optional.of(position));

    var response = mockMvc.perform(get(url + "/1/members/1")
            .accept(MediaTypes.JSON_API_VALUE))
        .andReturn().getResponse();

    assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    assertThat(response.getContentAsString()).isEqualTo(String.valueOf(position));
  }

  @Test
  void retrieveQueueCreator() throws Exception {
    var queue = queue(1L);
    given(queueService.findById(anyLong())).willReturn(Optional.of(queue));

    var resultActions = mockMvc.perform(get(url + "/" + queue.getId() + "/creator")
        .accept(MediaTypes.JSON_API_VALUE));
    resultActions.andExpect(status().isOk())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.JSON_API_VALUE))
        .andExpect(jsonPath("data.id", is(queue.getCreator().getId().toString())))
        .andExpect(jsonPath("data.type", is("users")));
  }

  @Test
  void retrieveQueueManagers() throws Exception {
    var queue = queue(1L);
    queue.setManagers(Set.of(user(1L), user(2L), user(3L)));

    given(queueService.findById(anyLong())).willReturn(Optional.of(queue));
    var resultActions = mockMvc.perform(get(url + "/" + queue.getId() + "/managers")
        .accept(MediaTypes.JSON_API_VALUE));
    resultActions.andExpect(status().isOk())
        .andExpect(jsonPath("data", hasSize(queue.getManagers().size())))
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.JSON_API_VALUE))
        .andExpect(content().string(allOf(
            containsString("\"id\":\"1\""),
            containsString("\"id\":\"2\""),
            containsString("\"id\":\"3\""))));
  }

  @Test
  void retrieveQueueEntries() throws Exception {
    var queue = queue(1L);
    queue.setEntries(List.of(
        entry(1L, 1L), entry(2L, 1L), entry(3L, 1L)));

    given(queueService.findById(anyLong())).willReturn(Optional.of(queue));
    var resultActions = mockMvc.perform(get(url + "/" + queue.getId() + "/entries")
        .accept(MediaTypes.JSON_API_VALUE));
    resultActions.andExpect(status().isOk())
        .andExpect(jsonPath("data", hasSize(queue.getEntries().size())))
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.JSON_API_VALUE))
        .andExpect(content().string(allOf(
            containsString("\"id\":\"1\""),
            containsString("\"id\":\"2\""),
            containsString("\"id\":\"3\""))));
  }

  @Test
  void createQueue() throws Exception {
    var queue = queue(1L);
    var dto = queueMapper.toDto(queue);
    var json = new ObjectMapper().writeValueAsString(dto);

    given(queueService.create(any(Queue.class))).willReturn(queue);
    var resultActions = mockMvc.perform(post(url + "/")
        .contentType(MediaType.APPLICATION_JSON)
        .content(json));
    resultActions.andExpect(status().isCreated())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.JSON_API_VALUE))
        .andExpect(content().string(allOf(
            containsString("\"id\":\"1\""),
            containsString("\"type\":\"queues\"")
        )));
  }

  @Test
  void deleteQueue() throws Exception {
    doNothing().when(queueService).deleteById(anyLong());
    mockMvc.perform(delete(url + "/1"))
        .andExpect(status().isOk());

  }


  private Queue queue(Long id) {
    var queue = new Queue();
    queue.setId(id);
    queue.setCreator(user(new Random().nextLong()));
    return queue;
  }

  private User user(Long id) {
    var creator = new User();
    creator.setId(id);
    return creator;
  }

  private Entry entry(Long memberId, Long queueId) {
    var entry = new Entry();
    entry.setId(new EntryId(memberId, queueId));
    entry.setMember(user(memberId));
    entry.setQueue(queue(queueId));
    return entry;
  }

  private List<Queue> queues(int amount) {
    return IntStream.rangeClosed(1, amount)
        .mapToObj(Long::valueOf)
        .map(this::queue)
        .toList();
  }
}
