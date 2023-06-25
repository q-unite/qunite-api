package com.qunite.api.web.mapper;

import com.qunite.api.domain.Entry;
import com.qunite.api.web.dto.entry.EntryDto;
import org.mapstruct.BeanMapping;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EntryMapper {
  @Mapping(source = "queueId", target = "queue.id")
  @Mapping(source = "memberId", target = "member.id")
  Entry toEntity(EntryDto entryDto);

  @InheritInverseConfiguration(name = "toEntity")
  @Mapping(target = "createdAt", dateFormat = "dd-MM-yyyy HH:mm:ss")
  EntryDto toDto(Entry entry);

  @InheritConfiguration(name = "toEntity")
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  Entry partialUpdate(
      EntryDto entryDto, @MappingTarget Entry entry);
}
