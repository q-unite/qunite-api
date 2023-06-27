package com.qunite.api.web;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qunite.api.domain.Entry;
import com.qunite.api.domain.EntryId;
import com.qunite.api.domain.Queue;
import com.qunite.api.domain.User;
import com.qunite.api.service.QueueService;
import com.qunite.api.web.controller.QueueController;
import com.qunite.api.web.mapper.EntryMapperImpl;
import com.qunite.api.web.mapper.QueueMapper;
import com.qunite.api.web.mapper.QueueMapperImpl;
import com.qunite.api.web.mapper.UserMapperImpl;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

// TODO: 27.06.2023  
@Disabled("Refactor due to security emergence")
@WebMvcTest(controllers = QueueController.class)
@Import({QueueMapperImpl.class, UserMapperImpl.class, EntryMapperImpl.class})
class QueueControllerTest {

  private final String url = "/queues";

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private QueueService queueService;

  @Autowired
  private QueueMapper queueMapper;

  @Test
  void retrieveByIdWhenExists() throws Exception {
    var queue = queue(1L);
    given(queueService.findById(anyLong())).willReturn(Optional.of(queue));

    var resultActions = mockMvc.perform(get(url + "/" + queue.getId())
        .accept(MediaType.APPLICATION_JSON));
    resultActions.andExpect(status().isOk())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(content().json("{id: 1}"));
  }

  @Test
  void retrieveAllQueues() throws Exception {
    var queues = queues(3);
    given(queueService.findAll()).willReturn(queues);

    var resultActions = mockMvc.perform(get(url).accept(MediaType.APPLICATION_JSON));
    resultActions.andExpect(status().isOk())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$", hasSize(queues.size())))
        .andExpect(content().json("[ {id: 1}, {id: 2}, {id: 3} ]"));
  }

  @Test
  void retrieveMembersAmount() throws Exception {
    var amount = 5;
    given(queueService.getMembersAmountInQueue(anyLong())).willReturn(Optional.of(amount));

    var response = mockMvc.perform(get(url + "/1/members-amount")
            .accept(MediaType.APPLICATION_JSON))
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
            .accept(MediaType.APPLICATION_JSON))
            .andReturn().getResponse();

    assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    assertThat(response.getContentAsString()).isEqualTo(String.valueOf(position));
  }

  @Test
  void retrieveQueueCreator() throws Exception {
    var queue = queue(1L);
    given(queueService.findById(anyLong())).willReturn(Optional.of(queue));

    var resultActions = mockMvc.perform(get(url + "/" + queue.getId() + "/creator")
        .accept(MediaType.APPLICATION_JSON));
    resultActions.andExpect(status().isOk())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(content().json("{id: 2}"));
  }

  @Test
  void retrieveQueueManagers() throws Exception {
    var queue = queue(1L);
    queue.setManagers(Set.of(user(1L), user(2L), user(3L)));

    given(queueService.findById(anyLong())).willReturn(Optional.of(queue));
    var resultActions = mockMvc.perform(get(url + "/" + queue.getId() + "/managers")
        .accept(MediaType.APPLICATION_JSON));
    resultActions.andExpect(status().isOk())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$", hasSize(queue.getManagers().size())))
        .andExpect(content().json("[ {id: 1}, {id: 2}, {id: 3} ]"));
  }

  @Test
  void retrieveQueueEntries() throws Exception {
    var queue = queue(1L);
    queue.setEntries(List.of(
        entry(1L, 1L), entry(2L, 1L), entry(3L, 1L)));

    given(queueService.findById(anyLong())).willReturn(Optional.of(queue));
    var resultActions = mockMvc.perform(get(url + "/" + queue.getId() + "/entries")
        .accept(MediaType.APPLICATION_JSON));
    resultActions.andExpect(status().isOk())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$", hasSize(queue.getEntries().size())))
        .andExpect(content().json("""
            [
              {memberId:1, queueId:1},
              {memberId:2, queueId:1},
              {memberId:3, queueId:1}
            ]""")
        );
  }

  @Test
  void createQueue() throws Exception {
    var queue = queue(1L);
    var dto = queueMapper.toDto(queue);
    var json = new ObjectMapper().writeValueAsString(dto);

    given(queueService.create(any(Queue.class))).willReturn(queue);
    var resultActions = mockMvc.perform(post(url)
        .contentType(MediaType.APPLICATION_JSON)
        .content(json));
    resultActions.andExpect(status().isCreated())
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(content().json("{id: 1}"));
  }

  @Test
  void deleteQueue() throws Exception {
    doNothing().when(queueService).deleteById(anyLong(), null);
    mockMvc.perform(delete(url + "/1"))
        .andExpect(status().isNoContent());
  }


  private Queue queue(Long id) {
    var queue = new Queue();
    queue.setId(id);
    queue.setCreator(user(2L));
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
