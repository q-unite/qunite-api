package com.qunite.api.web.dto;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * A DTO for the {@link com.qunite.api.domain.Entry} entity
 */
@AllArgsConstructor
@Getter
public class EntryDto implements Serializable {
  private final Long memberId;
  private final Long queueId;
  private final String createdAt;
  private final Integer entryIndex;
}