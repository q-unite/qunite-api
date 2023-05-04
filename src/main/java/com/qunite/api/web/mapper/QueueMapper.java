package com.qunite.api.web.mapper;

import com.qunite.api.domain.Queue;
import com.qunite.api.web.dto.QueueDto;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface QueueMapper {
  @Mapping(source = "creatorId", target = "creator.id")
  Queue toEntity(QueueDto queueDto);

  @Mapping(source = "creator.id", target = "creatorId")
  @Mapping(target = "createdAt", dateFormat = "dd-MM-yyyy HH:mm:ss")
  QueueDto toDto(Queue queue);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(source = "creatorId", target = "creator.id")
  Queue partialUpdate(QueueDto queueDto, @MappingTarget Queue queue);
}