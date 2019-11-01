#!/bin/bash

set -eu

export CARGO_HOME="$(pwd)/cargo-home"
export CARGO_TARGET_DIR="$(pwd)/cargo-target-dir"
export GRADLE_USER_HOME="$(pwd)/gradle-home"

pushd repo

make test-in-ci
