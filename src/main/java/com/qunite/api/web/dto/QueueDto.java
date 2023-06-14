package com.qunite.api.web.dto;

import com.fasterxml.jackson.annotation.JsonView;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A DTO for the {@link com.qunite.api.domain.Queue} entity
 */
@AllArgsConstructor
@Getter
@Setter
public class QueueDto implements Serializable {
  private Long id;
  @JsonView(Views.Patch.class)
  private String name;
  @JsonView(Views.Patch.class)
  private Long creatorId;
  private String createdAt;
}