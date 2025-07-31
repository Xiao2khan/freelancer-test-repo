plugins {
    id("org.springframework.boot")
}

dependencies {
    implementation(project(":company-lookup"))
    implementation(project(":util"))
    implementation(project(":tenant"))
    implementation(project(":company"))
}