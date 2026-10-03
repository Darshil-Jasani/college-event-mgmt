# Architecture & DevOps Flow

## Pipeline
```mermaid
flowchart LR
  A[Developer commit / PR] --> B[GitHub Actions]
  B --> C[Maven build + tests]
  C --> D[Trivy fs scan]
  D --> E[Docker build]
  E --> F[Trivy image scan]
  F --> G[Push to GHCR - versioned]
  G --> H[kubectl rollout]
  H --> I{Healthy?}
  I -- yes --> J[Live]
  I -- no --> K[rollout undo]
```

## Runtime
```mermaid
flowchart LR
  U[Browser / API client] --> IN[Ingress]
  IN --> SVC[Service]
  SVC --> P1[App pod 1]
  SVC --> P2[App pod 2]
  P1 & P2 --> DB[(PostgreSQL + PVC)]
  PR[Prometheus] -- scrapes /actuator/prometheus --> P1 & P2
  GR[Grafana] --> PR
  GR --> LK[Loki logs]
```

## Repository layout
```
src/main/java/...        controllers, services, repositories, entities, security config
src/main/resources       application.yml, static web UI
src/test/java/...        unit (Mockito) + integration (MockMvc) tests
Dockerfile               multi-stage, layered, non-root
docker-compose.yml       local full stack with monitoring + logging
.github/workflows        CI/CD pipeline
Jenkinsfile              alternative pipeline
k8s/                     Kubernetes manifests
monitoring/              Prometheus, Grafana dashboards/datasources, Promtail config
scripts/                 local K8s deploy, rollback
```

## Data model
- **Event**(id, title, description, venue, eventDate, capacity, createdBy)
- **Registration**(id, event_id, username, status, attended, attendedAt, registeredAt) — unique(event, username)

## Rolling-update vs blue-green
The Deployment uses `RollingUpdate` with `maxUnavailable: 0` (zero downtime). Rollback is a single command.
