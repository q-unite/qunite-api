package com.qunite.api.web.mapper;

import com.qunite.api.domain.Queue;
import com.qunite.api.web.dto.queue.QueueCreationDto;
import com.qunite.api.web.dto.queue.QueueDto;
import com.qunite.api.web.dto.queue.QueueUpdateDto;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface QueueMapper {
  @Mapping(source = "creatorId", target = "creator.id")
  Queue toEntity(QueueCreationDto queueDto);

  @Mapping(source = "creator.id", target = "creatorId")
  @Mapping(target = "createdAt", dateFormat = "dd-MM-yyyy HH:mm:ss")
  QueueDto toDto(Queue queue);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  Queue partialUpdate(QueueUpdateDto queueDto, @MappingTarget Queue queue);
}
