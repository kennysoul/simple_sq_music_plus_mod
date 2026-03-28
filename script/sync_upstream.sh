#!/usr/bin/env bash
set -euo pipefail

UPSTREAM_REMOTE="upstream"
UPSTREAM_REPO="59799517/simple_sq_music_plus"
UPSTREAM_BRANCH="3.0"
LOCAL_BRANCH="3.0"
PUSH=true
ALLOW_DIRTY=false

usage() {
  cat <<'EOF'
Usage:
  bash script/sync_upstream.sh [options]

Options:
  --push               Sync local branch then push to origin (default)
  --no-push            Sync local branch only, do not push
  --allow-dirty        Allow running with uncommitted local changes
  --upstream-repo REPO Upstream repo in owner/name format (default: 59799517/simple_sq_music_plus)
  --upstream-branch BR Upstream branch name (default: 3.0)
  --local-branch BR    Local branch name to receive updates (default: 3.0)
  -h, --help           Show this help

Examples:
  bash script/sync_upstream.sh --push
  bash script/sync_upstream.sh --no-push
  bash script/sync_upstream.sh --upstream-branch 3.0 --local-branch 3.0 --push
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --push)
      PUSH=true
      shift
      ;;
    --no-push)
      PUSH=false
      shift
      ;;
    --allow-dirty)
      ALLOW_DIRTY=true
      shift
      ;;
    --upstream-repo)
      UPSTREAM_REPO="$2"
      shift 2
      ;;
    --upstream-branch)
      UPSTREAM_BRANCH="$2"
      shift 2
      ;;
    --local-branch)
      LOCAL_BRANCH="$2"
      shift 2
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      echo "Unknown option: $1" >&2
      usage
      exit 1
      ;;
  esac
done

ROOT_DIR="$(git rev-parse --show-toplevel 2>/dev/null || true)"
if [[ -z "$ROOT_DIR" ]]; then
  echo "[ERROR] Not inside a git repository." >&2
  exit 1
fi

cd "$ROOT_DIR"

echo "[INFO] Repo root: $ROOT_DIR"
echo "[INFO] Local branch: $LOCAL_BRANCH"
echo "[INFO] Upstream branch: $UPSTREAM_BRANCH"
echo "[INFO] Upstream repo: $UPSTREAM_REPO"

if [[ "$ALLOW_DIRTY" != "true" ]] && [[ -n "$(git status --porcelain)" ]]; then
  echo "[ERROR] Working tree is not clean. Commit/stash changes first, or use --allow-dirty." >&2
  exit 1
fi

if ! git remote get-url "$UPSTREAM_REMOTE" >/dev/null 2>&1; then
  echo "[INFO] Adding upstream remote: $UPSTREAM_REPO"
  git remote add "$UPSTREAM_REMOTE" "https://github.com/${UPSTREAM_REPO}.git"
fi

echo "[INFO] Fetching remotes..."
git fetch origin "$LOCAL_BRANCH"
git fetch "$UPSTREAM_REMOTE" "$UPSTREAM_BRANCH"

if ! git show-ref --verify --quiet "refs/heads/${LOCAL_BRANCH}"; then
  echo "[INFO] Creating local branch ${LOCAL_BRANCH} from origin/${LOCAL_BRANCH}"
  git checkout -b "$LOCAL_BRANCH" "origin/${LOCAL_BRANCH}"
else
  git checkout "$LOCAL_BRANCH"
fi

echo "[INFO] Fast-forward local branch from origin/${LOCAL_BRANCH}"
git pull --ff-only origin "$LOCAL_BRANCH"

LOCAL_SHA="$(git rev-parse HEAD)"
UPSTREAM_SHA="$(git rev-parse ${UPSTREAM_REMOTE}/${UPSTREAM_BRANCH})"

if [[ "$LOCAL_SHA" == "$UPSTREAM_SHA" ]]; then
  echo "[INFO] Local branch already matches upstream commit: $LOCAL_SHA"
else
  echo "[INFO] Merging ${UPSTREAM_REMOTE}/${UPSTREAM_BRANCH} into ${LOCAL_BRANCH}"
  git merge --no-edit "${UPSTREAM_REMOTE}/${UPSTREAM_BRANCH}"
fi

if [[ "$PUSH" == "true" ]]; then
  echo "[INFO] Pushing ${LOCAL_BRANCH} to origin/${LOCAL_BRANCH}"
  git push origin "$LOCAL_BRANCH"
fi

echo "[DONE] Sync complete."
