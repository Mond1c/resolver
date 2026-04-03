from eclipse-temurin:25-jdk-alpine as builder

workdir /resolver

copy . .

run chmod +x gradlew

run ./gradlew shadowJar -x test

from alpine:3.23

run apk add --no-cache openjdk25-jre=25.0.2_p10-r1

run addgroup -S appgroup && adduser -S appuser -G appgroup
user appuser

workdir /resolver

copy --from=builder /resolver/build/libs/*-all.jar resolver.jar
copy --from=builder /resolver/source ./source
expose 8080

entrypoint ["java", "-jar", "resolver.jar", "-c", "./source"]