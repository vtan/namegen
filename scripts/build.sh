#!/bin/bash
set -e

rm -rf target/build
mkdir -p target/build/data target/build/public
yarn build &
sbt assembly &
wait
rm -rf target/dist/*.map
cp data/*.csv target/build/data/
cp src/main/public/* target/dist/* target/build/public/
cp target/scala-2.13/namegen-assembly-0.1.jar target/build/
