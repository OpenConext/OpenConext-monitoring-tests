FROM ghcr.io/openconext/openconext-basecontainers/java21:latest
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-XX:MaxRAMPercentage=75","-XX:+HeapDumpOnOutOfMemoryError","-XX:HeapDumpPath=/tmp","-XX:+ExitOnOutOfMemoryError","-jar","/app.jar"]
