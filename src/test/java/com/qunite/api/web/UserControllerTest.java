package com.qunite.api.web;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.qunite.api.data.EntryRepository;
import com.qunite.api.data.UserRepository;
import com.qunite.api.domain.Entry;
import com.qunite.api.domain.EntryId;
import com.qunite.api.domain.Queue;
import com.qunite.api.domain.User;
import com.qunite.api.service.QueueService;
import com.qunite.api.service.UserService;
import com.qunite.api.web.controller.UserController;
import com.qunite.api.web.mapper.QueueMapper;
import com.qunite.api.web.mapper.QueueMapperImpl;
import com.qunite.api.web.mapper.UserMapper;
import com.qunite.api.web.mapper.UserMapperImpl;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
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
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@Import(UserMapperImpl.class)
@MockBeans({@MockBean(QueueMapperImpl.class)})
@ActiveProfiles("test")
public class UserControllerTest {
  private final String url = "/api/v1/users";

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

    var resultActions = mockMvc.perform(get(url).accept(
        MediaType.JSON_AP));
    resultActions
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
