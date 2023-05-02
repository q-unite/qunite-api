package com.qunite.api.web.json;

import static com.qunite.api.web.json.JsonModelApiUtils.assemblyAffordanceToLink;
import static com.qunite.api.web.json.JsonModelApiUtils.toLink;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import com.qunite.api.web.controller.QueueController;
import com.qunite.api.web.controller.UserController;
import com.qunite.api.web.dto.QueueDto;
import com.toedter.spring.hateoas.jsonapi.JsonApiModelBuilder;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.javatuples.Triplet;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

@Component
public class QueueDtoJsonModelConverter implements JsonModelConverter<QueueDto> {

  @Override
  public RepresentationModel<QueueDto> toJsonApiModel(QueueDto entityDto, boolean affordances) {
    var target = toLink(methodOn(QueueController.class).getById(entityDto.getId()));
    Link selfLink = affordances
        ? assemblyAffordanceToLink(target, HttpMethod.DELETE, "Delete queue")
        : target;

    var builder =
        new AtomicReference<>(JsonApiModelBuilder.jsonApiModel().model(entityDto).link(selfLink));

    Optional.ofNullable(entityDto.getCreatorId())
        .ifPresent(creatorId ->
            builder.set(JsonModelApiUtils.builderRelationships(builder.get(),
                Triplet.with("creator",
                    toLink(methodOn(UserController.class).getById(creatorId)),
                    toLink(methodOn(QueueController.class).getQueueCreator(entityDto.getId()))),
                Triplet.with("managers",
                    toLink(methodOn(QueueController.class).getQueueManagers(entityDto.getId())),
                    null),
                Triplet.with("entries",
                    toLink(methodOn(QueueController.class).getQueueEntries(entityDto.getId())),
                    null))));

    @SuppressWarnings("unchecked")
    RepresentationModel<QueueDto> build = (RepresentationModel<QueueDto>) builder.get().build();
    return build;
  }

  @Override
  public RepresentationModel<QueueDto> toJsonCollectionModel(Collection<QueueDto> entityDtos) {
    Link selfLink = toLink(methodOn(QueueController.class).all());
    var modelContent = entityDtos.stream()
        .map(dto -> toJsonApiModel(dto, false))
        .toList();
    var builder = JsonApiModelBuilder.jsonApiModel()
        .model(CollectionModel.of(modelContent))
        .link(selfLink);

    @SuppressWarnings("unchecked")
    RepresentationModel<QueueDto> build = (RepresentationModel<QueueDto>) builder.build();
    return build;
  }

}