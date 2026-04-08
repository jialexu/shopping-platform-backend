# Shopping Platform 2.0 Upgrade Planning Discussion

Date: 2026-04-08  
Scope: gateway, auth-service, account-service, item-service, inventory-service, order-service, payment-service, platform tooling

## 1) Purpose

This document provides a practical, execution-ready discussion framework for upgrading the current Shopping Platform from the existing 1.0 baseline to a resilient, secure, and operationally mature 2.0 platform.

Goals:
- Preserve current business capabilities (account, catalog, inventory, order, payment, auth, gateway)
- Reduce integration break risk during upgrades
- Standardize security, build, observability, and event contracts
- Establish measurable reliability and delivery quality gates

## 2) Current-State Snapshot (What Exists Today)

### Platform Baseline
- Multi-module Maven parent project with Java 21 and Spring Boot 3.3.4 at root.
- Spring Cloud 2023.0.3 in parent BOM, but some service-level BOM duplication.
- Docker Compose includes: Gateway, 6 business/auth services, Kafka + Zookeeper, MySQL, PostgreSQL, MongoDB, Cassandra.
- Polyglot persistence is implemented and aligned with service responsibilities.
- Kafka event-driven flow is implemented for order/payment/inventory status transitions.

### Service Topology and Responsibilities
- gateway: edge routing, JWT validation/filtering, CORS
- auth-service: login and token issuance/validation
- account-service: user profile and credential hash storage, internal auth lookup
- item-service: item metadata and UPC-indexed catalog in MongoDB
- inventory-service: SKU inventory reservation/release in Cassandra
- order-service: order lifecycle and payment-event reaction in Cassandra
- payment-service: payment orchestration, idempotency, and Kafka event publication in MySQL

## 3) Existing Service Analysis and 2.0 Upgrade Direction

## 3.1 Gateway

Current strengths:
- Centralized route management and coarse-grained auth boundary.
- Works with all downstream services and health endpoints.

Observed upgrade gaps:
- Hardcoded/shared HMAC secret pattern across platform.
- High debug logging by default can leak sensitive context and increase noise.
- Route resiliency patterns (timeouts, retries, circuit breakers, fallback policies) are not standardized.

2.0 direction:
- Move to issuer-based JWT verification via JWKS (asymmetric keys).
- Add policy-driven gateway filters: timeout, retry with idempotency safeguards, and rate limiting.
- Introduce correlation IDs and structured logging at edge.

## 3.2 Auth Service

Current strengths:
- Functional login and token issuance integrated with account-service.
- Feign-based user verification already in place.

Observed upgrade gaps:
- Secret-key token model across services is operationally risky.
- Limited token lifecycle controls (refresh rotation/revocation strategy not fully implemented).
- Auth contract is still tightly coupled to shared secret assumptions.

2.0 direction:
- Become the authoritative token issuer with rotating asymmetric keypairs.
- Publish JWKS endpoint and short-lived access tokens.
- Add refresh token rotation and revocation list (or cache-backed revocation strategy).

## 3.3 Account Service

Current strengths:
- Core CRUD and internal auth endpoint are implemented.
- Security + JPA stack and tests are present.

Observed upgrade gaps:
- Local and Docker database strategy is inconsistent (PostgreSQL in local profile, MySQL in docker profile).
- POM includes both postgres and mysql drivers, and uses legacy mysql artifact naming in one dependency.
- Mixed assumptions increase environment drift risk.

2.0 direction:
- Choose one primary relational engine for account-service in all environments (recommended: PostgreSQL for consistency and indexing flexibility).
- Remove unused DB driver and align profile-specific datasource contracts.
- Add DB migration tooling (Flyway/Liquibase) and remove dependence on hibernate ddl-auto update for controlled schema evolution.

## 3.4 Item Service

Current strengths:
- Catalog schema flexibility in MongoDB is appropriate.
- UPC uniqueness and CRUD APIs are aligned with domain.

Observed upgrade gaps:
- JWT resource server config relies on local secret field style, not issuer/JWKS.
- Logging is very verbose in default config.
- Contract/versioning strategy for external item lookup APIs is not explicit.

