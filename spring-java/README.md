# Simple scheduler built with Spring and Java

## Business logic

* You can only schedule a meeting between "working hours", ["08:00-17:00"]
* At upstart, add some meetings, ["12:00-12:15", "09:15-10:45", 15:00-15:30]
* You can schedule a meeting over lunch
* A meeting must be 15 minutes or longer
* Manage bad requests
* Manage when there are no available meeting slots
* Submit meeting time in minutes and return all possible times
* Nothing stops you to add a meeting i the past
* You cannot add a meeting on a weekend
* There is nothing stopping you to add a meeting on a holiday, f.e. easter, christmas

## Build application

```
./mvnw clean install
```

## Run application

```
./mvnw spring-boot:run
```

## Request examples

To get a pretty print of JSON response I utilise **jq**. It is a popular JSON tool in the Unix/Linux environment, also described as "sed for JSON data."
If we don’t have **jq** installed, try installing it using package managers:

* On Ubuntu/Debian – `sudo apt install jq`
* On CentOS/RHEL – `sudo yum install jq`
* On macOS – `brew install jq`

### Add meetings

curl -s -X POST -H "Accept: application/json" -H "Content-Type: application/json" --data \
'{"id":null,"title":"Team Standup","date":"2025-02-18","start":"09:00:00","end":"09:30:00"}' \
http://localhost:8080/api/scheduler/meetings | jq .

curl -s -X POST -H "Accept: application/json" -H "Content-Type: application/json" --data \
'{"id":null,"title":"Design Review","date":"2025-02-18","start":"10:00:00","end":"11:00:00"}' \
http://localhost:8080/api/scheduler/meetings | jq .

curl -s -X POST -H "Accept: application/json" -H "Content-Type: application/json" --data \
'{"id":null,"title":"Retrospective","date":"2025-02-18","start":"13:00:00","end":"15:00:00"}' \
http://localhost:8080/api/scheduler/meetings | jq .

### Get all meetings

curl -s -X GET http://localhost:8080/api/scheduler/meetings | jq .

### Get one meeting

curl -s -X GET http://localhost:8080/api/scheduler/meetings/1 | jq .

### Get available meeting slots by specifying a meeting date and the meeting duration in minutes

curl -s -F date=2025-02-18 -F duration=45 -X GET http://localhost:8080/api/scheduler/meetings/find | jq .
