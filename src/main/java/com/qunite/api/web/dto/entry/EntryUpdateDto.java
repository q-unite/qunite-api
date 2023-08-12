package com.qunite.api.web.dto.entry;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class EntryUpdateDto {
  @NotNull(message = "Specify position")
  @PositiveOrZero(message = "Must not be negative")
  private Integer entryIndex;
}
