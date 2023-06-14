package com.qunite.api.web.dto;

import com.fasterxml.jackson.annotation.JsonView;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A DTO for the {@link com.qunite.api.domain.User} entity
 */

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserDto implements Serializable {
  private Long id;
  @JsonView(Views.Patch.class)
  private String firstName;
  @JsonView(Views.Patch.class)
  private String lastName;
}