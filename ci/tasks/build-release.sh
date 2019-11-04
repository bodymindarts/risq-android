#!/bin/bash

set -eu

VERSION="$(cat version/number)"
OUT="$(pwd)/release"

export GRADLE_USER_HOME="$(pwd)/gradle-home"

pushd prepared-repo/git

echo ${KEYSTORE} | base64 --decode - >  app/risq-android.keystore
cat <<EOF > ${GRADLE_USER_HOME}/gradle.properties
storeFile=risq-android.keystore
storePassword=${STORE_PASSWORD}
keyAlias=${KEY_ALIAS}
keyPassword=${KEY_PASSWORD}
EOF

make build-release-in-ci

pushd rust/risq
RISQ_REV="$(git rev-parse --short HEAD)"
popd

# mv app/build/outputs/apk/release/app-release.apk ${OUT}/risq-${RISQ_REV}-android-${VERSION}.apk

gradle cargoBuild
gradle assemble

mv app/build/outputs/apk/debug/app-debug.apk ${OUT}/debug-risq-${RISQ_REV}-android-${VERSION}.apk
