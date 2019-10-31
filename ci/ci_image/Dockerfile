FROM gradle:jdk8

USER root

# Install Build Essentials
RUN apt-get update && apt-get install -y \
      build-essential file apt-utils curl

ENV SDK_URL="https://dl.google.com/android/repository/sdk-tools-linux-4333796.zip" \
      ANDROID_HOME="/usr/local/android-sdk" \
      ANDROID_VERSION=29 \
      ANDROID_BUILD_TOOLS_VERSION=29.0.2

# Download Android SDK
RUN mkdir "$ANDROID_HOME" .android \
      && cd "$ANDROID_HOME" \
      && curl -o sdk.zip $SDK_URL \
      && unzip sdk.zip \
      && rm sdk.zip \
      && mkdir "$ANDROID_HOME/licenses" || true \
      && echo "24333f8a63b6825ea9c5514f83c2829b004d1fee" > "$ANDROID_HOME/licenses/android-sdk-license"
      # && yes | $ANDROID_HOME/tools/bin/sdkmanager --licenses

# Install Android Build Tool and Libraries
RUN mkdir -p /root/.android && touch /root/.android/repositories.cfg
RUN $ANDROID_HOME/tools/bin/sdkmanager --update
RUN $ANDROID_HOME/tools/bin/sdkmanager "build-tools;${ANDROID_BUILD_TOOLS_VERSION}" \
      "platforms;android-${ANDROID_VERSION}" \
      "platform-tools" \
      "ndk-bundle"

RUN curl https://sh.rustup.rs -sSf | \
      sh -s -- --default-toolchain stable-x86_64-unknown-linux-gnu -y

ENV PATH ${PATH}:/root/.cargo/bin

RUN rustup target add armv7-linux-androideabi
RUN rustup target add i686-linux-android
RUN rustup target add aarch64-linux-android
