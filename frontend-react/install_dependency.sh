#!/bin/bash

git clone --depth 1 $1

for item in ./live-v3/src/frontend/*; do
  if [ "$(basename "$item")" != "build.gradle.kts" ]; then
    mv "$item" .
  fi
done

rm -rf ./live-v3