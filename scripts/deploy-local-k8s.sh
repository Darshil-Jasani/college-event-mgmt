#!/usr/bin/env bash
# Deploy to a local Minikube/kind cluster using a locally built image.
set -euo pipefail
cd "$(dirname "$0")/.."

IMAGE=college-event-mgmt:local
docker build -t "$IMAGE" .

# Load image into the cluster
if command -v minikube >/dev/null 2>&1 && minikube status >/dev/null 2>&1; then
  minikube image load "$IMAGE"
else
  kind load docker-image "$IMAGE"
fi

kubectl apply -f k8s/namespace.yaml
if ! kubectl -n college-events get secret college-event-secrets >/dev/null 2>&1; then
  kubectl -n college-events create secret generic college-event-secrets \
    --from-literal=DB_PASSWORD=dbpass123 --from-literal=POSTGRES_PASSWORD=dbpass123 \
    --from-literal=ADMIN_PASSWORD=admin123 --from-literal=ORGANIZER_PASSWORD=organizer123 \
    --from-literal=STUDENT_PASSWORD=student123
fi
kubectl apply -f k8s/configmap.yaml -f k8s/postgres.yaml -f k8s/service.yaml -f k8s/hpa.yaml -f k8s/monitoring.yaml
kubectl apply -f k8s/deployment.yaml
kubectl -n college-events set image deployment/college-event-mgmt app="$IMAGE"
kubectl -n college-events patch deployment college-event-mgmt \
  -p '{"spec":{"template":{"spec":{"containers":[{"name":"app","imagePullPolicy":"IfNotPresent"}]}}}}'
kubectl -n college-events rollout status deployment/college-event-mgmt --timeout=180s
echo "Run: kubectl -n college-events port-forward svc/college-event-mgmt 8080:80   -> http://localhost:8080"
