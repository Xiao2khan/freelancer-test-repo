plugins {
    id("org.springframework.boot")
}

dependencies {
    implementation(project(":util"))
    implementation(project(":tenant"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework:spring-context")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // converting images to pdf, invoices etc.
    implementation("com.itextpdf:itext7-core:9.2.0")

    runtimeOnly("org.postgresql:postgresql")
}
