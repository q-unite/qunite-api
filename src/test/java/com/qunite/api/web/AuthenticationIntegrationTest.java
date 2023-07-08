package com.qunite.api.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.qunite.api.annotation.IntegrationTest;
import com.qunite.api.data.UserRepository;
import com.qunite.api.domain.User;
import com.qunite.api.service.UserService;
import com.qunite.api.web.dto.auth.AuthenticationRequest;
import com.qunite.api.web.mapper.UserMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@IntegrationTest
class AuthenticationIntegrationTest {

  private final String url = "auth";

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private UserMapper userMapper;

  @Autowired
  private UserService userService;

  @Autowired
  private UserRepository userRepository;

  @AfterEach
  public void cleanAll() {
    userRepository.deleteAll();
  }

  @Test
  void unauthenticatedUserShouldAccessEndpoints() throws Exception {
    mockMvc.perform(post("/{url}/sign-up", url))
        .andExpect(status().isBadRequest());
    mockMvc.perform(post("/{url}/sign-in", url))
        .andExpect(status().isBadRequest());
  }

  @Test
  void signUpShouldCreateNewUserWithValidData() throws Exception {
    var user = new User();
    user.setUsername("John");
    user.setPassword("Johnson");
    user.setEmail("john@gmail.com");

    var dto = userMapper.toUserCreationDto(user);
    var json = new ObjectMapper().writeValueAsString(dto);

    mockMvc.perform(
            post("/{url}/sign-up", url)
                .contentType(MediaType.APPLICATION_JSON).content(json))
        .andExpect(status().is2xxSuccessful());

    assertThat(userService.findByUsername("John")).isPresent();
  }

  @ParameterizedTest
  @CsvSource({"First, Johnson, john@gmail.com", "John, Johnson, User1@user.com"})
  @Sql("/users-create.sql")
  void signUpShouldNotCreateWithExistingLogin(String username, String password, String email) throws Exception {
    var user = new User();
    user.setUsername(username);
    user.setPassword(password);
    user.setEmail(email);

    var dto = userMapper.toUserCreationDto(user);
    var json = new ObjectMapper().writeValueAsString(dto);

    mockMvc.perform(
            post("/{url}/sign-up", url)
                .contentType(MediaType.APPLICATION_JSON).content(json))
        .andExpect(status().isBadRequest());
  }

  @Test
  void signUpShouldEncryptPassword() throws Exception {
    var user = new User();
    user.setUsername("John");
    user.setPassword("Johnson");
    user.setEmail("john@gmail.com");

    var dto = userMapper.toUserCreationDto(user);
    var json = new ObjectMapper().writeValueAsString(dto);

    mockMvc.perform(
            post("/{url}/sign-up", url)
                .contentType(MediaType.APPLICATION_JSON).content(json))
        .andExpect(status().isOk());

    var createdUser = userService.findByUsername("John").get();
    var encoder = new BCryptPasswordEncoder();
    assertTrue(encoder.matches("Johnson", createdUser.getPassword()));
  }

  @ParameterizedTest
  @CsvSource({"First, asd", "User1@user.com, asd"})
  @Sql("/users-create.sql")
  void signInShouldReturnAccessTokenWithValidLogin(String login, String password) throws Exception {
    var requestData = new AuthenticationRequest();
    requestData.setLogin(login);
    requestData.setPassword(password);

    var json = new ObjectMapper().writeValueAsString(requestData);

    var resultActions = mockMvc.perform(post("/{url}/sign-in", url)
        .contentType(MediaType.APPLICATION_JSON).content(json));

    resultActions
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token", notNullValue()))
        .andExpect(jsonPath("$.type", is("JWT")))
        .andExpect(jsonPath("$.algorithm", is("HS256")));
  }

  @Test
  @Sql("/users-create.sql")
  void signInShouldNotReturnAccessTokenWithInvalidLogin() throws Exception {
    var requestData = new AuthenticationRequest();
    requestData.setLogin("invalid228");
    requestData.setPassword("asd");

    var json = new ObjectMapper().writeValueAsString(requestData);

    var resultActions = mockMvc.perform(post("/{url}/sign-in", url)
        .contentType(MediaType.APPLICATION_JSON).content(json));
    resultActions
        .andExpect(status().isForbidden());
  }

  @Test
  @Sql("/users-create.sql")
  void signInShouldNotReturnAccessTokenWithInvalidPassword() throws Exception {
    var requestData = new AuthenticationRequest();
    requestData.setLogin("First");
    requestData.setPassword("invalidik");

    var json = new ObjectMapper().writeValueAsString(requestData);

    var resultActions = mockMvc.perform(post("/{url}/sign-in", url)
        .contentType(MediaType.APPLICATION_JSON).content(json));
    resultActions
        .andExpect(status().isForbidden());
  }

  @Test
  @Sql("/users-create.sql")
  void accessTokenShouldBeValid() throws Exception {
    var requestData = new AuthenticationRequest();
    requestData.setLogin("First");
    requestData.setPassword("asd");

    var json = new ObjectMapper().writeValueAsString(requestData);

    var resultActions = mockMvc.perform(post("/{url}/sign-in", url)
        .contentType(MediaType.APPLICATION_JSON).content(json));

    var token =
        new ObjectMapper().readTree(resultActions.andReturn().getResponse().getContentAsString())
            .get("token").asText();

    mockMvc.perform(get("/users/self")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.username", is("First")));
  }

  @Test
  @Sql("/users-create.sql")
  void invalidAccessTokenShouldBeInvalid() throws Exception {
    mockMvc.perform(get("/users/self")
            .header(HttpHeaders.AUTHORIZATION, "Bearer aaaaaaaaaaaaaaaaaaaaaaaaa"))
        .andExpect(status().isForbidden());
  }
}
