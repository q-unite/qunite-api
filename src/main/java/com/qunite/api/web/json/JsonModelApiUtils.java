package com.qunite.api.web.json;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

import com.toedter.spring.hateoas.jsonapi.JsonApiModelBuilder;
import java.util.Arrays;
import java.util.Optional;
import lombok.experimental.UtilityClass;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mediatype.Affordances;
import org.springframework.http.HttpMethod;

@UtilityClass
public class JsonModelApiUtils {

  public static Link assemblyAffordanceToLink(Link targetLink, HttpMethod httpMethod, String name) {
    return Affordances.of(targetLink)
        .afford(httpMethod)
        .withName(name)
        .toLink();
  }

  @SafeVarargs
  public static Link assemblyAffordancesToLink(Link targetLink, Pair<HttpMethod, String>... pairs) {
    return Arrays.stream(pairs)
        .reduce(targetLink,
            (link, pair) -> assemblyAffordanceToLink(link, pair.getValue0(), pair.getValue1()),
            (a, b) -> b);
  }


  public static Link toLink(Object controllerMethod) {
    return linkTo(controllerMethod).withSelfRel();
  }

  private Pair<String, String> mapLinksToStringPair(Link selfMethodLink,
                                                    Link relatedMethodLink) {
    return Optional.ofNullable(selfMethodLink)
        .map(Link::getHref)
        .map(selfHref -> Optional.ofNullable(relatedMethodLink)
            .map(Link::getHref)
            .map(relatedHref -> Pair.with(selfHref, relatedHref))
            .orElse(Pair.with(selfHref, null)))
        .orElse(Pair.with(null, null));
  }

  @SafeVarargs
  public static JsonApiModelBuilder builderRelationships(JsonApiModelBuilder builder,
                                                         Triplet<String, Link, Link>... triplets) {
    return Arrays.stream(triplets)
        .map(triplet -> {
          var pair = mapLinksToStringPair(triplet.getValue1(), triplet.getValue2());
          return builder.relationship(triplet.getValue0(), pair.getValue0(), pair.getValue1(),
              null);
        })
        .reduce(builder, (t1, t2) -> t2, (builder1, builder2) -> builder2);
  }

}
