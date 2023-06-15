package com.qunite.api.web.dto.user;

import lombok.Data;
import org.springframework.lang.Nullable;

@Data
public class UserUpdateDto {
  @Nullable
  private String firstName;
  @Nullable
  private String lastName;
}
