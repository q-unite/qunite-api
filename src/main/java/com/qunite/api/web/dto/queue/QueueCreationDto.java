package com.qunite.api.web.dto.queue;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class QueueCreationDto {
  @NotBlank(message = "Specify name")
  private String name;
}