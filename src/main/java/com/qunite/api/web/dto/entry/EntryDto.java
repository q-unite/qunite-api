package com.qunite.api.web.dto.entry;

import java.io.Serializable;
import lombok.Data;

/**
 * A DTO for the {@link com.qunite.api.domain.Entry} entity
 */

@Data
public class EntryDto implements Serializable {
  private Long memberId;
  private Long queueId;
  private String createdAt;
  private Integer entryIndex;
}
