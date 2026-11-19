FROM eclipse-temurin:25-jdk-jammy AS builder

RUN apt-get update && \
    apt-get install -y maven && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY pom.xml ./
RUN mvn dependency:resolve -B

COPY src ./src

RUN mvn clean package -Dmaven.test.skip=true


FROM eclipse-temurin:25.0.5_8-jre-noble@sha256:3c6d7b40c2e78d4a8f4fe00af8165c706b34faf672dc21ff0924e583559ec6c2

RUN apt-get update && \
    apt-get install -y curl && \
    apt-get install -y --only-upgrade perl-base perl perl-modules-5.38 libperl5.38t64 && \
    dpkg --compare-versions "$(dpkg-query -W -f='${Version}' perl-base)" ge 5.38.2-3.2ubuntu0.4 && \
    rm -rf /var/lib/apt/lists/*

RUN groupadd -r millete -g 1001 && \
    useradd -r -g millete -u 1001 millete

WORKDIR /app

COPY --from=builder --chown=millete:millete /app/target/*.jar app.jar

USER millete

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
