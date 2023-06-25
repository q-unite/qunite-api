package com.qunite.api;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(
    info = @Info(title = "QUnite API", version = "0.1",
        license = @License(name = "Apache-2.0 license",
            url = "https://www.apache.org/licenses/LICENSE-2.0")
    )
)
public class QuniteApiApplication {

  public static void main(String[] args) {
    SpringApplication.run(QuniteApiApplication.class, args);
  }

}
