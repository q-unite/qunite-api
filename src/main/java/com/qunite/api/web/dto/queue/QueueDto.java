package com.qunite.api.web.dto.queue;

import java.io.Serializable;
import lombok.Data;

/**
 * A DTO for the {@link com.qunite.api.domain.Queue} entity
 */

@Data
public class QueueDto implements Serializable {
  private Long id;
  private String name;
  private Long creatorId;
  private String createdAt;
}
