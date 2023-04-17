# QUnite API

## Checkstyle

Our code convention is fully compliant with Google Checks specifications. 
Read more [here](https://google.github.io/styleguide/javaguide.html).

To configure this code convention in your IDE, 
use the Checkstyle plugin and the [config file](config/checkstyle.xml).

To run checkstyle, execute `mvn clean verify -P checkstyle`

## Testing

Application has other [configuration](src/main/resources/application-test.properties) for testing.\
To use this config, mark all test classes with `@ActiveProfiles("test")`

To write **integration** tests, use `@IntegrationTest`. There are two options:
- mark test class with this annotation
- make test class implement interface or extend class, marked with this annotation.

To run **integration** tests, follow next steps:
1. install and run Docker on your local machine
2. execute `mvn clean verify -P integration-test`