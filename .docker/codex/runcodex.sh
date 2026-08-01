#!/usr/bin/env bash

set -e

script_dir="$(realpath "$(dirname "${BASH_SOURCE[0]}")")"
repo_root="$(realpath "${script_dir}/../..")"

docker build -t poetassistant-agent-base -f "${repo_root}/.docker/agent-base/Dockerfile" "${repo_root}/.docker/agent-base"
docker build -t poetassistant-codex -f "${script_dir}/Dockerfile" "${script_dir}"

docker run --rm -it \
  -e GM_API_KEY="${GM_API_KEY}" \
  -e GIT_AUTHOR_NAME="$(git config user.name) (via Codex)" \
  -e GIT_AUTHOR_EMAIL="$(git config user.email)" \
  -e GIT_COMMITTER_NAME="$(git config user.name) (via Codex)" \
  -e GIT_COMMITTER_EMAIL="$(git config user.email)" \
  -v "${repo_root}":/opt/myproj \
  -v /dev/null:/opt/myproj/.env \
  -v /opt/myproj/node_modules \
  -v "${script_dir}/local.properties":/opt/myproj/local.properties \
  -v "${script_dir}/.gradle":/home/vibeuser/.gradle \
  -v "${script_dir}/.m2":/home/vibeuser/.m2 \
  -w /opt/myproj \
  --entrypoint bash poetassistant-codex
