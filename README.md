🌐 API Gateway – Online Voting Microservices
============================================
📌 Overview
--------------

The API Gateway acts as the single entry point for all client requests in the Online Voting Microservice Architecture.
It handles request routing, JWT authentication, and security enforcement, ensuring that only authorized requests reach downstream services.

The gateway is fully integrated with Eureka Service Discovery and performs centralized JWT validation.

🧱 Responsibilities
--------------------

Single entry point for all clients

- JWT validation and authentication
- Route requests to internal microservices
- Forward user identity and role information
- Protect internal services from direct exposure

🛠️ Tech Stack
---------------

Java 21

Spring Boot 3.1.8

Spring Cloud Gateway

Spring Cloud Netflix Eureka Client

JWT (JJWT)

Maven

📂 Project Structure
---------------------

```
api-gateway/
├── src/main/java/com/online/voting/gateway
│   ├── filter
│   │   └── JwtAuthFilter.java
│   ├── security
│   │   └── JwtValidator.java
│   └── ApiGatewayApplication.java
├── src/main/resources
│   └── application.yml
├── pom.xml
└── README.md

```

🔐 Security Flow
------------------

Client sends credentials to Auth Service

Auth Service issues JWT

Client sends JWT to API Gateway

Gateway validates JWT

Gateway forwards request with headers:

X-User

X-Role
____________________________________________
👨‍💻 Author
=============
Irakarama Bergerac

Online Voting Microservice System




