FROM mcr.microsoft.com/devcontainers/base:2.0.4-noble

# Install JDK 17 and required tools
RUN apt-get update && export DEBIAN_FRONTEND=noninteractive \
    && apt-get -y install --no-install-recommends \
        openjdk-17-jdk \
        wget \
        unzip \
        git \
        curl \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
ENV PATH="${JAVA_HOME}/bin:${PATH}"

# Android SDK setup
ENV ANDROID_SDK_ROOT=/opt/android-sdk
ENV ANDROID_HOME=${ANDROID_SDK_ROOT}
ENV PATH="${ANDROID_SDK_ROOT}/cmdline-tools/latest/bin:${ANDROID_SDK_ROOT}/platform-tools:${PATH}"

RUN mkdir -p ${ANDROID_SDK_ROOT}/cmdline-tools

# Download Android command-line tools
RUN wget -q https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip \
        -O /tmp/cmdline-tools.zip \
    && unzip -q /tmp/cmdline-tools.zip -d /tmp/cmdline-tools-extract \
    && mv /tmp/cmdline-tools-extract/cmdline-tools ${ANDROID_SDK_ROOT}/cmdline-tools/latest \
    && rm -rf /tmp/cmdline-tools.zip /tmp/cmdline-tools-extract

# Accept licenses and install SDK components
RUN yes | sdkmanager --licenses > /dev/null 2>&1 || true \
    && sdkmanager \
        "platform-tools" \
        "platforms;android-36" \
        "build-tools;36.0.0"

# Allow vscode user to use the SDK
RUN chown -R vscode:vscode ${ANDROID_SDK_ROOT}
