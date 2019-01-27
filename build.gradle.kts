plugins {
    java
    `java-library`
}

group = "com.github.aesteve"
version = "0.0.1-SNAPSHOT"

val vertxVersion = "3.6.2"
val graphQlJavaVersion = "11.0"

tasks.withType<JavaCompile> {
    sourceCompatibility = JavaVersion.VERSION_1_8.toString()
}

repositories {
    mavenCentral()
}

dependencies {
    api("io.vertx:vertx-web:$vertxVersion")
    api("com.graphql-java:graphql-java:$graphQlJavaVersion")

    testImplementation("io.vertx:vertx-junit5:$vertxVersion")
    testImplementation("io.vertx:vertx-web-client:$vertxVersion")
}
