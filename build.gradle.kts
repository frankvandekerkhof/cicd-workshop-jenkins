import com.moowork.gradle.node.npm.NpmTask

plugins {
    java
    idea
    checkstyle

    id("org.springframework.boot") version "2.3.0.RELEASE"
    id("io.spring.dependency-management") version "1.0.9.RELEASE"
    id("com.moowork.node") version "1.3.1"
    id("com.github.spotbugs") version "4.2.3"
    id("com.heroku.sdk.heroku-gradle") version "1.0.4"
}

version = "1.0"

repositories {
    mavenCentral()
}

val lombokVersion = "1.18.12"
val junitVersion = "5.3.1"
val frontendDir = "$projectDir/src/main/webapp"

dependencies {
    // Application dependencies
    implementation("org.springframework.boot:spring-boot-starter-web")
    compileOnly("org.projectlombok:lombok:$lombokVersion")
    annotationProcessor("org.projectlombok:lombok:$lombokVersion")

    // Test dependencies
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage")
    }
    testCompileOnly("org.projectlombok:lombok:$lombokVersion")
    testAnnotationProcessor("org.projectlombok:lombok:$lombokVersion")
}

tasks {
    // Configure task  'processResources'
    test {
        useJUnitPlatform()
    }

    // Configure task  'processResources'
    processResources {
        dependsOn("frontendCopy")
    }

    // Configure task  'bootJar'
    bootJar {
        mainClassName = "com.jcore.cicd.helloworld.HelloworldApplication"
    }

    // Configure task  'checkstyle'
    checkstyle {
        configFile = File("checkstyle.xml")
        toolVersion = "8.32"
    }

    // Create a new task named `buildAngular`
    val buildAngular = register<NpmTask>("buildAngular") {
        dependsOn("installAngular")

        // Set input and output so Gradle can
        // determine if task is up-to-date.
        inputs.dir("${frontendDir}/e2e")
        inputs.dir("${frontendDir}/src")
        inputs.dir("${frontendDir}/node_modules")
        outputs.dir("${frontendDir}/dist")

        setArgs(listOf("run-script", "build"))

        setWorkingDir(frontendDir)
    }

    // Create a new task named `buildAngular`
    register<NpmTask>("installAngular") {
        // Set input and output so Gradle can
        // determine if task is up-to-date.
        inputs.file("${frontendDir}/package.json")
        outputs.dir("${frontendDir}/node_modules")

        setArgs(listOf("install"))
        setWorkingDir(frontendDir)
    }

    // Create a new task names `frontendCopy`
    register<Copy>("frontendCopy") {
        from(buildAngular)
        into("${sourceSets.main.get().output.resourcesDir}/static")
    }
    
}

heroku {
    val bootJar: org.springframework.boot.gradle.tasks.bundling.BootJar by tasks
    appName = "jcore-cicd-demo"
    includes = listOf("${project.buildDir}/libs/${bootJar.archiveFileName.get()}")
    includeBuildDir = false
    includeRootDir = project.projectDir
    processTypes = mapOf(
            "web" to "java \$JVM_OPTS -jar build/libs/${bootJar.archiveFileName.get()} --server.port=\$PORT --add-opens java.base/java.lang=ALL-UNNAMED"
    )
}
