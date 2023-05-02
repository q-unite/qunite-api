package com.qunite.api.web.json;

import com.qunite.api.web.dto.UserDto;
import java.util.Collection;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.stereotype.Component;

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
