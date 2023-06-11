package com.qunite.api.web.dto;

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
  private final String name;
  private final Long creatorId;
  private final String createdAt;
}