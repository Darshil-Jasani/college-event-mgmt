#!/usr/bin/env bash
# Roll the application back to the previous revision.
set -euo pipefail
kubectl -n college-events rollout history deployment/college-event-mgmt
kubectl -n college-events rollout undo deployment/college-event-mgmt
kubectl -n college-events rollout status deployment/college-event-mgmt --timeout=120s
