package se.callistaenterprise.scheduler.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import se.callistaenterprise.scheduler.exceptions.DbElementInsertException
import se.callistaenterprise.scheduler.exceptions.DbElementNotFoundException
import se.callistaenterprise.scheduler.model.Meeting
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.time.LocalDate

object MeetingQueries {
    const val SELECT_ALL_MEETINGS = "SELECT * FROM meetings ORDER BY date, start"
    const val SELECT_MEETING_BY_ID = "SELECT * FROM meetings WHERE id = ?"
    const val SELECT_MEETING_BY_DATE = "SELECT * FROM meetings WHERE date = ?"
    const val INSERT_MEETING = "INSERT INTO meetings (title, date, start, \"end\") VALUES (?, ?, ?, ?)"
    const val UPDATE_MEETING = "UPDATE meetings SET title = ?, date = ?, start = ?, \"end\" = ? WHERE id = ?"
    const val DELETE_MEETING = "DELETE FROM meetings WHERE id = ?"
}

class MeetingRepository(private val connection: Connection) {
    // Create new Meeting
    suspend fun create(meeting: Meeting): Long =
        withContext(Dispatchers.IO) {
            useUpdate(
                MeetingQueries.INSERT_MEETING,
                listOf(
                    meeting.title,
                    toSqlDate(meeting.date),
                    toSqlTime(meeting.start),
                    toSqlTime(meeting.end),
                ),
            ) { statement ->
                val generatedKeys = statement.generatedKeys
                if (generatedKeys.next()) {
                    generatedKeys.getLong(1)
                } else {
                    throw DbElementInsertException("Unable to retrieve the id of the newly inserted Meeting")
                }
            }
        }

    // Generalized Read: All Meetings or by Date
    suspend fun read(
        query: String,
        parameter: Any? = null,
    ): List<Meeting> =
        withContext(Dispatchers.IO) {
            useQuery(query, listOfNotNull(parameter)) { resultSet ->
                generateSequence {
                    if (resultSet.next()) resultSetToMeeting(resultSet) else null
                }.toList()
            }
        }

    suspend fun readAll(): List<Meeting> = read(MeetingQueries.SELECT_ALL_MEETINGS)

    suspend fun read(date: LocalDate): List<Meeting> = read(MeetingQueries.SELECT_MEETING_BY_DATE, toSqlDate(date))

    // Read a single Meeting by Id
    suspend fun read(id: Long): Meeting =
        withContext(Dispatchers.IO) {
            useQuery(MeetingQueries.SELECT_MEETING_BY_ID, listOf(id)) { resultSet ->
                if (resultSet.next()) {
                    resultSetToMeeting(resultSet)
                } else {
                    throw DbElementNotFoundException("Record not found for ID: $id")
                }
            }
        }

    // Update a Meeting
    suspend fun update(
        id: Long,
        meeting: Meeting,
    ) = withContext(Dispatchers.IO) {
        useUpdate(
            MeetingQueries.UPDATE_MEETING,
            listOf(
                meeting.title,
                toSqlDate(meeting.date),
                toSqlTime(meeting.start),
                toSqlTime(meeting.end),
                id,
            ),
        ) {}
    }

    // Delete a Meeting
    suspend fun delete(id: Long) =
        withContext(Dispatchers.IO) {
            useUpdate(MeetingQueries.DELETE_MEETING, listOf(id)) {}
        }

    // Utility to Abstract Query Execution with Resource Management
    private suspend fun <T> useQuery(
        query: String,
        parameters: List<Any>,
        block: (ResultSet) -> T,
    ): T =
        connection.prepareStatement(query).use { statement ->
            setParameters(statement, parameters)
            statement.executeQuery().use(block)
        }

    // Utility to Abstract Update Execution with Resource Management
    private suspend fun <T> useUpdate(
        query: String,
        parameters: List<Any>,
        block: (PreparedStatement) -> T,
    ): T =
        connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS).use { statement ->
            setParameters(statement, parameters)
            statement.executeUpdate()
            block(statement)
        }

    private fun setParameters(
        statement: PreparedStatement,
        parameters: List<Any>,
    ) {
        parameters.forEachIndexed { index, parameter ->
            when (parameter) {
                is String -> statement.setString(index + 1, parameter)
                is java.sql.Date -> statement.setDate(index + 1, parameter)
                is java.sql.Time -> statement.setTime(index + 1, parameter)
                is Long -> statement.setLong(index + 1, parameter)
            }
        }
    }

    // Mapping ResultSet to Meeting
    private fun resultSetToMeeting(resultSet: ResultSet): Meeting {
        val id = resultSet.getLong("id")
        val title = resultSet.getString("title")
        val date = resultSet.getDate("date").toLocalDate()
        val start = resultSet.getTime("start").toLocalTime()
        val end = resultSet.getTime("end").toLocalTime()
        return Meeting(id, title, date, start, end)
    }

    private fun toSqlDate(date: LocalDate): java.sql.Date = java.sql.Date.valueOf(date)

    private fun toSqlTime(time: java.time.LocalTime): java.sql.Time = java.sql.Time.valueOf(time)
}
