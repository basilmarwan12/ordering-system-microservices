# Ordering System Microservices

> **About:** A Spring Boot microservices ordering platform with JWT
> authentication, an API gateway, Redis rate limiting, product and order
> services, RabbitMQ-based saga messaging, and isolated MySQL databases.

Migration of the [Ordering-System](https://github.com/basilmarwan12/Ordering-System) monolith
into microservices. This repository contains the authentication service, API gateway, two core domain
services, and the messaging contract between them. Service discovery remains
intentionally deferred while the deployment is small.

**GitHub repository description:** `Spring Boot microservices ordering platform with JWT auth, API Gateway, Redis rate limiting, RabbitMQ saga messaging, and Docker Compose.`

## Highlights

- API Gateway on port `8080` with service routing and Redis-backed rate limiting.
- JWT authentication and role-based authorization in `auth-service`.
- Product and order services with isolated MySQL databases.
- RabbitMQ order saga for asynchronous stock reservation.
- Aggregated Swagger UI available through the gateway.

## Authentication & authorization

auth-service issues HS256 JWTs signed with a shared secret (`JWT_SECRET` env var,
same value across all three services). product-service and order-service validate
that signature locally as OAuth2 Resource Servers — no network call back to
auth-service per request.

- **All endpoints require a valid token** — there is no anonymous/open access anywhere,
  including GET requests.
- **Role enforcement**: the JWT's `role` claim is mapped to a Spring Security authority.
  Product mutations (`POST`/`PATCH`/`DELETE /products/**`) require `ROLE_ADMIN`; product
  reads and all order endpoints require any authenticated user.
- **Bootstrapping an admin**: every new registration defaults to `ROLE_CUSTOMER` — there
  is no self-service way to register as admin. Promote a user via
  `PATCH /users/{id}/role` on auth-service (itself `ROLE_ADMIN`-gated), which means the
  very first admin has to be set directly in the `users` table in `auth_db`, e.g.:
  ```sql
  UPDATE users SET role = 'ROLE_ADMIN' WHERE email = 'you@example.com';
  ```
  Use that account's token to promote everyone else through the API from then on.
- **Known gap**: order-service doesn't yet check that the JWT's subject (user id)
  matches an order's `userId` — any authenticated user can currently read any order by
  id. Fix before this touches real user data.

## What's here

```
services/
├── api-gateway/       # Public entry point + Redis-backed rate limiting (port 8080)
├── auth-service/      # Registration, login, JWT issuance, and user roles (port 8083)
├── product-service/   # Product CRUD + stock reservation (port 8081)
└── order-service/     # Order CRUD + saga orchestration (port 8082)
libs/
└── common/            # Shared event DTOs + RabbitMQ topology constants
```

## How order creation works (the saga)

1. `POST /orders` on order-service → row saved as `PENDING`, prices/names fetched
   synchronously from product-service via REST.
2. order-service publishes `OrderCreatedEvent` to RabbitMQ (`orders.exchange`, routing key `order.created`).
3. product-service consumes it, locks the relevant product rows, and does an
   **all-or-nothing** check: if every line has enough stock, it decrements all of them;
   if any line is short, it decrements nothing.
4. product-service publishes `StockReservationResultEvent` back (routing key `stock.reservation.result`).
5. order-service consumes the reply and flips the order to `CONFIRMED` or `CANCELLED`.

This keeps the two services' databases fully independent — no shared schema, no
cross-service JPA relations — at the cost of the order briefly sitting in `PENDING`
until the round trip completes (typically milliseconds locally).

## Running locally

```bash
cp .env.example .env
docker compose up --build
```

- api-gateway: http://localhost:8080
- RabbitMQ management UI: http://localhost:15672 (guest/guest)

Use the gateway for application traffic: `/auth/**` and `/users/**` route to
auth-service, `/products/**` routes to product-service, and `/orders/**` routes
to order-service. The gateway uses Redis token buckets keyed by client IP:
auth requests allow 5 requests/second with a burst of 10, product requests
allow 20/second with a burst of 40, and order requests allow 10/second with a
burst of 20. Requests over a route's limit receive HTTP 429.
The three application services are internal Docker services and are not exposed
directly on the host.
Opening `http://localhost:8080/` returns the gateway status and available route
prefixes; application requests must use one of the listed prefixes.
Gateway Swagger UI is available at
`http://localhost:8080/swagger-ui.html`; its service selector loads the auth,
product, and order OpenAPI documents through the gateway.

Each service has its own MySQL container/schema (`auth-db`, `product-db`, and `order-db`)
— this is intentional; it's the main thing that makes them independently deployable.

## What's deliberately deferred to the next slice

- **api-gateway**: static routes are now provided on port 8080. Service URLs
  can be overridden with `AUTH_SERVICE_URL`, `PRODUCT_SERVICE_URL`, and
  `ORDER_SERVICE_URL`; Redis is configured with `REDIS_HOST` and `REDIS_PORT`.
- **discovery-service**: URLs are static config (`PRODUCT_SERVICE_URL` env var) rather
  than dynamic service discovery. Fine at 2 services; worth adding Eureka once there
  are 4+.
- **Idempotency beyond the status guard**: `StockReservationResultListener` skips
  duplicate deliveries by checking the order isn't already `PENDING`, but there's no
  dead-letter queue configured yet for messages that fail after all retries — add one
  before this goes near production traffic.
- **Distributed tracing**: no correlation ID is threaded through the REST call +
  event chain yet. Worth adding (e.g. Micrometer Tracing + Zipkin) once there are
  enough services that a request spans more than two hops.

## Not yet migrated from the monolith

Ported as-is: `Product`, `Order`, `OrderItem` entity shapes (relations to `User`/other
aggregates replaced with plain ID columns per the notes in each entity file).
The authentication and JWT/OAuth2 pieces now live in `auth-service`; automated
database seeding, service discovery, distributed tracing, and a dead-letter
queue are still not included.
