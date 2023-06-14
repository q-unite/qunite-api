package com.qunite.api.web.dto;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * A DTO for the {@link com.qunite.api.domain.Entry} entity
 */
@AllArgsConstructor
@Data
public class EntryDto implements Serializable {
  private Long memberId;
  private Long queueId;
  private String createdAt;
  private Integer entryIndex;
}
