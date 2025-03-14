package se.callistaenterprise.scheduler.plugins

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.plugins.requestvalidation.RequestValidation
import io.ktor.server.plugins.requestvalidation.ValidationResult.Invalid
import io.ktor.server.plugins.requestvalidation.ValidationResult.Valid
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import se.callistaenterprise.scheduler.model.Meeting
import se.callistaenterprise.scheduler.service.MeetingRepository
import se.callistaenterprise.scheduler.service.MeetingService
import java.sql.Connection
import java.time.LocalDate
import java.time.format.DateTimeParseException

fun Application.configureRouting(dbConnection: Connection) {
    install(RequestValidation) {
        validate<Meeting> { meeting ->
            if (meeting.title.isBlank()) Invalid("Title cannot be blank") else Valid
        }
    }

    val meetingService = MeetingService(MeetingRepository(dbConnection))

    routing {
        // Health check endpoint
        get { call.response.status(HttpStatusCode.OK) }

        route("/api/scheduler/meetings") {
            get {
                handleRequest {
                    val meetings = meetingService.all()
                    call.respond(HttpStatusCode.OK, meetings)
                }
            }

            get("/{id}") {
                handleRequest {
                    val id = call.extractId()
                    val meeting = meetingService.findById(id)
                    call.respondWith(meeting, HttpStatusCode.OK, HttpStatusCode.NotFound)
                }
            }

            get("/find") {
                handleRequest {
                    val (date, duration) = call.extractDateParams()
                    val meetings = meetingService.findIntervals(date, duration)
                    call.respondWith(meetings, HttpStatusCode.OK, HttpStatusCode.NotFound)
                }
            }

            post {
                handleRequest {
                    val meeting = call.receive<Meeting>()
                    val createdMeeting = meetingService.add(meeting)
                    call.respondWith(createdMeeting, HttpStatusCode.Created, HttpStatusCode.InternalServerError)
                }
            }

            put("/{id}") {
                handleRequest {
                    val id = call.extractId()
                    val meeting = call.receive<Meeting>()
                    meetingService.update(id, meeting)
                    call.response.status(HttpStatusCode.OK)
                }
            }

            delete("/{id}") {
                handleRequest {
                    val id = call.extractId()
                    meetingService.delete(id)
                    call.response.status(HttpStatusCode.OK)
                }
            }
        }
    }
}

private suspend fun RoutingContext.handleRequest(block: suspend () -> Unit) {
    try {
        block()
    } catch (e: IllegalArgumentException) {
        call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
    } catch (e: DateTimeParseException) {
        call.respond(HttpStatusCode.BadRequest, "Invalid date format. Use yyyy-mm-dd")
    }
}

private suspend inline fun <reified T> ApplicationCall.respondWith(
    result: T?,
    onSuccessStatus: HttpStatusCode,
    onFailureStatus: HttpStatusCode,
) {
    result?.let { respond(onSuccessStatus, it) } ?: response.status(onFailureStatus)
}

private fun ApplicationCall.extractId(): Long = parameters["id"]?.toLong() ?: throw IllegalArgumentException("Invalid meeting ID")

private fun ApplicationCall.extractDateParams(): Pair<LocalDate, Long> {
    val dateParam =
        request.queryParameters["date"]
            ?: throw IllegalArgumentException("Missing date parameter")
    val durationParam =
        request.queryParameters["duration"]?.toLong()
            ?: throw IllegalArgumentException("Missing or invalid duration parameter")
    val date =
        try {
            LocalDate.parse(dateParam)
        } catch (e: DateTimeParseException) {
            throw IllegalArgumentException("Invalid date format. Use yyyy-mm-dd")
        }
    return date to durationParam
}
