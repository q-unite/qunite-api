package com.qunite.api.web.dto;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A DTO for the {@link com.qunite.api.domain.Queue} entity
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class QueueDto implements Serializable {
  private Long id;
  private String name;
  private Long creatorId;
  private String createdAt;
}