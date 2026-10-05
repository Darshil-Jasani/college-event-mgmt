# 🎓 College Event Management System – CI/CD & DevOps Project

MSc IT DevOps project #15. A Spring Boot application for creating events, registering
participants and recording attendance, wrapped in a complete DevOps pipeline:
Git workflow → Maven build → automated tests → security scans → Docker image →
registry → Kubernetes deployment → monitoring & logging.

## 1. Application features
- **Events**: create / read / update / delete (Admin & Organizer)
- **Registrations**: students register or cancel; capacity and duplicate checks
- **Participation records**: organizers mark attendance per registration
- **Roles** (HTTP Basic): `admin`, `organizer`, `student`
- **Web UI** at `/`, REST API under `/api`, health at `/actuator/health`, metrics at `/actuator/prometheus`

| User | Default password (local only) | Can do |
|---|---|---|
| admin | admin123 | everything |
| organizer | organizer123 | manage events, view registrations, mark attendance |
| student | student123 | view events, register / cancel |

Passwords are overridden by environment variables / Kubernetes Secrets in Docker and K8s.

### REST API
| Method | Path | Role |
|---|---|---|
| GET | /api/events, /api/events/{id} | any |
| POST/PUT/DELETE | /api/events[/{id}] | admin, organizer |
| POST/DELETE | /api/events/{id}/register | any (own registration) |
| GET | /api/events/{id}/registrations | admin, organizer |
| POST | /api/events/{id}/attendance/{username}?attended=true | admin, organizer |
| GET | /api/me/registrations | any |

## 2. Run locally
```bash
# Java 17 + Maven
mvn test                 # unit + integration tests
mvn spring-boot:run      # http://localhost:8080  (in-memory H2)
```

## 3. Docker Compose (app + PostgreSQL + Prometheus + Grafana + Loki)
```bash
cp .env.example .env     # edit passwords
docker compose up --build
```
| Service | URL |
|---|---|
| App | http://localhost:8080 |
| Prometheus | http://localhost:9090 |
| Grafana | http://localhost:3000 (admin / GRAFANA_PASSWORD) – dashboard "College Event Mgmt - Overview" |

Logs are collected by Promtail → Loki and shown in the Grafana dashboard (Explore → Loki).

## 4. Kubernetes (Minikube / kind)
```bash
./scripts/deploy-local-k8s.sh
kubectl -n college-events port-forward svc/college-event-mgmt 8080:80
```
Manifests in `k8s/`: Namespace, ConfigMap (env config), Secret (example), PostgreSQL + PVC,
Deployment (RollingUpdate, liveness/readiness/startup probes, resource limits, non-root),
Service, Ingress, HPA, plus in-cluster Prometheus/Grafana (`monitoring.yaml`).

**Rollback:** `./scripts/rollback.sh` (or `kubectl -n college-events rollout undo deployment/college-event-mgmt`).

## 5. CI/CD pipeline (`.github/workflows/ci-cd.yml`)
1. **Build & test** – `mvn verify`, version `1.0.<run_number>`, JAR uploaded as a versioned artifact
2. **Source security scan** – Trivy (vulnerable dependencies, secrets, misconfigurations)
3. **Docker** – build image, Trivy image scan (fails on CRITICAL), push to GHCR with version + `latest`
4. **Deploy** (main only, `production` environment) – apply manifests, set new image, wait for rollout, **auto-rollback** on failure

A `Jenkinsfile` with the same stages is provided as an alternative.

### One-time setup for the pipeline
1. Push the repo to GitHub (branches: `main`, `develop`, `feature/*`).
2. Replace `darshil-jasani` in `k8s/deployment.yaml` (and `Jenkinsfile`).
3. For the deploy job add repository secret `KUBE_CONFIG` (base64 of your kubeconfig) and create the
   K8s secret once: see `k8s/secret.example.yaml`.
4. Make the GHCR package public or add an image pull secret.

## 6. Git workflow
`main` (production) ← `develop` (integration) ← `feature/<name>`. Open PRs; CI must pass before merge.
Release by tagging, e.g. `git tag v1.0.0`.

## 7. Security measures
Role-based access, BCrypt password hashing, secrets via env/K8s Secrets (not baked into image),
non-root container + dropped capabilities, Trivy dependency/secret/image scanning, input validation.
**For production**, replace the in-memory demo users with a real user store.

## 8. Project structure
See `docs/ARCHITECTURE.md`.
