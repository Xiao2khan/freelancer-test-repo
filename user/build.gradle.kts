plugins {
    id("org.springframework.boot")
}

dependencies {
    implementation(project(":util"))
    implementation(project(":tenant"))

    implementation("org.springframework.boot:spring-boot-starter-security")
}
