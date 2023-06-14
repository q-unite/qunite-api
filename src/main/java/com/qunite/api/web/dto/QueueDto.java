package com.qunite.api.web.dto;

import com.fasterxml.jackson.annotation.JsonView;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * A DTO for the {@link com.qunite.api.domain.Queue} entity
 */
@AllArgsConstructor
@Data
public class QueueDto implements Serializable {
  private Long id;
  @JsonView(Views.Patch.class)
  private String name;
  private Long creatorId;
  private String createdAt;
}