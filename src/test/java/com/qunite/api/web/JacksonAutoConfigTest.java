package com.qunite.api.web;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qunite.api.QuniteApiApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@SpringBootTest
@Import({AnnotationConfigApplicationContext.class, QuniteApiApplication.class})
public class JacksonAutoConfigTest {

  @Autowired
  private AnnotationConfigApplicationContext context;

  @Test
  public void defaultObjectMapperBuilder() throws Exception {
    this.context.register(JacksonAutoConfiguration.class);
    this.context.refresh();
    Jackson2ObjectMapperBuilder builder =
        this.context.getBean(Jackson2ObjectMapperBuilder.class);
    ObjectMapper mapper = builder.build();
    assertTrue(MapperFeature.DEFAULT_VIEW_INCLUSION.enabledByDefault());
    assertFalse(mapper.getDeserializationConfig().isEnabled(
        MapperFeature.DEFAULT_VIEW_INCLUSION));
    assertFalse(mapper.getSerializationConfig().isEnabled(
        MapperFeature.DEFAULT_VIEW_INCLUSION));
  }
}
