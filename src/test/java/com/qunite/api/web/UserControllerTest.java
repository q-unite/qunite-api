package com.qunite.api.web;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qunite.api.domain.Queue;
import com.qunite.api.domain.User;
import com.qunite.api.service.UserService;
import com.qunite.api.web.controller.UserController;
import com.qunite.api.web.mapper.EntryMapperImpl;
import com.qunite.api.web.mapper.QueueMapperImpl;
import com.qunite.api.web.mapper.UserMapper;
import com.qunite.api.web.mapper.UserMapperImpl;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

// TODO: 27.06.2023  
@Disabled("Refactor due to security emergence")
@WebMvcTest(controllers = UserController.class)
@Import({QueueMapperImpl.class, UserMapperImpl.class, EntryMapperImpl.class})
class UserControllerTest {
  private final String url = "/users";

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private UserMapper userMapper;

  @MockBean
  private UserService userService;

  @Test
  void retrieveByIdWhenExists() throws Exception {
    var user = user(1L);
    given(userService.findOne(anyLong())).willReturn(Optional.of(user));

    var resultActions =
        mockMvc.perform(get("/{url}/{id}", url, user.getId()));

    resultActions.andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(user.getId().intValue())));
  }

  @Test
  void retrieveAll() throws Exception {
    var users = users(3);
    given(userService.findAll()).willReturn(users);

    var resultActions = mockMvc.perform(get("/{url}", url));
    resultActions.andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(users.size()))).andExpect(content().json("""
            [ {"id": 1}, {"id": 2}, {"id": 3} ]"""));
  }

  @Test
  void retrieveManagedQueues() throws Exception {
    var userId = 1L;
    var queues = queues(3, 1L, userId);
    given(userService.getManagedQueues(anyLong())).willReturn(Optional.of(queues));

    var resultActions =
        mockMvc.perform(get("/{url}/{id}/managed-queues", url, userId));

    resultActions.andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(queues.size())))
        .andExpect(content().json("""
            [ {"id": 1}, {"id": 2}, {"id": 3} ]"""));
  }

  @Test
  void retrieveCreatedQueues() throws Exception {
    var userId = 1;
    var queues = queues(4, (long) userId);
    given(userService.getCreatedQueues(anyLong())).willReturn(Optional.of(queues));

    var resultActions =
        mockMvc.perform(get("/{url}/{id}/created-queues", url, userId));

    resultActions.andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(queues.size())))
        .andExpect(content().json("""
            [ {"id": 1}, {"id": 2}, {"id": 3} , {"id": 4} ]"""));
  }

  @Test
  void createUser() throws Exception {
    var user = user(1L);
    var dto = userMapper.toDto(user);
    var json = new ObjectMapper().writeValueAsString(dto);

    given(userService.createOne(any(User.class))).willReturn(user);

    var resultActions =
        mockMvc.perform(post("/{url}", url).contentType(MediaType.APPLICATION_JSON).content(json));

    resultActions.andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", is(user.getId().intValue())));
  }

  @Test
  void updateUser() throws Exception {
    final var user = user(1L);
    user.setUsername("John");
    final var dto = userMapper.toDto(user);
    final var json = new ObjectMapper().writeValueAsString(dto);
    var expectedUser = user(1L);
    expectedUser.setUsername("Mark");

    given(userService.findOne(anyLong())).willReturn(Optional.of(user));
    given(userService.createOne(any(User.class))).willReturn(expectedUser);

    var resultActions =
        mockMvc.perform(patch("/{url}/{id}", url, user.getId())
            .contentType(MediaType.APPLICATION_JSON).content(json));

    resultActions.andExpect(status().isOk())
        .andExpect(jsonPath("$.username", is("Mark")));
  }

  @Test
  void deleteUser() throws Exception {
    doNothing().when(userService).deleteOne(anyLong());
    mockMvc.perform(delete("/{url}/1", url))
        .andExpect(status().isNoContent());
  }


  private Queue queue(Long queueId, Long creatorId) {
    var queue = new Queue();
    queue.setId(queueId);
    queue.setCreator(user(creatorId));
    return queue;
  }

  private Queue queue(Long queueId, Long creatorId, Long managerId) {
    var queue = new Queue();
    queue.setId(queueId);
    queue.addManager(user(managerId));
    queue.setCreator(user(creatorId));
    return queue;
  }

  private User user(Long id) {
    var creator = new User();
    creator.setId(id);
    return creator;
  }


  private List<Queue> queues(int amount, Long creatorId) {
    return IntStream.rangeClosed(1, amount).mapToObj(Long::valueOf).map(id -> queue(id, creatorId))
        .toList();
  }

  private List<Queue> queues(int amount, Long creatorId, Long managerId) {
    return IntStream.rangeClosed(1, amount).mapToObj(Long::valueOf)
        .map(id -> queue(id, creatorId, managerId)).toList();
  }

  private List<User> users(int amount) {
    return IntStream.rangeClosed(1, amount).mapToObj(Long::valueOf).map(this::user).toList();
  }
}
