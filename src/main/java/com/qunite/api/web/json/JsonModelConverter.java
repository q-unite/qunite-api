package com.qunite.api.web.json;

import java.util.Collection;
import org.springframework.hateoas.RepresentationModel;

public interface JsonModelConverter<T extends RepresentationModel<? extends T>> {
  RepresentationModel<T> toJsonApiModel(T entityDto, boolean includeAffordances);

  RepresentationModel<T> toJsonCollectionModel(Collection<T> entityDtos);

}
