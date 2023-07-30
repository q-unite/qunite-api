package com.qunite.api.web.dto.entry;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class EntryUpdateDto {
  @PositiveOrZero(message = "Must be non-negative")
  private Integer entryIndex;
}
