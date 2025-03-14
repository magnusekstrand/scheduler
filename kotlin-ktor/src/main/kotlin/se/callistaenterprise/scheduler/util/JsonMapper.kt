package se.callistaenterprise.scheduler.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

object JsonMapper {
    // automatically installs the Kotlin module
    val defaultMapper: ObjectMapper = jacksonObjectMapper()

    init {
        defaultMapper.configure(SerializationFeature.INDENT_OUTPUT, true)
        defaultMapper.registerModule(JavaTimeModule())
        defaultMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    }
}
