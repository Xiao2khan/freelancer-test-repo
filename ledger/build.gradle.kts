plugins {
    id("org.springframework.boot")
}

dependencies {
    implementation(project(":util"))
    implementation(project(":tenant"))
    implementation(project(":attachment"))

    implementation("org.yaml:snakeyaml")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")
}