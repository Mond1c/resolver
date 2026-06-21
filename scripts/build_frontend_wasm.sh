#!/bin/bash

./gradlew :app:resolverControlWasm:shadowJar
./gradlew :app:resolverWasm:shadowJar