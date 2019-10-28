#!/bin/sh
LIB_NAME="risq_glue"
JNI_LIBS=../../app/src/main/jniLibs

cd rust/risq-glue
cargo build --target aarch64-linux-android --release
cargo build --target armv7-linux-androideabi --release
cargo build --target i686-linux-android --release

rm -rf $JNI_LIBS
mkdir $JNI_LIBS
mkdir $JNI_LIBS/arm64-v8a
mkdir $JNI_LIBS/armeabi-v7a
mkdir $JNI_LIBS/x86

cp target/aarch64-linux-android/release/lib${LIB_NAME}.so $JNI_LIBS/arm64-v8a/
cp target/armv7-linux-androideabi/release/lib${LIB_NAME}.so $JNI_LIBS/armeabi-v7a/
cp target/i686-linux-android/release/lib${LIB_NAME}.so $JNI_LIBS/x86/
