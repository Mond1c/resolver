#!/bin/bash

git clone $1

mv ./live-v3/src/frontend/* .

rm -rf ./live-v3

rm -f ./build.gradle.kts