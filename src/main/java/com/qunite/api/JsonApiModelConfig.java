package com.qunite.api;

import com.toedter.spring.hateoas.jsonapi.JsonApiConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JsonApiModelConfig {
  @Bean
  JsonApiConfiguration jsonApiConfiguration() {
    return new JsonApiConfiguration()
        .withAffordancesRenderedAsLinkMeta(JsonApiConfiguration.AffordanceType.SPRING_HATEOAS)
        .withJsonApi11LinkPropertiesRemovedFromLinkMeta(false);
  }
}
