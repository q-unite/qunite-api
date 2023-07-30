package com.qunite.api.web.dto.queue;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class QueueUpdateDto {
  @NotBlank(message = "Specify name")
  private String name;
}
