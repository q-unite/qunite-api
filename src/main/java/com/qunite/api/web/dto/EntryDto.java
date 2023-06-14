package com.qunite.api.web.dto;

import com.fasterxml.jackson.annotation.JsonView;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * A DTO for the {@link com.qunite.api.domain.Entry} entity
 */
@AllArgsConstructor
@Getter
@Setter
public class EntryDto implements Serializable {

  @JsonView(Views.Patch.class)
  private Long memberId;

  @JsonView(Views.Patch.class)
  private Long queueId;
  private String createdAt;

  @JsonView(Views.Patch.class)
  private Integer entryIndex;
}