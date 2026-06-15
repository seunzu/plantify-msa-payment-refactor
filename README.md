# plantify-msa-payment-refactor

Local lab for comparing two payment system architectures in a Spring Boot MSA.

- Baseline (`main`): `pay-service` owns orchestration, ledger, and settlement together. Distributed locks are scoped per service, leaving the same resource unprotected across service boundaries.
- Refactor target (`refactor/payment-orchestration-saga`): `payment-service` becomes the orchestrator. `pay-service` is reduced to ledger responsibility only. Monetary changes are serialized with a `userId`-scoped Redis lock. Duplicate payment entry is rejected by `orderId` unique constraints, and Ledger retries are guarded by `transactionId`.

## Services

- `payment-service`: payment entry point, orchestrates pay/refund/cancel flows, manages Payment state.
- `pay-service`: Pay balance, points, settlement records, ledger lock.
- `transaction-service`: transaction creation and final state transitions, publishes Kafka status events.
- `experiments/k6`: consistency and concurrency test scripts. (`refactor/payment-orchestration-saga` only)
- `docs`: architecture, sequence diagrams, and consistency strategy notes. (`refactor/payment-orchestration-saga` only)

## Auth assumption

Both branches delegate token validation to `auth-service` via network call, the same way as `plantify-msa-auth-refactor` `main`.

```text
payment-service / pay-service
  -> JwtFilter
      -> auth-service /v1/auth/validate-token
```

JWKS local validation is not applied here. `transaction-service` is an internal server and skips JWT validation entirely; the caller passes the verified user identifier in the request body.

## Local Run

Start local infrastructure and all three services:

```bash
docker compose up --build redis zookeeper kafka pay-service transaction-service payment-service
```

`auth-service` is not included in this repository. At runtime, `AUTH_SERVICE_URL` defaults to `http://host.docker.internal:8081`.
In the provided Docker Compose environment, `mock-auth` replaces auth-service for local consistency tests. `pay-service` listens on `8082` inside the Docker network and is exposed as `18082` on the host to avoid local port conflicts.

Verify compilation per service:

```bash
./gradlew compileJava
```

Run consistency experiment scripts (available on `refactor/payment-orchestration-saga` only):

```bash
docker compose --profile test run --rm k6 run /scripts/duplicate-transaction.js
docker compose --profile test run --rm k6 run /scripts/concurrent-payment-same-user.js
docker compose --profile test run --rm k6 run /scripts/multi-user-payment.js
```

## CI/CD

This repository currently has CI only. There is no CD target because this lab does not deploy to AWS/EKS yet.

## Docs

- [Architecture overview](docs/architecture-overview.md)
- [Design history](docs/design-history.md)
- [Consistency strategy](docs/consistency-strategy.md)
- [Package responsibilities](docs/package-responsibilities.md)
- [API response convention](docs/api-response-convention.md)
- [API spec](docs/api-spec.md)
- [Baseline sequence](docs/baseline-sequence.md)
- [Refactor target sequence](docs/refactor-target-sequence.md)
- [Concurrency test plan](docs/concurrency-test-plan.md)
- [Idempotency test plan](docs/idempotency-test-plan.md)
- [Failure scenario test plan](docs/failure-scenario-test-plan.md)
- [Test report](docs/test-report.md)
