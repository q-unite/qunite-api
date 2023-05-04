package com.qunite.api.web.json;

import com.qunite.api.web.dto.UserDto;
import java.util.Collection;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.stereotype.Component;

// TODO: 03.05.2023 implement model converter
@Component
public class UserDtoJsonModelConverter implements JsonModelConverter<UserDto> {
  @Override
  public RepresentationModel<UserDto> toJsonApiModel(UserDto entityDto,
                                                     boolean includeAffordances) {
    return null;
  }

  @Override
  public RepresentationModel<UserDto> toJsonCollectionModel(Collection<UserDto> entityDtos) {
    return null;
  }

}
