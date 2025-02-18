# Simple scheduler

## Business logic

* You can only schedule a meeting between "working hours", ["09:00-17:00"]
* At upstart, add some meetings, ["12:00-12:15", "09:15-10:45", 15:00-15:30]
* You can schedule a meeting over lunch
* A meeting must be 15 minutes or longer
* Manage bad requests
* Manage when there are no available meeting slots
* You should be able to send a request
* Submit meeting time in minutes and return all possible times
* Nothing stops you to add a meeting i the past
* Nothing stops you to schedule meetings on weekends and holidays

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

### Get available meeting slots by specifying the meeting date and duration in minutes

curl -s -X GET http://localhost:8080/api/scheduler/meetings/2025-02-18/45 | jq .
