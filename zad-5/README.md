# HOW TO RUN?

## Run main application:
```java
mvn -DskipTests=true spring-boot:run
```

## Enable bootstrap:
```java
mvn -DskipTests=true -Dapp.bootstrap.enabled=true spring-boot:run
```

## Disable bootstrap:
```java
mvn -DskipTests=true -Dapp.bootstrap.enabled=false spring-boot:run
```


## Run Unit tests:
```java
mvn -f pom.xml test
```

## Run tests with code coverage:
```java
mvn verify
```

## Code Coverage Report
image: ![coverage report showing 84% coverage](markdown/coverage-report.png)

