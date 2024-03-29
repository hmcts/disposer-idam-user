# Idam user disposer

## Purpose

This micro-service runs periodically and disposes archived citizen accounts

## Getting started

### Prerequisites
- [JDK 21](https://openjdk.org/projects/jdk/21/)

### Building the application

The project uses [Gradle](https://gradle.org/) as a build tool. It already
contains `gradlew` wrapper script, so there's no need to install system-wide
gradle.

To build the project execute the following command:

```bash
./gradlew build
```

### Running the application

Create the image of the application by executing the following command:

```bash
  ./gradlew assemble
```

Create docker image:

```bash
  docker-compose build
```

Run the distribution by executing the following command:

```bash
  docker-compose up
```

This will start the container and by default exits immediately as service is
set to disabled. If service were enabled, then it would try to fetch stale
(archived) users from idam-api, filter by roles querying (am-role-assignments)
and call user deletion endpoint on idam-api.

### Alternative script to run application

To skip all the setting up and building, just execute the following command:

```bash
./bin/run-in-docker.sh
```

For more information:

```bash
./bin/run-in-docker.sh -h
```

Script includes bare minimum environment variables necessary to start api
instance. Whenever any variable is changed or any other script regarding docker
image/container build, the suggested way to ensure all is cleaned up properly
is by this command:

```bash
docker-compose rm
```

It clears stopped containers correctly. Might consider removing clutter of
images too, especially the ones fiddled with:

```bash
docker images

docker image rm <image-id>
```

There is no need to remove postgres and java or similar core images.

## Developing

### Unit tests
To run all unit tests execute the following command:
```bash
./gradlew test
```

### Integration tests
To run all integration tests execute the following command:
```bash
./gradlew integration
```

### Functional tests
Functional tests require setting the right environment variables pointing to
idam-api and am-role-assignemnt services. To access those services, VPN needs
to be connected. To get secret and password refer to disposer key-vault. Once
everything is setup, functional tests can be executed using the following
command:

```bash
./gradlew functional
```

### Code quality checks
We use [checkstyle](http://checkstyle.sourceforge.net/) and [PMD](https://pmd.github.io/).

To run all checks execute the following command:

```bash
./gradlew clean checkstyleMain checkstyleTest checkstyleIntegrationTest pmdMain pmdTest pmdIntegrationTest
```

or to generate a code coverage report execute the following command:

```bash
./gradlew integration functional jacocoTestReport
```

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details

