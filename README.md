# logwatch

Ingests stream of logs from docker containers using docker graylog driver, parses and stores them in InfluxDB. There is an example endpoint that retrieves the recent logs.

## How to run

There is a docker compose and a docker file in the root of the repo. Running `docker compose --profile staging up -d` will up the following containers:
- `ingestor`: The log ingestor including an example endpoint, listens to logs and stores them in InfluxDB. Running a `GET` on `localhost:8080/recent?count=5` will return the last 5 logs.
- `InfluxDB`: Time series database. More info here: https://github.com/influxdata/influxdb
- `log-generator`: Simple python script that generates logs as it was a real container. Can be replaced by an actual application. The `logging` key for this container in the docker compose file shows how logs are directed to the ingestor.

## Docker compose profiles

Running `docker compose --profile dev up -d` will spin up a development container with java 17 and gradle installed and the source mounted. Attach to it using vscode or nvim to develop inside the container.
When developing inside the container, `docker compose --profile manual up -d` spins up the log generator. This is to be able to run it after `./gradlew bootRun` so the logs are captured.

## Work in progress

Add a react front end to set up aggregate queries and cool things :cowboy_hat_face:
