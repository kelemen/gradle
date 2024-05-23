plugins {
    id("gradlebuild.distribution.api-java")
}

description = "A set of generic services and utilities to be used form workers. The reason to separate these from :base-services is because workers have a different Java requirement."

gradlebuildJava.usedForStartup()

dependencies {
    implementation(project(":core"))
}
