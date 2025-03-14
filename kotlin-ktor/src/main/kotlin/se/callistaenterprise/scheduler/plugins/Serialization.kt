package se.callistaenterprise.scheduler.plugins

import io.ktor.http.ContentType
import io.ktor.serialization.jackson.JacksonConverter
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import se.callistaenterprise.scheduler.util.JsonMapper.defaultMapper

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        register(ContentType.Application.Json, JacksonConverter(defaultMapper))
    }
}
