# Project Express

**A Spring Boot microservices-based job application platform**

Project Express is a backend-focused personal project exploring how independent services collaborate in a job application system. It brings together authentication, job management, applications, profiles, and notifications, with service discovery, centralized configuration, containerization, and a Jenkins CI pipeline.

Project Express combines Spring Boot microservices, service-to-service communication, event messaging, caching, fault tolerance, and container-based development. Jenkins automates Job Service builds, tests, and Docker image creation.

## Technology Stack

| Area | Technologies |
| --- | --- |
| Language and framework | Java 25, Spring Boot 4.1.1 |
| Security | Spring Security, JWT |
| Microservices infrastructure | Spring Cloud Config Server, Eureka Discovery Server, Spring Cloud API Gateway |
| Persistence | MySQL, Spring Data JPA / Hibernate |
| Synchronous service communication | Spring Cloud OpenFeign (Feign Client) |
| Asynchronous messaging | Apache Kafka |
| Caching | Redis |
| Resilience & fault tolerance | Resilience4j Circuit Breaker, Retry, fallback handling |
| Observability | Zipkin |
| Object storage | SeaweedFS (S3-compatible) |
| Build and testing | Maven Wrapper, JUnit / Spring Boot tests |
| Containers and automation | Docker, Docker Compose, Jenkins, GitHub |

## Architecture

```mermaid
flowchart TD
    Client[Client / API Consumer] --> Gateway[API Gateway :8080]
    Gateway --> Auth[Auth Service :8081]
    Gateway --> Job[Job Service :8082]
    Gateway --> Application[Application Service :8083]
    Gateway --> Profile[Profile Service :8085]
    Gateway --> Notification[Notification Service :8084]

    Config[Config Server :8888] -. configuration .-> Job
    Discovery[Eureka :8761] -. service discovery .-> Gateway
    Discovery -. service discovery .-> Job

    Application -. synchronous REST via OpenFeign .-> CircuitBreaker[Resilience4j Circuit Breaker + Retry]
    CircuitBreaker -. protected downstream call .-> Job
    Job --> MySQL[(MySQL)]
    Job --> Redis[(Redis)]
    Job --> Kafka[(Kafka)]
    Kafka -. events .-> Notification
    Job -. traces .-> Zipkin[Zipkin :9411]
```



## Inter-Service Communication, Messaging & Caching

Project Express uses different approaches depending on whether a service needs an immediate response, needs to publish an event, or wants to avoid repeatedly fetching data.

| Technology | Communication type | Role in the project |
| --- | --- | --- |
| **Spring Cloud OpenFeign (Feign Client)** | Synchronous HTTP/REST | Defines declarative clients for service-to-service API calls. The caller waits for the response. |
| **Apache Kafka** | Asynchronous messaging | Allows services to publish and consume events without requiring an immediate response from the consumer. |
| **Redis** | Caching | Provides fast access to cached data and can reduce repeated data retrieval. |
| **Eureka Discovery Server** | Service discovery | Helps services locate registered instances instead of relying on fixed host addresses. |

### How they differ

- **OpenFeign:** Use when one service needs information from another service **right now** (request/response).
- **Kafka:** Use when a service needs to **announce an event** and other services can process it independently.
- **Redis:** Use when the application benefits from **reusing cached data** rather than retrieving it repeatedly.

**Example:** OpenFeign handles request/response interactions, Kafka supports event-driven processing, and Redis helps reduce repeated data access.


## Resilience & Fault Tolerance

Project Express uses **Resilience4j Circuit Breaker and Retry** to protect synchronous calls from **Application Service** to **Job Service** through **Spring Cloud OpenFeign**.

- **Circuit Breaker:** Monitors downstream failures and can temporarily reject requests to an unavailable service to help prevent cascading failures.
- **Retry:** Retries eligible transient failures; configured retryable failures can be attempted up to three times.
- **Fallback:** A separate Spring-managed `JobValidationService` applies `@CircuitBreaker(name = "jobService", fallbackMethod = "jobServiceFallback")` so Spring's Resilience4j proxy can intercept calls correctly.
- **Controlled error handling:** If Job Service is unavailable, the fallback returns a controlled **HTTP 503 Service Unavailable** response. A nonexistent job returns **HTTP 404** without unnecessary retries.

| Circuit Breaker state | Behavior |
| --- | --- |
| **Closed** | Calls are allowed and failures are monitored. |
| **Open** | Calls fail fast while the downstream service is considered unhealthy. |
| **Half-open** | Limited trial calls are allowed to check recovery. |

