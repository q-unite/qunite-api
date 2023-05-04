package com.qunite.api.web.dto;

import com.toedter.spring.hateoas.jsonapi.JsonApiId;
import com.toedter.spring.hateoas.jsonapi.JsonApiTypeForClass;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;

/**
 * A DTO for the {@link com.qunite.api.domain.Queue} entity
 */
@AllArgsConstructor
@Getter
@JsonApiTypeForClass("queues")
public class QueueDto extends RepresentationModel<QueueDto> implements Serializable {
  @JsonApiId
  private final Long id;
  private final String name;
  private final Long creatorId;
  private final String createdAt;
}