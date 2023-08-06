package com.qunite.api.web.dto.queue;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class QueueUpdateDto {
  @NotBlank(message = "Specify name")
  @Schema(example = "new_name")
  private String name;
}
