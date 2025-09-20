# URL Scan Service

## Overview

The URL Scan Service serves as the backend API service for facilitating URL security scans. Users can perform CRUD
operations against URL scans through the `/scans` endpoint:

* GET `/scans`: lists historical scan requests with summary information for each scan
* GET `/scans/{scanId}`: retrieves detailed information for a given scan
* POST `/scans`: submits a new scan request
* DELETE `/scans/{scanId}`: deletes an existing scan request

## Development

### Pre-requisites

Before attempting to build and run the server locally, make sure the following tools are set up on your local
development machine.

#### Docker

Docker is used for hosting the containers that the service is composed of. To get started quickly,
install [Docker Desktop](https://docs.docker.com/desktop/), which provides a GUI.

### Building the Service

To build the service and run unit tests, run the following command:

```shell
./mvnw clean install
```

### Running the Service

The service runs as 2 separate containers in Docker:

1. A postgres instance that serves as the database
2. A Spring Boot application instance that serves as the API server

Both containers have port mappings which can be used for localhost access:

* Postgres: http://localhost:5432
* Application: http://localhost:80

In order to run the service, run the following command:

```shell
docker compose up --build
```

### Database Schema Changes

Whenever the project is built, the `create.sql` file under `database/` will be updated with sample SQL statements for initializing a fresh database.

The `init.sql` file in the `database/sql` folder contains the SQL commands for initializing a Postgres database when the
container is first started in Docker. In order for the changes to be reflected in the database container, the container
must be restarted using:

```shell
docker compose down -v
```

Once the running containers have terminated, run the following command to restart them:

```shell
docker compose up --build
```

## Testing

Confidence in the correctness of the service should be gained mostly through automated testing in the form of unit tests
which run at build time and integration tests which run once the service is running. However, to facilitate development
of new APIs and features, a [Swagger UI](https://swagger.io/tools/swagger-ui/) is exposed which simplifies direct API
access.

### Manual Testing

Once the service is running in Docker, navigate to http://localhost/swagger-ui/index.html to access the Swagger UI.
The UI displays API documentation, as well as domain models used in requests and responses.

The UI can be leveraged to send requests and receive responses by expanding each API endpoint and clicking `Try it out`.

### Unit Testing

Unit tests run at build time and both line and branch coverage is required to be at least 80% in order to pass. GitHub
also has a workflow set up for pull requests that run unit tests as an approval step before merges can occur.

### Integration Testing

Integration tests run against a running service. It ensures the integration points of the service with dependencies (
Postgres database, urlscan.io) are working as expected. The integration tests are maintained in a
separate [url-scan-service-tests]() repository. Refer to the README of the repository for executing the tests.

## Authentication

User authentication to the service is done using [Auth0](https://auth0.com/), which allows the creation of new user accounts for testing.

*Note: Auth0 was chosen over a manual implementation in order to simulate standard authentication and authorization
protocols through the Authorization header of requests*

# Design
