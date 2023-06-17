package com.qunite.api.web.dto.user;

import java.io.Serializable;
import lombok.Data;

/**
 * A DTO for the {@link com.qunite.api.domain.User} entity
 */

@Data
public class UserDto implements Serializable {
  private Long id;
  private String username;
  private String email;
}
