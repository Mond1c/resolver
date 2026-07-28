#!/bin/bash

./gradlew :frontend-compose:resolverControlWasm:shadowJar
./gradlew :frontend-compose:resolverWasm:shadowJar