2.0 direction:
- Switch to issuer-uri/JWK-set verification model.
- Define stable API contract versioning for cross-service item lookup paths.
- Add pagination/query performance baselines and index strategy review for high-cardinality attributes.

## 3.5 Inventory Service

Current strengths:
- Reservation/release semantics support order workflow.
- Event-driven cancellation compensation exists.

Observed upgrade gaps:
- Cassandra config and schema lifecycle rely on mixed initialization assumptions.
- Event payload governance and backward-compatibility controls are limited.
- Retry/DLQ strategy for Kafka consumer errors is not standardized.

2.0 direction:
- Standardize Cassandra keyspace/table migrations and startup readiness checks.
- Introduce typed event schema governance (JSON schema or Avro contract discipline).
- Add consumer error handling policy: retries, DLQ topic, poison-message observability.

## 3.6 Order Service

Current strengths:
- Integrates synchronous validation (item/inventory) and asynchronous payment updates.
- Order state transitions and cancellation flow are present.

Observed upgrade gaps:
- Cassandra keyspace naming differs between configs (orderservice vs order_keyspace).
- Cross-service event class assumptions are brittle without shared schema governance.
- State transition rules should be explicitly guarded for duplicate/reordered event delivery.

2.0 direction:
- Unify keyspace naming across local/docker/infra scripts.
- Introduce event versioning and consumer compatibility testing.
- Implement explicit idempotency keys/state guards at order transition boundaries.

## 3.7 Payment Service

Current strengths:
- Idempotency concept and async processing are implemented.
- Publishes success/failure/refund events and persists payment records.

Observed upgrade gaps:
- Security model simplified after role checks were removed; authorization needs stronger policy definition.
- Event publication reliability should be hardened (outbox pattern strongly recommended).
- Operational SLOs (latency/error budget) are not formalized.

2.0 direction:
- Introduce role/scope-based authorization policy with explicit claim mapping.
- Add transactional outbox for payment events to improve exactly-once business behavior.
- Add payment-specific anti-fraud/risk hooks (even if initially mocked).

## 3.8 Platform Tooling and Delivery

Current strengths:
- Parent module and compose environment provide one-command local orchestration path.

Observed upgrade gaps:
- Makefile build target references a non-existent path (platform-infra/pom.xml), which can break CI/local consistency.
- Build, lint, and test quality gates are uneven by service.
- Current minimum test gate (30%) is too low for transaction-critical paths.

2.0 direction:
- Fix root build command path and standardize root-level Maven lifecycle.
- Add unified CI pipeline with required stages: compile, unit, integration, contract, security scan.
- Raise quality gates for critical services (order/payment/account/auth).

## 4) Cross-Cutting 2.0 Workstreams

## 4.1 Security and Identity Modernization
- Move from shared HMAC secrets to asymmetric signing + JWKS.
- Use standard claims model (sub, scope/roles, issuer, audience, expiration).
- Secret management via environment + vault style integration (no plaintext defaults in committed configs).

## 4.2 API and Event Contract Governance
- Introduce versioning rules for REST and Kafka events.
- Add consumer-driven contract tests for service-to-service APIs.
- Define schema compatibility policy: backward-compatible by default.

## 4.3 Data Management and Migration Discipline
- Flyway/Liquibase for relational schema migration.
- Cassandra and Mongo migration/versioning scripts with startup validation.
- Profile parity: local, docker, CI, and production use equivalent datasource contracts.

## 4.4 Reliability and Performance
- Gateway resilience patterns, service timeout budgets, and retry matrices.
- Kafka delivery reliability with DLQ and retry semantics by topic.
- Define SLOs: availability, p95 latency, async processing delay ceilings, event lag targets.

## 4.5 Observability and Operability
- OpenTelemetry tracing across gateway -> services -> Kafka transitions.
- Structured logs with correlation IDs and trace IDs.
- Service dashboards and alerting for queue lag, error rate, and database health.

