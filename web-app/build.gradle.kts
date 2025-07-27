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

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity6")
    implementation("nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect")
    implementation("io.github.wimdeblauwe:htmx-spring-boot:4.0.1")
    implementation("io.github.wimdeblauwe:htmx-spring-boot-thymeleaf:4.0.1")
//    developmentOnly("org.springframework.boot:spring-boot-devtools")

    // converting images to pdf, invoices etc.
    implementation("com.itextpdf:itext7-core:9.2.0")
    //implementation("org.springframework.boot:spring-boot-starter-validation")
    //implementation("org.springframework.session:spring-session-jdbc")

    // Test dependencies
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}