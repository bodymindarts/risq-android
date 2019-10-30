#!/bin/sh
export REPO_ROOT="$( cd "$( dirname "${BASH_SOURCE[0]}" )/../" >/dev/null && pwd )"

LIB_NAME="risq_glue"
JNI_LIBS=${REPO_ROOT}/app/src/main/jniLibs

cd ${REPO_ROOT}/rust/risq-glue
export DOCKER_OPTS="--volume=${REPO_ROOT}/rust/risq:/risq"
cargo build --target aarch64-linux-android
cargo build --target armv7-linux-androideabi
cargo build --target i686-linux-android

rm -rf $JNI_LIBS
mkdir $JNI_LIBS
mkdir $JNI_LIBS/arm64-v8a
mkdir $JNI_LIBS/armeabi-v7a
mkdir $JNI_LIBS/x86

cp target/aarch64-linux-android/debug/lib${LIB_NAME}.so $JNI_LIBS/arm64-v8a/
cp target/armv7-linux-androideabi/debug/lib${LIB_NAME}.so $JNI_LIBS/armeabi-v7a/
cp target/i686-linux-android/debug/lib${LIB_NAME}.so $JNI_LIBS/x86/
