plugins {
    id("org.springframework.boot")
}

dependencies {
    implementation(project(":company-lookup"))
    implementation(project(":util"))
    implementation(project(":user"))
    implementation(project(":tenant"))
    implementation(project(":company"))
    implementation(project(":ledger"))
    implementation(project(":contact"))
    implementation(project(":bank"))
    implementation(project(":attachment"))

    // Spring boot dependencies
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-web")
    //developmentOnly("org.springframework.boot:spring-boot-devtools")

    // Thymeleaf and HTMX support
    implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity6")
    implementation("nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect")
    implementation("io.github.wimdeblauwe:htmx-spring-boot:4.0.1")
    implementation("io.github.wimdeblauwe:htmx-spring-boot-thymeleaf:4.0.1")

    // Database & Migration dependencies
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    runtimeOnly("org.postgresql:postgresql")

    // Test dependencies
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}