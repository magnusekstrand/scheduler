package se.callistaenterprise.scheduler

import io.ktor.server.application.Application
import io.ktor.server.config.yaml.YamlConfig
import se.callistaenterprise.scheduler.plugins.configureMonitoring
import se.callistaenterprise.scheduler.plugins.configureRouting
import se.callistaenterprise.scheduler.plugins.configureSerialization
import java.sql.Connection
import java.sql.DriverManager

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSerialization()
    configureMonitoring()
    configureRouting(dbConnection = connectToPostgres(embedded = false))
}

private fun Application.connectToPostgres(embedded: Boolean): Connection {
    Class.forName("org.postgresql.Driver")
    if (embedded) {
        return DriverManager.getConnection("jdbc:postgresql://localhost/test;DB_CLOSE_DELAY=-1", "root", "")
    } else {
        val configs = YamlConfig("postgres.yaml")
        val url =
            "jdbc:postgresql://localhost:5432/" +
                configs?.property("services.postgres.environment.POSTGRES_DB")?.getString()
        val user = configs?.property("services.postgres.environment.POSTGRES_USER")?.getString()
        val password = configs?.property("services.postgres.environment.POSTGRES_PASSWORD")?.getString()
        return DriverManager.getConnection(url, user, password)
    }
}
