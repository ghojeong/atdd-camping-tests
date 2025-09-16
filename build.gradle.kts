plugins {
    java
}

group = "com.camping"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

// Versions
val cucumberVersion = "7.14.0"
val restAssuredVersion = "5.3.2"
val jacksonVersion = "2.17.2"

dependencies {
    // Cucumber
    testImplementation("io.cucumber:cucumber-java:$cucumberVersion")
    testImplementation("io.cucumber:cucumber-junit-platform-engine:$cucumberVersion")

    // RestAssured
    testImplementation("io.rest-assured:rest-assured:${restAssuredVersion}")
    testImplementation("com.fasterxml.jackson.core:jackson-databind:${jacksonVersion}")

    // JUnit Jupiter
    testImplementation("org.junit.platform:junit-platform-suite:1.10.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.10.0")
    testRuntimeOnly("org.junit.platform:junit-platform-suite-engine:1.10.0")

    // JDBC driver for test hooks
    testImplementation("com.mysql:mysql-connector-j:8.3.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.register<Exec>("infraUp") {
    group = "infra"
    description = "Start infrastructure (database with data initialization)"
    commandLine(
        "docker", "compose",
        "-f", "infra/docker-compose-infra.yml",
        "up", "-d"
    )
}

tasks.register<Exec>("infraDown") {
    group = "infra"
    description = "Stop infrastructure and remove volumes"
    commandLine(
        "docker", "compose",
        "-f", "infra/docker-compose-infra.yml",
        "down", "-v"
    )
}

tasks.register<Exec>("appsUp") {
    group = "infra"
    description = "Start application services (depends on infrastructure)"
    dependsOn("infraUp")
    mustRunAfter("infraUp")
    commandLine(
        "docker", "compose",
        "-f", "infra/docker-compose.yml",
        "up", "-d", "--build"
    )
    doFirst {
        // Wait for infrastructure to be ready
        Thread.sleep(10000)
    }
}

tasks.register<Exec>("appsDown") {
    group = "infra"
    description = "Stop application services"
    commandLine(
        "docker", "compose",
        "-f", "infra/docker-compose.yml",
        "down", "-v"
    )
}

tasks.register("allUp") {
    group = "infra"
    description = "Start infrastructure and all application services"
    dependsOn("infraUp", "appsUp")
    mustRunAfter("infraUp")
}

tasks.register("allDown") {
    group = "infra"
    description = "Stop all services and remove volumes"
    dependsOn("appsDown", "infraDown")
}

tasks.register<Exec>("appsLogs") {
    group = "infra"
    description = "Show application services logs"
    commandLine(
        "docker", "compose",
        "-f", "infra/docker-compose.yml",
        "logs", "-f"
    )
}

tasks.register("cloneRepos") {
    group = "setup"
    description = "Clone all required repositories"
    dependsOn("cloneKiosk", "cloneAdmin", "cloneReservation")
}

tasks.register<Exec>("cloneKiosk") {
    group = "setup"
    description = "Clone kiosk repository"
    doFirst {
        delete("repos/atdd-camping-kiosk")
        mkdir("repos")
    }
    commandLine(
        "git", "clone",
        "--branch", "main",
        "--single-branch",
        "--depth", "1",
        "https://github.com/next-step/atdd-camping-kiosk.git",
        "repos/atdd-camping-kiosk"
    )
    doLast {
        copy {
            from("infra/configs/kiosk-application.yml")
            into("repos/atdd-camping-kiosk/src/main/resources")
            rename { "application.yml" }
        }
        copy {
            from("infra/configs/kiosk-build.gradle")
            into("repos/atdd-camping-kiosk")
            rename { "build.gradle" }
        }
    }
}

tasks.register<Exec>("cloneAdmin") {
    group = "setup"
    description = "Clone admin repository"
    doFirst {
        delete("repos/atdd-camping-admin")
        mkdir("repos")
    }
    commandLine(
        "git", "clone",
        "--branch", "main",
        "--single-branch",
        "--depth", "1",
        "https://github.com/next-step/atdd-camping-admin.git",
        "repos/atdd-camping-admin"
    )
    doLast {
        copy {
            from("infra/configs/admin-application.yml")
            into("repos/atdd-camping-admin/src/main/resources")
            rename { "application.yml" }
        }
        copy {
            from("infra/configs/admin-build.gradle")
            into("repos/atdd-camping-admin")
            rename { "build.gradle" }
        }
    }
}

tasks.register<Exec>("cloneReservation") {
    group = "setup"
    description = "Clone reservation repository"
    doFirst {
        delete("repos/atdd-camping-reservation")
        mkdir("repos")
    }
    commandLine(
        "git", "clone",
        "--branch", "main",
        "--single-branch",
        "--depth", "1",
        "https://github.com/next-step/atdd-camping-reservation.git",
        "repos/atdd-camping-reservation"
    )
    doLast {
        copy {
            from("infra/configs/reservation-application.yml")
            into("repos/atdd-camping-reservation/src/main/resources")
            rename { "application.yml" }
        }
        copy {
            from("infra/configs/reservation-build.gradle")
            into("repos/atdd-camping-reservation")
            rename { "build.gradle" }
        }
    }
}
