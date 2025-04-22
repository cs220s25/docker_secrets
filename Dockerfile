FROM amazonlinux

WORKDIR /app

RUN yum install -y maven-amazon-corretto21

COPY pom.xml .
COPY src src

RUN mvn package

CMD ["java", "-jar", "target/secretsDemo-1.0.0-jar-with-dependencies.jar"]
