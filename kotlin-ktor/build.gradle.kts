val postgresqlVersion = "42.7.5"
val jacksonVersion = "2.17.2"

kotlin {
  jvmToolchain(21)
  compilerOptions.freeCompilerArgs.addAll("-Xjsr305=strict")
}

plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.ktor)
  alias(libs.plugins.ktlint)
}

group = "se.callistaenterprise.scheduler"
version = "0.0.1"

repositories { // Sources of dependencies. See 1️⃣
  mavenCentral() // Maven Central Repository. See 2️⃣
}

application {
  mainClass = "io.ktor.server.netty.EngineMain"

  val isDevelopment: Boolean = project.ext.has("development")
  applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
  mavenCentral()
}

dependencies {
  implementation(libs.ktor.server.content.negotiation)
  implementation(libs.ktor.server.core)
  implementation(libs.ktor.serialization.json)
  implementation(libs.ktor.server.call.logging)
  implementation(libs.ktor.simple.cache)
  implementation(libs.ktor.simple.redis.cache)
  implementation(libs.ktor.server.openapi)
  implementation(libs.ktor.server.request.validation)
  implementation(libs.ktor.server.netty)
  implementation(libs.logback.classic)
  implementation(libs.ktor.server.config.yaml)

  implementation("org.postgresql:postgresql:42.7.5")
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")

  testImplementation(libs.ktor.server.test.host)
  testImplementation(libs.kotlin.test.junit)
}

tasks.register("databaseInstance") {
  doLast {
    val command = arrayOf("docker-compose", "up")
    Runtime.getRuntime().exec(command)
  }
}
