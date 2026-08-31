# API Gateway

## Overview

The API Gateway is the single entry point for the online-voting system. It centralizes authentication, request routing, security enforcement, and user identity propagation for all downstream microservices.

This service is built with Java 25 and Spring Boot 3.5.0, and it routes client calls to the auth, voter, candidate, election, voting, and result services through Spring Cloud Gateway.

## Responsibilities

- Route HTTP traffic from clients to the correct microservice
- Validate JWTs centrally before allowing access
- Enforce role-based access rules for protected endpoints
- Add user identity metadata to internal requests using headers such as:
  - `X-User-Id`
  - `X-User-Role`
- Hide internal service topology from client applications
- Integrate with service discovery and actuator monitoring

## Tech Stack

- Java 25
- Spring Boot 3.5.0
- Spring Cloud Gateway 2025.0.0
- Spring Security
- Spring OAuth2 Resource Server
- JJWT 0.11.5
- Maven
- Spring Boot Actuator

## Current Runtime Configuration

The project is configured to run under the Java 25 LTS toolchain, which is required because the workspace upgraded from the older Boot 3.2.x baseline to the Java 25-compatible Spring Boot 3.5 line.

Relevant runtime setup:
- JDK: `C:\Program Files\Java\jdk-25.0.4.1`
- Spring Boot: `3.5.0`
- Spring Cloud: `2025.0.0`
- Java target version: `25`

## Service Routes

The gateway is configured in `src/main/resources/application.yaml` and routes requests to the following local services:

| Route id | Target service | Local URL | Path pattern |
| --- | --- | --- | --- |
| auth-service | Auth Service | `http://localhost:8081` | `/auth/**` |
| voter-service | Voter Service | `http://localhost:8082` | `/voters/**` |
| election-service | Election Service | `http://localhost:8084` | `/elections/**`, `/positions/**`, `/position-candidates/**` |
| candidate-service | Candidate Service | `http://localhost:8083` | `/candidates/**` |
| voting-service | Voting Service | `http://localhost:8085` | `/votes/**` |
| result-service | Result Service | `http://localhost:8086` | `/live-results/**`, `/final-results/**` |

The gateway itself listens on:
- `http://localhost:8080`

## Security Model

Authentication and authorization in the gateway are implemented with JWT-based resource server security.

### JWT handling

- The gateway expects a bearer token in the `Authorization` header.
- The token is validated using the shared JWT secret configured in the gateway properties.
- The `JwtAuthenticationFilter` extracts the subject and role data and adds them to the forwarded request headers.

### Headers added to downstream requests

- `X-User-Id` = JWT subject
- `X-User-Role` = JWT `role` claim

### Auth requirements

The gateway configuration permits some public endpoints and restricts others by role:

Public routes include:
- `/auth/login`
- `/auth/register`
- `/elections/{electionId}`
- `/elections/bulk`
- `/positions/{positionId}`
- `/positions/bulk`
- `/candidates/{candidateId}`
- `/candidates/bulk`

Role-protected routes include:
- admin-only endpoints for auth and election management
- candidate/admin access for candidate-related actions
- authenticated access for the remaining routes

## Project Structure

```text
api-gateway/
├── src/
│   ├── main/
│   │   ├── java/com/online/voting/gateway/
│   │   │   ├── config/
│   │   │   │   ├── JwtAuthConverter.java
│   │   │   │   └── SecurityConfig.java
│   │   │   ├── handler/
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   ├── MissingRoleClaimException.java
│   │   │   │   └── ResourceFoundException.java
│   │   │   ├── security/
│   │   │   │   ├── JwtAuthenticationFilter.java
│   │   │   │   └── JwtUtil.java
│   │   │   └── ApiGatewayApplication.java
│   │   └── resources/
│   │       ├── application.yaml
│   │       └── application.properties
│   └── test/
│       └── java/com/online/voting/gateway/
│           ├── ApiGatewayApplicationTests.java
│           ├── config/
│           └── security/
├── pom.xml
├── mvnw
├── mvnw.cmd
├── HELP.md
├── README.md
└── target/
```

## Prerequisites

Before running the gateway, ensure the following are present:

- JDK 25 installed and active in your environment
- Maven wrapper available in the project folder
- All downstream microservices are running and reachable at the configured URI targets
- Shared JWT secret matches the auth service configuration

## Run Locally

From the project root:

```powershell
cd d:\spring boot\online-voting-system\api-gateway
$env:JAVA_HOME='C:\Program Files\Java\jdk-25.0.4.1'
$env:Path = "$env:JAVA_HOME\bin;" + $env:Path
.\mvnw clean test
.\mvnw spring-boot:run
```

Then open:
- `http://localhost:8080`

## Configuration Notes

The gateway uses the following security-related properties:

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          secret: "your secret key"
```

The same value must be kept aligned with the auth-service JWT configuration to avoid authentication failures.

## Validation and Troubleshooting

### Build and tests

Run:

```powershell
cd d:\spring boot\online-voting-system\api-gateway
$env:JAVA_HOME='C:\Program Files\Java\jdk-25.0.4.1'
$env:Path = "$env:JAVA_HOME\bin;" + $env:Path
.\mvnw test
```

### Common issues

- Java version mismatch: ensure the active JDK is 25
- JWT validation errors: confirm both gateway and auth service use the same secret
- Missing role claim: the gateway rejects tokens without a `role` claim with a `403` response
- Route not found: verify the downstream service is running and the path matches the configured route patterns

## Notes

This gateway was upgraded to the Java 25 / Spring Boot 3.5-compatible stack as part of the project modernization. It remains fully aligned with the rest of the online-voting microservice architecture and continues to serve as the central security and routing layer.

> The project currently emits Spring Cloud deprecation warnings for the legacy `spring-cloud-starter-gateway` / `spring-cloud-gateway-server` artifacts in the Boot 3.5 line. The application still starts and behaves correctly, but the gateway dependency set may be updated in a future cleanup pass to the newer `spring-cloud-starter-gateway-server-webflux` naming.

## Author / Project

This service is part of the Online Voting Microservice System.




