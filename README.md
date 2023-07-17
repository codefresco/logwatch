# logwatch

Ingests stream of logs from docker containers using docker Graylog driver parses and stores them in InfluxDB. There is an example endpoint that retrieves the recent logs.

## How to run

There is a docker compose and a docker file in the root of the repo. Running `docker compose --profile staging up -d` will up the following containers:
- `ingestor`: The log ingestor, including an example endpoint, listens to logs and stores them in InfluxDB. Running a `GET` on `localhost:8080/recent?count=5` will return the last 5 logs.
- `InfluxDB`: Time series database. More info here: https://github.com/influxdata/influxdb
- `log-generator`: Simple Python script that generates logs as if it was a real container. It can be replaced by an actual application. The `logging` key for this container in the docker compose file shows how logs are directed to the ingestor.

> :warning: **If switching from staging to dev profile or back**: Remember to down one profile and up the other. They both use the same ports for the log ingestor. See below for the uses of these profiles.

## Docker compose profiles
- `staging`: This profile runs up the application, InfluxDB and the log generator. Run `docker compose --profile staging up -d --build` to rebuild the image if changed the code.
- `dev`: This profile is for developing inside a container (see below.) This is simple to set up for projects and is useful in many cases.
  - Host machine is Windows, but developing for Linux: helps keep the dev environment as close to the prod environment.
  - Working with different versions of different languages: keeps a reproducible development environment for each project without having to remember and a development suit for each project
  - Other cases like working on a remote machine, debugging weird environment dependant bugs, feeling really cool, etc.


## Developing inside the container

Running `docker compose --profile dev up -d` will spin up a development container with java 17 and gradle installed and the source mounted. Attach to it using vscode or nvim to develop inside the container.
The application is mounter under `/usr/src/app` (change in docker compose file if needed), so the remote editor has to open that directory.

While developing inside the container (running the dev profile), the `log-generator` does not spin up in this profile. Running `docker compose  up log-generator -d --no-deps` on the host machine spins up the log generator. This is to be able to run it when the application under development is running.

For example: made some changes and ran `./gradlew bootRun` in the container, run the `log-generator` on the host to send some logs to the application.

## Endpoints

Either explore in localhost:8086 (InfluxDB UI) or curl the following endpoints:
- `GET localhost:8080/recent?count=10` Returns 10 recent logs recorded
- `GET localhost:8080/latency?count=100` Returns 100 recent request latencies recorded

## Work in progress

Add a react front end to set up aggregate queries and cool things :cowboy_hat_face:
