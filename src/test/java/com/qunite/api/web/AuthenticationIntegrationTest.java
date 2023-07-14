package com.qunite.api.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.qunite.api.annotation.IntegrationTest;
import com.qunite.api.data.UserRepository;
import com.qunite.api.service.UserService;
import com.qunite.api.web.dto.auth.AuthenticationRequest;
import com.qunite.api.web.dto.user.UserCreationDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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
  private UserService userService;

  @Autowired
  private UserRepository userRepository;

  @AfterEach
  public void cleanAll() {
    userRepository.deleteAll();
  }

  @Test
  @DisplayName("Any user should access auth endpoints")
  void anyUserShouldAccessEndpoints() throws Exception {
    mockMvc.perform(post("/{url}/sign-up", url))
        .andExpect(status().isBadRequest());
    mockMvc.perform(post("/{url}/sign-in", url))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("Sign up should create user with not used username and email")
  void signUpShouldCreate() throws Exception {
    var user = new UserCreationDto();
    user.setUsername("John");
    user.setPassword("asd");
    user.setEmail("John@user.com");

    var json = new ObjectMapper().writeValueAsString(user);

    mockMvc.perform(
            post("/{url}/sign-up", url)
                .contentType(MediaType.APPLICATION_JSON).content(json))
        .andExpect(status().is2xxSuccessful());

    assertThat(userService.findByUsername("John")).isPresent();
  }

  @ParameterizedTest
  @DisplayName("Sign up should not create user with existing username or email")
  @CsvSource({
      "First, dsa, John@user.com",
      "John, dsa, User1@user.com"
  })
  @Sql("/users-create.sql")
  void signUpShouldNotAllowUsedLogin(String username,
                                     String password, String email) throws Exception {
    var user = new UserCreationDto();
    user.setUsername(username);
    user.setPassword(password);
    user.setEmail(email);

    var json = new ObjectMapper().writeValueAsString(user);

    mockMvc.perform(
            post("/{url}/sign-up", url)
                .contentType(MediaType.APPLICATION_JSON).content(json))
        .andExpect(status().isBadRequest());
  }

  @ParameterizedTest
  @DisplayName("Sign in should return access token when given user exists")
  @CsvSource({
      "First, asd",
      "User1@user.com, asd"
  })
  @Sql("/users-create.sql")
  void signInShouldReturnAccessToken(String login, String password) throws Exception {
    var requestData = new AuthenticationRequest();
    requestData.setLogin(login);
    requestData.setPassword(password);

    var json = new ObjectMapper().writeValueAsString(requestData);

    var resultActions = mockMvc.perform(post("/{url}/sign-in", url)
        .contentType(MediaType.APPLICATION_JSON).content(json));

    resultActions
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token", notNullValue()));
  }

  @ParameterizedTest
  @DisplayName("sign in should not return access token when password does not match or"
      + " user does not exist")
  @CsvSource({
      "John@user.com, asd",
      "First, dsa"
  })
  @Sql("/users-create.sql")
  void signInShouldCheckCredentials() throws Exception {
    var requestData = new AuthenticationRequest();
    requestData.setLogin("John@user.com");
    requestData.setPassword("asd");

    var json = new ObjectMapper().writeValueAsString(requestData);

    var resultActions = mockMvc.perform(post("/{url}/sign-in", url)
        .contentType(MediaType.APPLICATION_JSON).content(json));
    resultActions
        .andExpect(status().isNotFound());
  }
}