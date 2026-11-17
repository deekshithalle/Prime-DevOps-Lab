FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY src ./src
RUN mkdir out && javac -d out src/*.java && java -cp out PrimeNumberTest
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/out ./out
COPY web ./web
USER 10001
EXPOSE 8081
CMD ["java", "-cp", "out", "PrimeServer"]
