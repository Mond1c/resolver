from eclipse-temurin:25-jdk-alpine as builder

workdir /resolver

copy . .

run chmod +x gradlew

run ./gradlew shadowJar -x test --no-daemon

from alpine:3.23

run apk add --no-cache openjdk25-jre=25.0.2_p10-r1

run addgroup -S appgroup && adduser -S appuser -G appgroup
user appuser

workdir /resolver

copy --from=builder /resolver/build/libs/*-all.jar resolver.jar
copy --from=builder /resolver/source ./source

env port=8080
env host=0.0.0.0
env source=./source/1

expose ${port}

entrypoint ["sh", "-c", "java -jar resolver.jar -c ${source} -h ${host} -p ${port}"]