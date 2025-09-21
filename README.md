# URL Scan Service

## Overview

The URL Scan Service is a system that allows users to submit, manage, and view the results of scans against URLs for
potential security issues. The service offers an endpoint for clients to create, view, and delete scan requests through
the `/v1/scans` endpoint.

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

The service runs as 4 separate containers in Docker:

1. `postgres`: A postgres instance that serves as the database
2. `api-server`: A Spring Boot application that serves as the API server
3. `scan-submitter`: A Spring Boot application that runs a scheduled job for submitting pending scan requests
4. `status-poller`: A Spring Boot application that runs a scheduled job for polling the status of ongoing scan requests

The following containers have port mappings which can be used for localhost access:

* Postgres: http://localhost:5432
* Application: http://localhost:80

In order to run the service, run the following command:

```shell
docker compose up --build
```

### Database Schema Changes

Whenever the project is built, the `create.sql` file under `database/` will be updated with sample SQL statements for
initializing a fresh database.

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

#### Logging Out

To log out, navigate to http://localhost/logout.

### Unit Testing

Unit tests run at build time and both line and branch coverage is required to be at least 80% in order to pass.

### Integration Testing

Integration tests run against a running service. It ensures the integration points of the service with dependencies (
Postgres database, urlscan.io) are working as expected. The integration tests are maintained in a
separate [url-scan-service-tests]() repository. Refer to the README of the repository for executing the tests.

## Authentication

User authentication to the service is done using [Auth0](https://auth0.com/), which allows the creation of new user
accounts for testing.

*Note: Auth0 was chosen over a manual implementation in order to simulate standard authentication and authorization
protocols through the Authorization header of requests*

# Design

The URL Scan Service is composed of 3 separate components:

* API Server: A REST API service for performing CRUD operations against URL scans
* Scan Requester: A long-running process that asynchronously submits URL scan requests to urlscan.io
* Status Poller: A long-running process that asynchronously polls the status of submitted URL scans for completion

The components are separated because they perform different responsibilities, and as the system scales, are likely scale
differently.

For example:

* In the event that read requests increase disproportionately to write requests, only the API service is required to
  scale to handle the increased load
* In the event that the status poller is to process scan results (which tend to be large), it can scale independently of
  the API server or the scan submitter

## API Server

The API Server provides a REST API for CRUD operations against URL scans through the following APIs:

* GET `/v1/scans`: lists historical scan requests with summary information for each scan
* GET `/v1/scans/{scanId}`: retrieves detailed information for a given scan
* POST `/v1/scans`: submits a new scan request
* DELETE `/v1/scans/{scanId}`: deletes an existing scan request

APIs require authentication, which is managed
through [an Auth0 application](https://manage.auth0.com/dashboard/us/dev-bglprge8mcc8yj82/applications/7s16iwyxHFmiZO7CJeRYmaFMTqB7nP4I/settings).
Users are only allowed to view and manage their own scans, but can be returned scan results from a prior scan with
the same parameters if submitted within a given dedupe window (currently 1 hour).

Data for the service is persisted in a Postgres database that contains 2 tables:

* `scan`: table that contains entries for individual user scan requests, with a foreign key to the `scan_result` which
  contains details on the scan that was requested
* `scan_result`: table that contains entries for individual scan results

Two tables are used in order to separate the handling of user scan requests, and the scans that are actually issued to
urlscan.io. This separation allows for simpler deduplication on the scans that are sent to urlscan.io, which reduces load against the service.

## Scan Requester


