package com.qunite.api.web.dto;

import com.fasterxml.jackson.annotation.JsonView;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * A DTO for the {@link com.qunite.api.domain.User} entity
 */

@AllArgsConstructor
@Getter
public class UserDto implements Serializable {
  @JsonView(Views.Post.class)
  private final Long id;
  @JsonView(Views.Patch.class)
  private final String firstName;
  @JsonView(Views.Patch.class)
  private final String lastName;
}