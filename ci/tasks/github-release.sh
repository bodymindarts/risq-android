#!/bin/bash

set -eu

VERSION=$(cat version/number)
WORKSPACE="$( cd "$( dirname "${BASH_SOURCE[0]}" )/../../../" >/dev/null && pwd )"
REPO_ROOT="${WORKSPACE}/prepared-repo/git"
RELEASE_ROOT="${WORKSPACE}/gh-release"
RELEASE_NAME="risq-android release"

mv ${REPO_ROOT}/../notes.md ${RELEASE_ROOT}/notes.md

mkdir -p ${RELEASE_ROOT}/artifacts
mv release/* ${RELEASE_ROOT}/artifacts/

echo "v${VERSION}"                         > ${RELEASE_ROOT}/tag
echo "${RELEASE_NAME} v${VERSION}"         > ${RELEASE_ROOT}/name
