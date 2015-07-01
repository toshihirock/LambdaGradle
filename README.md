# LambdaGradle

# Overview

+ Java1.8
+ Gradle2.4
+ aws-lambda-java-core 1.0.0
+ aws-lambda-java-events:1.0.0
+ JUnit4.11

# Require

+ Java1.8 installed and set $JAVA_HOME

# build and test

```
$git clone https://github.com/toshihirock/LambdaGradle.git
$cd LambdaGradle
$./gradlew build
:compileJava
:processResources UP-TO-DATE
:classes
:jar
:assemble
:compileTestJava
:processTestResources UP-TO-DATE
:testClasses
:test
:check
:build

BUILD SUCCESSFUL
```

then you can find jar for AWS Lambda in **build/libs/LambdaGradle-0.0.1-SNAPSHOT.jar**
