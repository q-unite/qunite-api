package com.qunite.api.web.dto;

import com.fasterxml.jackson.annotation.JsonView;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * A DTO for the {@link com.qunite.api.domain.Queue} entity
 */
@AllArgsConstructor
@Getter
public class QueueDto implements Serializable {
  private final Long id;
  @JsonView(Views.Patch.class)
  private final String name;
  @JsonView(Views.Patch.class)
  private final Long creatorId;
  @JsonView(Views.Patch.class)
  private final String createdAt;
}