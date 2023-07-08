package com.qunite.api.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.auth0.jwt.exceptions.JWTDecodeException;
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

  private static final String occupiedUsername = "First";
  private static final String notOccupiedUsername = "John";
  private static final String occupiedEmail = "User1@user.com";
  private static final String notOccupiedEmail = "john@user.com";
  private static final String correctPassword = "asd";
  private static final String incorrectPassword = "dsa";

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
    user.setUsername(notOccupiedUsername);
    user.setPassword(correctPassword);
    user.setEmail(notOccupiedEmail);

    var dto = userMapper.toUserCreationDto(user);
    var json = new ObjectMapper().writeValueAsString(dto);

    mockMvc.perform(
            post("/{url}/sign-up", url)
                .contentType(MediaType.APPLICATION_JSON).content(json))
        .andExpect(status().is2xxSuccessful());

    assertThat(userService.findByUsername(notOccupiedUsername)).isPresent();
  }

  @ParameterizedTest
  @CsvSource({
      occupiedUsername + ", " + incorrectPassword + ", " + notOccupiedEmail,
      notOccupiedUsername + ", " + incorrectPassword + ", " + occupiedEmail
  })
  @Sql("/users-create.sql")
  void signUpShouldNotCreateWithExistingLogin(String username,
                                              String password, String email) throws Exception {
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

  @ParameterizedTest
  @CsvSource({
      occupiedUsername + ", " + correctPassword,
      occupiedEmail + ", " + correctPassword
  })
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
        .andExpect(jsonPath("$.token", notNullValue()));
  }

  @ParameterizedTest
  @CsvSource({
      notOccupiedEmail + ", " + correctPassword,
      occupiedUsername + ", " + incorrectPassword
  })
  @Sql("/users-create.sql")
  void signInShouldNotReturnAccessTokenWithInvalidData() throws Exception {
    var requestData = new AuthenticationRequest();
    requestData.setLogin(notOccupiedEmail);
    requestData.setPassword(correctPassword);

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
    requestData.setLogin(occupiedUsername);
    requestData.setPassword(correctPassword);

    var json = new ObjectMapper().writeValueAsString(requestData);

    var resultActions = mockMvc.perform(post("/{url}/sign-in", url)
        .contentType(MediaType.APPLICATION_JSON).content(json));

    var token =
        new ObjectMapper().readTree(resultActions.andReturn().getResponse().getContentAsString())
            .get("token").asText();

    mockMvc.perform(get("/users/self")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.username", is(occupiedUsername)));
  }

  @Test
  @Sql("/users-create.sql")
  void invalidAccessTokenShouldBeInvalid() {
    assertThrows(JWTDecodeException.class, () -> mockMvc.perform(get("/users/self")
            .header(HttpHeaders.AUTHORIZATION, "Bearer aaaaaaaaaaaaaaaaaaaaaaaaa"))
        .andExpect(status().isForbidden()));
  }
}