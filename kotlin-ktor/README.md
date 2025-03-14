# scheduler

This project was created using the [Ktor Project Generator](https://start.ktor.io).

Here are some useful links to get you started:

- [Ktor Documentation](https://ktor.io/docs/home.html)
- [Ktor GitHub page](https://github.com/ktorio/ktor)
- The [Ktor Slack chat](https://app.slack.com/client/T09229ZC6/C0A974TJ9). You'll need to [request an invite](https://surveys.jetbrains.com/s3/kotlin-slack-sign-up) to join.

## Features

Here's a list of features included in this project:

| Name                                                               | Description                                                                        |
|--------------------------------------------------------------------|------------------------------------------------------------------------------------|
| [Content Negotiation](https://start.ktor.io/p/content-negotiation) | Provides automatic content conversion according to Content-Type and Accept headers |
| [Routing](https://start.ktor.io/p/routing)                         | Provides a structured routing DSL                                                  |
| [Jackson](https://start.ktor.io/p/ktor-jackson)                    | Handles JSON serialization using Jackson library                                   |
| [Call Logging](https://start.ktor.io/p/call-logging)               | Logs client requests                                                               |
| [Request Validation](https://start.ktor.io/p/request-validation)   | Adds validation for incoming requests                                              |

## Building & Running

1. Execute gradle task `databaseInstance` and wait until Docker Compose builds image and starts container

1. To build or run the project, use one of the following tasks:

| Task              | Description                                                          |
|-------------------|----------------------------------------------------------------------|
| `./gradlew test`  | Run the tests                                                        |
| `./gradlew build` | Build everything                                                     |
| `./gradlew run`   | Run the server                                                       |

If the server starts successfully, you'll see the following output:

```
2024-12-04 14:32:45.584 [main] INFO  Application - Application started in 0.303 seconds.
2024-12-04 14:32:45.682 [main] INFO  Application - Responding at http://0.0.0.0:8080
```

## Sample requests

### Add meetings

curl -s -X POST -H "Accept: application/json" -H "Content-Type: application/json" --data \
'{"id":null,"title":"Team Standup","date":"2025-03-14","start":"09:00:00","end":"09:30:00"}' \
http://localhost:8080/api/scheduler/meetings | jq .

curl -s -X POST -H "Accept: application/json" -H "Content-Type: application/json" --data \
'{"id":null,"title":"Design Review","date":"2025-03-14","start":"10:00:00","end":"11:00:00"}' \
http://localhost:8080/api/scheduler/meetings | jq .

curl -s -X POST -H "Accept: application/json" -H "Content-Type: application/json" --data \
'{"id":null,"title":"Retrospective","date":"2025-03-14","start":"13:00:00","end":"15:00:00"}' \
http://localhost:8080/api/scheduler/meetings | jq .

### Get all meetings

curl -s -X GET http://localhost:8080/api/scheduler/meetings | jq .

### Get one meeting

curl -s -X GET http://localhost:8080/api/scheduler/meetings/1 | jq .

### Get available meeting slots by specifying a meeting date and the meeting duration in minutes

curl -s -X GET "http://localhost:8080/api/scheduler/meetings/find?date=2025-03-14&duration=45" | jq .

