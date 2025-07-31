plugins {
    id("org.springframework.boot")
}

dependencies {
    implementation(project(":util"))
    implementation(project(":tenant"))

    implementation("com.itextpdf:itext7-core:9.2.0")
}
