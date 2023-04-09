package com.qunite.api;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest
class QuniteApiApplicationTests {

  @Test
  void contextLoads(ApplicationContext ctx) {
    assertThat(ctx).isNotNull();
  }

}
