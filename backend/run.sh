#!/bin/bash
export JAVA_HOME=/usr/lib/jvm/java-25-openjdk
cd "$(dirname "$0")"
./mvnw clean spring-boot:run
