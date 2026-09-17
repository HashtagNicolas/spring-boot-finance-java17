#!/bin/bash
set -x
ARTIFACTS=(
  "org.mapstruct:mapstruct:1.6.3"
  "org.mapstruct:mapstruct-processor:1.6.3"
  "io.jsonwebtoken:jjwt-api:0.12.6"
  "io.jsonwebtoken:jjwt-impl:0.12.6"
  "io.jsonwebtoken:jjwt-jackson:0.12.6"
  "io.cucumber:cucumber-java:7.20.1"
  "io.cucumber:cucumber-spring:7.20.1"
  "io.cucumber:cucumber-junit-platform-engine:7.20.1"
  "com.tngtech.archunit:archunit-junit5:1.3.0"
  "org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.5"
)
for a in "${ARTIFACTS[@]}"; do
  echo "=== $a ==="
  mvn -q dependency:get -Dartifact="$a"
  echo "result=$?"
done
