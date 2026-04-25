plugins {
    java
    id("java")
    id("application")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "org.teodor"
version = "1.0.0"
java.sourceCompatibility = JavaVersion.VERSION_21

tasks.processResources {
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

    val botToken = project.findProperty("BOT_TOKEN") as String?
    val adminId = project.findProperty("ADMIN_CHAT_ID") as String?
    val dbName = project.findProperty("DB_NAME") as String?
    val dbUser = project.findProperty("DB_USERNAME") as String?
    val dbPass = project.findProperty("DB_PASSWORD") as String?

    if (botToken != null) systemProperty("BOT_TOKEN", botToken)
    if (adminId != null) systemProperty("ADMIN_CHAT_ID", adminId)
    if (dbName != null) systemProperty("DB_NAME", dbName)
    if (dbUser != null) systemProperty("DB_USERNAME", dbUser)
    if (dbPass != null) systemProperty("DB_PASSWORD", dbPass)
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

tasks.register("stage") {
    dependsOn("clean", "shadowJar")

    doLast {
        val shadowJarFile = tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar").get().archiveFile.get().asFile
        val destination = file("${project.rootDir}/app.jar")

        shadowJarFile.copyTo(destination, overwrite = true)
        println("Copied ${shadowJarFile.name} to ${destination.absolutePath}")
    }
}

tasks.named("stage") {
    mustRunAfter("clean")
}

tasks.clean {
    doLast {
        val file = project.file("app.jar")
        if (file.exists()) {
            file.delete()
        }
    }
}