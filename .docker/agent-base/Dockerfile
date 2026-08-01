FROM ubuntu:24.04

# Avoid prompts from apt during installation.
ENV DEBIAN_FRONTEND=noninteractive

# Common tooling for Android app development and test automation.
RUN apt-get update && apt-get install -y --no-install-recommends \
    bash \
    ca-certificates \
    curl \
    git \
    openjdk-21-jdk \
    python3 \
    python3-pip \
    python3-venv \
    unzip \
    wget \
    && rm -rf /var/lib/apt/lists/*

ENV JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
ENV PATH="${JAVA_HOME}/bin:${PATH}"

RUN mkdir -p /opt/android-sdk/platform-tools && \
    wget -q https://dl.google.com/android/repository/platform-tools-latest-linux.zip -O /tmp/platform-tools.zip && \
    unzip -q /tmp/platform-tools.zip -d /opt/android-sdk && \
    rm /tmp/platform-tools.zip && \
    wget -q https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -O /tmp/cmdline-tools.zip && \
    mkdir -p /opt/android-sdk/cmdline-tools/latest && \
    unzip -q /tmp/cmdline-tools.zip -d /tmp/cmdline-tools-temp && \
    mv /tmp/cmdline-tools-temp/cmdline-tools/* /opt/android-sdk/cmdline-tools/latest/ && \
    rm -rf /tmp/cmdline-tools.zip /tmp/cmdline-tools-temp

ENV ANDROID_SDK_ROOT=/opt/android-sdk
ENV ANDROID_HOME=/opt/android-sdk
ENV PATH="${ANDROID_SDK_ROOT}/cmdline-tools/latest/bin:${ANDROID_SDK_ROOT}/platform-tools:${PATH}"

RUN yes | sdkmanager --licenses
RUN sdkmanager "build-tools;36.0.0" "platforms;android-37.1" "platform-tools"

RUN useradd -ms /bin/bash vibeuser
USER vibeuser
WORKDIR /home/vibeuser

RUN python3 -m venv /home/vibeuser/venv && \
    /home/vibeuser/venv/bin/pip install --no-cache-dir --upgrade pip && \
    /home/vibeuser/venv/bin/pip install --no-cache-dir gmsaas

ENV PATH="/home/vibeuser/venv/bin:/home/vibeuser/.local/bin:${PATH}"

RUN gmsaas config set android-sdk-path /opt/android-sdk

# Verify installations.
RUN adb --version && gmsaas --version
