package com.qunite.api.web.dto;

import com.toedter.spring.hateoas.jsonapi.JsonApiId;
import com.toedter.spring.hateoas.jsonapi.JsonApiTypeForClass;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;

/**
 * A DTO for the {@link com.qunite.api.domain.Entry} entity
 */
@AllArgsConstructor
@Getter
@JsonApiTypeForClass("entries")
public class EntryDto extends RepresentationModel<EntryDto> implements Serializable {
  @JsonApiId
  private final Long memberId;
  private final Long queueId;
  private final String createdAt;
  private final Integer entryIndex;
}