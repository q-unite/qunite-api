package com.qunite.api.web.dto;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * A DTO for the {@link com.qunite.api.domain.User} entity
 */

@AllArgsConstructor
@Getter
public class UserDto implements Serializable {
  private final Long id;
  private final String firstName;
  private final String lastName;
}