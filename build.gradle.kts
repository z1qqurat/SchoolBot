plugins {
    java
    id("java")
    id("application")
}

group = "org.teodor"
version = "1.0.0"
java.sourceCompatibility = JavaVersion.VERSION_21

//java {
//    toolchain {
//        languageVersion.set(JavaLanguageVersion.of(21))
//    }
//}

tasks.processResources {
    val botToken = project.findProperty("BOT_TOKEN") as String?
    val adminChatId = project.findProperty("ADMIN_CHAT_ID") as String?
    val dbName = project.findProperty("DB_NAME") as String?
    val dbUsername = project.findProperty("DB_USERNAME") as String?
    val dbPassword = project.findProperty("DB_PASSWORD") as String?

    inputs.properties(
        mapOf(
            "BOT_TOKEN" to (botToken ?: ""),
            "ADMIN_CHAT_ID" to (adminChatId ?: ""),
            "DB_NAME" to (dbName ?: ""),
            "DB_USERNAME" to (dbUsername ?: ""),
            "DB_PASSWORD" to (dbPassword ?: "")
        )
    )

    filesMatching("**/*.properties") {
        expand(
            mapOf(
                "BOT_TOKEN" to (botToken ?: ""),
                "ADMIN_CHAT_ID" to (adminChatId ?: ""),
                "DB_NAME" to (dbName ?: ""),
                "DB_USERNAME" to (dbUsername ?: ""),
                "DB_PASSWORD" to (dbPassword ?: "")
            )
        )
    }
}


application {
    mainClass.set("org.teodor.Application")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "org.teodor.Application"
    }
}

val copyDependencies by tasks.registering(Copy::class) {
    from(configurations.runtimeClasspath)
    into(layout.buildDirectory.dir("lib"))
}

tasks.build {
    dependsOn(copyDependencies)
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")

    implementation("org.jsoup:jsoup:1.21.2")

    implementation("io.rest-assured:rest-assured:5.5.6")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.4.2")
    implementation("jakarta.xml.bind:jakarta.xml.bind-api:4.0.2")
    implementation("org.glassfish.jaxb:jaxb-runtime:4.0.5")

    implementation("org.telegram:telegrambots-longpolling:9.2.0")
    implementation("org.telegram:telegrambots-abilities:9.2.0")
    implementation("org.telegram:telegrambots-client:9.2.0")
    implementation("com.vdurmont:emoji-java:5.1.1")

    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.24.3")
    implementation("org.apache.logging.log4j:log4j-core:2.24.3")

    testImplementation("io.rest-assured:rest-assured:3.0.0")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testCompileOnly("org.projectlombok:lombok:1.18.42")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.42")

    implementation("org.postgresql:postgresql:42.7.8")
}

tasks.test {
    useJUnitPlatform()
}

tasks.register<Copy>("stage") {
    dependsOn("clean", "build")

    from(tasks.jar.get().archiveFile)
    into(project.rootDir)
    rename { "app.jar" }
}

tasks.named("stage") {
    mustRunAfter("clean")
}

tasks.clean {
    doLast {
        project.file("app.jar").delete()
    }
}