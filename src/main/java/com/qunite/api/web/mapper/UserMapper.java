package com.qunite.api.web.mapper;

import com.qunite.api.domain.User;
import com.qunite.api.web.dto.user.UserCreationDto;
import com.qunite.api.web.dto.user.UserDto;
import com.qunite.api.web.dto.user.UserUpdateDto;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

  User toEntity(UserCreationDto userDto);

  UserDto toDto(User user);

  UserCreationDto toUserCreationDto(User user);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  User partialUpdate(UserUpdateDto userDto, @MappingTarget User user);
}
