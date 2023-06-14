package com.qunite.api.web.dto;

import com.fasterxml.jackson.annotation.JsonView;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * A DTO for the {@link com.qunite.api.domain.User} entity
 */

@Data
@AllArgsConstructor
public class UserDto implements Serializable {
  private Long id;
  @JsonView(Views.Patch.class)
  private String firstName;
  @JsonView(Views.Patch.class)
  private String lastName;
}