## 4.6 Engineering Quality and CI/CD
- Standardized Maven plugin versions and dependency convergence checks.
- Service test pyramid: unit + slice/integration + contract + end-to-end smoke.
- Security scans for dependencies and container images in CI.

## 5) Phased Upgrade Plan (Recommended Execution)

Phase 0 (Week 1): Alignment and Design Freeze
- Produce ADRs for identity model, DB choices per service, event schema strategy.
- Lock non-functional targets (SLOs, RTO/RPO assumptions, deployment strategy).
- Define compatibility policy for 1.x to 2.0 rollout.

Phase 1 (Weeks 2-3): Platform Foundation
- Security base migration (JWKS, issuer/audience checks, claim normalization).
- Build and CI standardization, including Makefile and root Maven path fixes.
- Observability baseline: tracing, metrics, log correlation.

Phase 2 (Weeks 4-6): Service Hardening
- account-service DB unification and migration tooling.
- order/inventory Cassandra keyspace and event handling hardening.
- payment outbox and authorization policy hardening.
- gateway resiliency policy rollout.

Phase 3 (Weeks 7-8): Contract and Load Validation
- Run contract compatibility matrix across all producers/consumers.
- Execute load tests on order creation, payment processing, and inventory reservation.
- Validate failure injection scenarios (Kafka delay, DB restart, downstream timeout).

Phase 4 (Week 9): Controlled Release
- Canary deployment by route/domain path.
- Progressive traffic shifting with rollback checkpoints.
- Post-release verification and 2.0 stabilization backlog.

## 6) Risk Register (Top Planning Risks)

1. Security migration drift
- Risk: Mixed token validation models across services.
- Mitigation: Enforce issuer/JWKS in a shared starter and centralized policy tests.

2. Data environment divergence
- Risk: Local, docker, and CI using different DB engines/config assumptions.
- Mitigation: Single-source config contracts and migration scripts validated in CI.

3. Event schema breakage
- Risk: Producer change breaks consumer deserialization/state transitions.
- Mitigation: Versioned schemas, compatibility tests, DLQ fallback.

4. Payment consistency regression
- Risk: Duplicate or missing event publication during failures.
- Mitigation: Transactional outbox + idempotent consumer handlers.

5. Operational blind spots
- Risk: Incidents without sufficient telemetry.
- Mitigation: SLO dashboards and alert thresholds before release.

## 7) Definition of Done for 2.0

Functional:
- All 1.0 business flows pass with unchanged external behavior or approved versioned changes.
- Auth, account, item, inventory, order, payment, and gateway contracts are versioned and documented.

Quality and Reliability:
- CI pipeline green on compile, tests, contract checks, and security scan.
- Critical service coverage targets increased beyond 30% baseline (recommended: 60%+ service logic for payment/order/account/auth).
- p95 latency and error-rate SLOs are measured and met under agreed load.

Security and Operations:
- No committed plaintext production secrets.
- JWT validation model unified across gateway and all protected services.
- Observability stack provides trace continuity for end-to-end order-to-payment flow.

## 8) Recommended Planning Discussion Agenda

1. Confirm 2.0 target outcomes and rollout constraints.
2. Decide identity model and token lifecycle policy.
3. Resolve account-service relational engine standardization.
4. Agree Kafka schema/version and DLQ policy.
5. Approve phased plan, owners, and acceptance gates.
6. Schedule canary and rollback rehearsal before production cutover.

## 9) Immediate Next Actions (Actionable This Week)

1. Create ADRs for:
- JWT issuer/JWKS model
- Account-service primary relational DB
- Kafka event schema governance

2. Open implementation epics:
- Platform foundation epic (security, observability, CI)
- Service hardening epic (order/inventory/payment/account)
- Validation epic (contract/load/chaos tests)

3. Run baseline verification:
- Current end-to-end demo path
- Current test and build reproducibility
- Current topic and schema compatibility map

This planning document is intentionally discussion-oriented and execution-focused. It should be used as the source for converting 2.0 strategy into sprint-level tickets, ADRs, and release milestones.