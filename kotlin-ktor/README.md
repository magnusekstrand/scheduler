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

To build or run the project, use one of the following tasks:

| Task                          | Description                                                          |
| -------------------------------|---------------------------------------------------------------------- |
| `./gradlew test`              | Run the tests                                                        |
| `./gradlew build`             | Build everything                                                     |
| `buildFatJar`                 | Build an executable JAR of the server with all dependencies included |
| `buildImage`                  | Build the docker image to use with the fat JAR                       |
| `publishImageToLocalRegistry` | Publish the docker image locally                                     |
| `run`                         | Run the server                                                       |
| `runDocker`                   | Run using the local docker image                                     |

If the server starts successfully, you'll see the following output:

```
2024-12-04 14:32:45.584 [main] INFO  Application - Application started in 0.303 seconds.
2024-12-04 14:32:45.682 [main] INFO  Application - Responding at http://0.0.0.0:8080
```

## Sample requests

curl -X GET http://localhost:8080/api/v1/meetings

curl -X GET http://localhost:8080/api/v1/meetings/1

curl -X POST http://localhost:8080/api/v1/meetings -d '{ "title" : "Lunch phone call",  "date" : "2025-03-13",  "start" : "12:00",  "end" : "12:30" }'

curl -X PUT http://localhost:8080/api/v1/meetings/1 -d '{ "id" : 1, "title" : "Lunch phone call",  "date" : "2025-03-11",  "start" : "12:00",  "end" : "13:00" }'

curl -X DELETE http://localhost:8080/api/v1/meetings/1