**Error handling:** Retry is applied selectively, HTTP 404 responses avoid unnecessary retries, and downstream unavailability is handled through a controlled fallback.

## Services

| Component | Local port | Responsibility |
| --- | ---: | --- |
| API Gateway | 8080 | Entry point and routing |
| Auth Service | 8081 | Authentication and JWT-related functionality |
| Job Service | 8082 | Job-related backend functionality |
| Application Service | 8083 | Job application functionality |
| Notification Service | 8084 | Notification-related functionality |
| Profile Service | 8085 | Profile-related functionality |
| Eureka Discovery Server | 8761 | Service registration and discovery |
| Spring Cloud Config Server | 8888 | Centralized application configuration |
| Kafka | 9092 | Event messaging |
| Redis | 6379 | Caching / data access support |
| Zipkin | 9411 | Distributed tracing |
| SeaweedFS S3 endpoint | 8333 | S3-compatible object storage |

Ports describe the project's local configuration. Actual availability depends on which services are running.

## Repository Layout

The repository contains separate Spring Boot services and infrastructure configuration. Important paths include:

```text
project-express/
├── Jenkinsfile
├── docker-compose.yml
├── job-service/
│   └── job-service/
│       ├── Dockerfile
│       ├── mvnw.cmd
│       ├── pom.xml
│       └── src/
└── ... other microservices and configuration ...
```

## Local Setup

### Prerequisites

- JDK 25
- Docker Desktop with the Linux container engine
- MySQL accessible on the host, if using the current Compose database configuration
- Maven Wrapper (included with the services)

### Configure environment variables

The Docker Compose setup references these variables:

```dotenv
DB_USERNAME=your_mysql_username
DB_PASSWORD=your_mysql_password
JWT_SECRET=replace_with_a_long_random_secret
```

Place the values in a local `.env` file alongside `docker-compose.yml`, or supply them securely as environment variables. **Never commit real credentials, JWT secrets, or `.env` files.**

### Build Job Service

From the repository root in Windows PowerShell:

```powershell
cd .\job-service\job-service
.\mvnw.cmd clean package "-DskipTests"
```

### Build the Job Service Docker image

From the same Job Service directory:

```powershell
docker build -t project-express/job-service:latest .
```

The Dockerfile uses `eclipse-temurin:25-jre` and copies the Maven-generated JAR from `target/`.

### Docker Compose

Return to the repository root and validate the configuration:

```powershell
cd ..\..
docker compose config --quiet
```

The Compose file defines Job Service with the image `project-express/job-service:latest`. Start only the components you intend to use, and ensure their dependencies are available. For example, to start **only** Job Service without automatically starting dependencies:

```powershell
docker compose up -d --no-deps --no-build job-service
```

> Job Service requires supporting infrastructure such as Config Server, Eureka, Kafka, Redis, and MySQL. The command above does **not** start those dependencies, so it is not a complete standalone startup procedure.

## Jenkins CI/CD

The repository contains a declarative `Jenkinsfile` with these stages:

1. **Checkout** — retrieve source from GitHub.
2. **Build Job Service** — package the Spring Boot application using Maven.
3. **Run Unit Tests** — execute the Job Service tests.
4. **Build Docker Image** — create `project-express/job-service:latest`.
5. **Deploy Job Service** — defines a Docker Compose command to recreate the Job Service container.

The Jenkins pipeline builds and tests Job Service and creates its Docker image. The deployment stage defines this command:

```powershell
docker compose up -d --no-deps --force-recreate --no-build job-service
```

Jenkins credentials are referenced by ID rather than hardcoded into the repository:

- `project-express-db-username`
- `project-express-db-password`
- `project-express-jwt-secret`

The current Jenkins setup is local and has been triggered manually with **Build Now**. GitHub push triggers can be added to automate pipeline execution.

## Next Improvements

- Add deployment health checks to the Jenkins pipeline.
- Add a reliable local startup sequence and dependency readiness checks.
- Expand automated integration tests across services.
- Introduce Kubernetes Deployments, Services, ConfigMaps, and Secrets.
- Configure a container registry and versioned image tags for a fuller deployment pipeline.

## Author

**Simanta Paul**  
GitHub: [@simantapaul06](https://github.com/simantapaul06)

---

*A hands-on Java and Spring Boot microservices portfolio project.*
