package com.qunite.api.web.dto;

import com.toedter.spring.hateoas.jsonapi.JsonApiId;
import com.toedter.spring.hateoas.jsonapi.JsonApiTypeForClass;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;

/**
 * A DTO for the {@link com.qunite.api.domain.User} entity
 */

@AllArgsConstructor
@Getter
@JsonApiTypeForClass("users")
public class UserDto extends RepresentationModel<UserDto> implements Serializable {
  @JsonApiId
  private final Long id;
  private final String firstName;
  private final String lastName;
}